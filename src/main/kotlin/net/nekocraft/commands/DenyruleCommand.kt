package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity


object DenyruleCommand {
    private val ACCEPTED_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("你已经同意遵守了服务器规定"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("denyRule")
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    context?.source?.player?.let {
                        execute(
                            context.source,
                            it
                        )
                    } ?: -1
                }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity): Int {
        logger.info(String.format("[rule][deny] %s", player))
        if ((player as IMixinServerPlayerEntity).acceptedRules) throw ACCEPTED_EXCEPTION.create()
        player.networkHandler.disconnect(net.minecraft.text.Text.of("§e[NekoCraft] §c你拒绝遵守服务器规定"))
        return 0
    }
}
