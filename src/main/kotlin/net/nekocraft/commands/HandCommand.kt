package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger

object HandCommand {
    private val NO_ITEM_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("请将要展示的物品放在主手"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("hand")
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    context?.source?.let {
                        context.source?.player?.let { player ->
                            execute(
                                it,
                                player
                            )
                        }
                    } ?: -1
                }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val itemStack: ItemStack = player.mainHandStack
        if (itemStack.isEmpty) throw NO_ITEM_EXCEPTION.create()

        logger.info(String.format("[hand] %s with %s", player, itemStack))
        val text: MutableText? = MutableText.of(Literal("[这里]"))
            .append(player.getDisplayName())
            .append(
                MutableText.of(Literal("[这里]"))
                    .styled { style: Style? -> style!!.withColor(Formatting.GRAY) }
            )
            .append(itemStack.toHoverableText())
        source.server.playerManager.broadcast(text, false) // TODO fix this shit
        return 0
    }
}
