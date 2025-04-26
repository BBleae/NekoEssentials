package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import net.nekocraft.utils.SavedLocation

object BackCommand {
    private val NO_BACK_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(net.minecraft.text.Text.of("没有返回点可以前往"))
    private val INVALID_DIMENSION_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType(java.util.function.Function { id: Any? -> net.minecraft.text.Text.of("invalid back dimension: $id") })

    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("back")
                .executes { context: CommandContext<ServerCommandSource?>? ->
                    execute(
                        context?.source,
                        context?.source?.player
                    )
                }
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity): Int {
        val loc: SavedLocation = (player as IMixinServerPlayerEntity).getLastLocation()
        if (loc == null) throw NO_BACK_EXCEPTION.create()

        val registryKey: net.minecraft.registry.RegistryKey<World?>? =
            net.minecraft.registry.RegistryKey.of<World?>(RegistryKeys.WORLD, net.minecraft.util.Identifier(loc.world))
        val serverWorld: ServerWorld? = source.server.getWorld(registryKey)
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world)

        logger.info(java.lang.String.format("[back][teleport] %s -> %s", player, loc.asFullString()))
        (player as IMixinServerPlayerEntity).setLastLocation(
            SavedLocation(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()
            )
        )
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        source.sendFeedback(net.minecraft.text.Text.of("已传送到上次传送的位置"), false)

        return 0
    }
}
