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
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object AcceptRuleCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("acceptRule")
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    val source = context?.source
                    val player = source?.player
                    execute(source, player)
                }
        )
    }

    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity?): Int {
//        logger.info(kotlin.String.format("[rule][accept] %s", player))
        (player as IMixinServerPlayerEntity).acceptedRules = true
        val feedbackSupplier: () -> MutableText = {
            MutableText.of(Literal("[这里]"))
                .styled { style: Style? -> style!!.withColor(Formatting.AQUA) }
        }
        source?.sendFeedback(
            feedbackSupplier,
            false
        )

        player.getInventory().offerOrDrop(
            ItemStack(
                Items.COOKED_BEEF,
                64
            )
        )
        source?.server?.playerManager?.broadcast(
            MutableText.of(Literal("[这里]"))
                .append(
                    MutableText.of(Literal("[这里]"))
                        .styled { style: net.minecraft.text.Style? -> style!!.withColor(Formatting.AQUA) }
                )
                .append(player.displayName)
                .append(
                    MutableText.of(Literal("[这里]"))
                        .styled { style: Style? -> style!!.withColor(Formatting.AQUA) }
                ), false
        ) // TODO change overlay
        return 0
    }
}
