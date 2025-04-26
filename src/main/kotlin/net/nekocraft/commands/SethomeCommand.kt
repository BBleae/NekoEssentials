package net.nekocraft.commands

import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object SethomeCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("sethome")
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        context.getSource().getPlayer()
                    )
                })
        )
    }

    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val world = player.getWorld().getRegistryKey().getValue().toString()
        val loc = SavedLocation(
            world,
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch()
        )
        logger.info(java.lang.String.format("[home][set] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).setHomeLocation(loc)
        source.sendFeedback(net.minecraft.text.Text.of("已成功在 " + loc.asString() + " 处设置家"), false)
        return 0
    }
}
