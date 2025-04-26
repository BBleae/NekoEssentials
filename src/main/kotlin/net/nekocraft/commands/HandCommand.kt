package net.nekocraft.commands

import net.minecraft.text.LiteralTextContent

object HandCommand {
    private val NO_ITEM_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("请将要展示的物品放在主手"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("hand")
                .executes(com.mojang.brigadier.Command { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context.getSource(),
                        java.util.Objects.requireNonNull<ServerPlayerEntity?>(context.getSource().getPlayer())
                    )
                })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val itemStack: ItemStack = player.getMainHandStack()
        if (itemStack.isEmpty()) throw NO_ITEM_EXCEPTION.create()

        logger.info(String.format("[hand] %s with %s", player, itemStack))
        val text: MutableText? = MutableText.of(LiteralTextContent("[这里]"))
            .append(player.getDisplayName())
            .append(
                MutableText.of(LiteralTextContent("[这里]"))
                    .styled(UnaryOperator { style: net.minecraft.text.Style? -> style!!.withColor(net.minecraft.util.Formatting.GRAY) })
            )
            .append(itemStack.toHoverableText())
        source.getServer().getPlayerManager().broadcast(text, false) // TODO fix this shit
        return 0
    }
}
