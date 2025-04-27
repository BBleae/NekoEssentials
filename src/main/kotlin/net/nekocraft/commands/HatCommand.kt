package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.PlainTextContent.Literal
import net.minecraft.text.MutableText
import net.nekocraft.NekoEssentials.Companion.logger

object HatCommand {
    private val NO_ITEM_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("请将要穿戴的物品放在主手"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("hat")
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    context?.source?.let { source ->
                        source.player?.let { player ->
                            execute(
                                source,
                                player
                            )
                        }
                    } ?: -1
                }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val itemStack: ItemStack = player.getMainHandStack()
        if (itemStack.isEmpty) throw NO_ITEM_EXCEPTION.create()

        val hat: ItemStack = player.getEquippedStack(EquipmentSlot.HEAD)
        val itemStackCopy: ItemStack = itemStack.copy()
        itemStackCopy.count = 1
        player.equipStack(EquipmentSlot.HEAD, itemStackCopy)
        itemStack.decrement(1)
        if (!hat.isEmpty) {
            val bl: Boolean = player.getInventory().insertStack(hat)
            if (bl && hat.isEmpty) {
                hat.count = 1
                val itemEntity: ItemEntity? = player.dropItem(hat, false)
                itemEntity?.setDespawnImmediately()

                player.world.playSound(
                    null, player.x, player.y, player.z,
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f
                )
                player.currentScreenHandler.sendContentUpdates()
            } else {
                val itemEntity: ItemEntity? = player.dropItem(hat, false)
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay()
                    itemEntity.setOwner(player.getUuid())
                }
            }
        }

        logger.info(String.format("[hat] %s with %s", player, itemStackCopy))
        source.sendFeedback(
            { MutableText.of(Literal("已穿戴 ")).append(itemStackCopy.toHoverableText()) },
            false
        )

        return 0
    }
}
