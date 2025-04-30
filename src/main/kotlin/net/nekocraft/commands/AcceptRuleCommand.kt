package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.util.Formatting
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object AcceptRuleCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("acceptRule").executes { context: CommandContext<ServerCommandSource?>? ->
                context?.source?.let { source ->
                    source.player?.let { player -> execute(source, player) }
                } ?: -1
            })
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        if ((player as IMixinServerPlayerEntity).acceptedRules) {
            source.sendFeedback({
                MutableText.of(Literal("你已经同意遵守了服务器规定"))
                    .styled { style -> style.withColor(Formatting.RED).withBold(true) }
            }, false)
            return 0
        }
        logger.info(String.format("[rule][accept] %s", player))
        (player as IMixinServerPlayerEntity).acceptedRules = true
        source.sendFeedback(
            {
                MutableText.of(Literal("感谢您接受了服务器的规定, 同时也希望您能一直遵守规定!"))
                    .styled { style -> style.withColor(Formatting.GREEN) }
            }, false
        )
        player.getInventory().offerOrDrop(ItemStack(Items.COOKED_BEEF, 64))
        val welcomeMessage = MutableText.of(Literal(""))
            .append(MutableText.of(Literal("欢迎新玩家 ")).styled { style -> style.withColor(Formatting.AQUA) })
            .append(player.getDisplayName())
            .append(MutableText.of(Literal(" 加入服务器!")).styled { style -> style.withColor(Formatting.AQUA) })
        source.server.playerManager.broadcast(welcomeMessage, false)
        source.server.playerManager.broadcast(welcomeMessage, true)
        return 1
    }
}
