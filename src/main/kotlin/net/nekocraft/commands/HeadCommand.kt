package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.command.argument.ItemStackArgument
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.nekocraft.NekoEssentials.Companion.logger

object HeadCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("head")
                .then(
                    CommandManager.argument<GameProfileArgumentType.GameProfileArgument?>("player", GameProfileArgumentType.gameProfile())
                        .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                            execute(
                                context?.source, context?.source?.player,
                                GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                            )
                        })
                )
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context?.getSource(), context.getSource().getPlayer(),
                        context.getSource().getPlayer().getGameProfile()
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, profile: GameProfile): Int {
        val nbt = NbtCompound()
        nbt.putString("SkullOwner", profile.getName())
        val item = ItemStackArgument(Items.PLAYER_HEAD, nbt)
        val itemStack: ItemStack = item.createStack(1, false)
        logger.info(String.format("[head] %s with %s's skull", player, profile.getName()))
        val bl: Boolean = player.getInventory().insertStack(itemStack)
        if (bl && itemStack.isEmpty()) {
            itemStack.setCount(1)
            val itemEntity: ItemEntity? = player.dropItem(itemStack, false)
            itemEntity?.setDespawnImmediately()
            player.world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
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
