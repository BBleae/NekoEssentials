package net.nekocraft.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.command.argument.ItemStackArgument
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.nekocraft.NekoEssentials.Companion.logger

object HeadCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("head")
                .then(
                    CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes { context: CommandContext<ServerCommandSource?>? ->
                            context?.source?.player?.let {
                                execute(
                                    context.source, it,
                                    GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                                )
                            }                             ?: -1
                        }
                )
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    context?.getSource()?.player?.let {
                        execute(
                            context.getSource(), it,
                            context.getSource()?.player.getGameProfile()
                        )
                    }                    ?: -1
                }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, profile: GameProfile): Int {
        val nbt = NbtCompound()
        nbt.putString("SkullOwner", profile.name)
        val item = ItemStackArgument(Items.PLAYER_HEAD, nbt)
        val itemStack: ItemStack = item.createStack(1, false)
        logger.info(String.format("[head] %s with %s's skull", player, profile.name))
        val bl: Boolean = player.getInventory().insertStack(itemStack)
        if (bl && itemStack.isEmpty) {
            itemStack.count = 1
            val itemEntity: ItemEntity? = player.dropItem(itemStack, false)
            itemEntity?.setDespawnImmediately()
            player.world.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
                ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
            player.currentScreenHandler.sendContentUpdates()
        } else {
            val itemEntity: ItemEntity? = player.dropItem(itemStack, false)
            if (itemEntity != null) {
                itemEntity.resetPickupDelay()
                itemEntity.setOwner(player.getUuid())
            }
        }
        return 0
    }
}
