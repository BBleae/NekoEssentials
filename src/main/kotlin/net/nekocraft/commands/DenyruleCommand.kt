package net.nekocraft.commands

import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity

object DenyruleCommand {
    private val ACCEPTED_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("你已经同意遵守了服务器规定"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("denyrule")
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        context.getSource().getPlayer()
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity): Int {
        logger.info(String.format("[rule][deny] %s", player))
        if ((player as IMixinServerPlayerEntity).getAcceptedRules()) throw ACCEPTED_EXCEPTION.create()
        player.networkHandler.disconnect(net.minecraft.text.Text.of("§e[NekoCraft] §c你拒绝遵守服务器规定"))
        return 0
    }
}
