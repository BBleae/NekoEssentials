package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.PlayerManager
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Texts
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.TpaRequest
import java.util.*

object TpAcceptCommand {
    private val NO_TPA_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(Text.of("你还没有收到过任何传送请求"))
    private val NO_TPA_FROM_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { playerName: Any? ->
            Text.of("你还没有收到过来自 $playerName 的任何传送请求")
        }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("tpaccept").then(
                CommandManager.argument("target", EntityArgumentType.player())
                    .executes { context: CommandContext<ServerCommandSource?>? ->
                        context?.source?.let { source ->
                            source.player?.let { player ->
                                execute(
                                    source,
                                    player,
                                    EntityArgumentType.getPlayer(context, "target")
                                )
                            }
                        } ?: -1
                    }
            ).executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    context.source?.player?.let { player ->
                        execute(
                            source, player
                        )
                    }
                } ?: -1
            }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val reqs: HashMap<UUID?, TpaRequest?>? = (player as IMixinServerPlayerEntity).tpaReqs
        if (reqs == null || reqs.isEmpty()) {
            throw NO_TPA_EXCEPTION.create()
        }
        if (reqs.size > 1) {
            val msg: MutableText = MutableText.of(Literal("请从下列待接收请求中选择一个想要接受的请求: "))
            val playerManager: PlayerManager = source.server.playerManager

            val accepts: MutableList<Text?> = com.google.common.collect.Lists.newArrayList()
            for (req in reqs.values) {
                if (req == null) continue

                val from: ServerPlayerEntity? = playerManager.getPlayer(req.from)
                if (from == null) {
                    reqs.remove(req.from)
                } else {
                    accepts.add(
                        Texts.bracketed(
                            MutableText.of(Literal("")).append(from.getDisplayName())
                        ).styled { style: Style? ->
                            style!!.withColor(Formatting.AQUA)
                                .withHoverEvent(ShowText(Text.of("/tpaccept " + from.name.string)))
                                .withClickEvent(RunCommand("/tpaccept " + from.name.string))
                        }
                    )
                }
            }
            msg.append(Texts.join(accepts, Text.of(", ")))
            source.sendFeedback({ msg }, false)
        }
        if (reqs.size == 1) {
            val req: TpaRequest? = reqs.values.iterator().next()
            if (req == null) {
                throw NO_TPA_EXCEPTION.create()
            }
            val target: ServerPlayerEntity? = source.server.playerManager.getPlayer(req.from)
            if (target == null) {
                reqs.remove(req.from)
                throw NO_TPA_EXCEPTION.create()
            }
            return execute(source, player, target)
        }
        return 0
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val req: TpaRequest? = (player as IMixinServerPlayerEntity).tpaReqs?.get(target.uuid)
        if (req == null) {
            throw NO_TPA_FROM_EXCEPTION.create(target.displayName?.string ?: target.name.string ?: "unknown name")
        }
        logger.info(String.format("[tpa][accept] %s -> %s", target, player))
        req.execute()
        target.sendMessage(MutableText.of(Literal("发送到 ")).append(player.displayName).append(" 的传送请求已被接受"))
        source.sendFeedback({ MutableText.of(Literal("已接受来自 ")).append(target.displayName).append(" 的传送请求") }, false)
        return 0
    }
}
