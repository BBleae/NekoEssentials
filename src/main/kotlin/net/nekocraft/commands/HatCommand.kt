package net.nekocraft.commands

import net.minecraft.text.LiteralTextContent

object HatCommand {
    private val NO_ITEM_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("请将要穿戴的物品放在主手"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("hat")
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        context.getSource().getPlayer()
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val itemStack: ItemStack = player.getMainHandStack()
        if (itemStack.isEmpty()) throw NO_ITEM_EXCEPTION.create()

        val hat: ItemStack = player.getEquippedStack(EquipmentSlot.HEAD)
        val itemStackCopy: ItemStack = itemStack.copy()
        itemStackCopy.setCount(1)
        player.equipStack(EquipmentSlot.HEAD, itemStackCopy)
        itemStack.decrement(1)
        if (!hat.isEmpty()) {
            val bl: Boolean = player.getInventory().insertStack(hat)
            if (bl && hat.isEmpty()) {
                hat.setCount(1)
                val itemEntity: ItemEntity? = player.dropItem(hat, false)
                itemEntity?.setDespawnImmediately()

                player.world.playSound(
                    null, player.getX(), player.getY(), player.getZ(),
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
            MutableText.of(LiteralTextContent("已穿戴 ")).append(itemStackCopy.toHoverableText()),
            false
        )

        return 0
    }
}
