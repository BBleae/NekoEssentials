package net.nekocraft

import carpet.CarpetServer
import com.mojang.brigadier.CommandDispatcher
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.nekocraft.commands.CommandRegistry
import net.nekocraft.config.NekoConfig
import net.nekocraft.config.NekoConfigParsed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class NekoEssentials : ModInitializer {
    var luckPermsApi: LuckPerms? = null

    override fun onInitialize() {
        logger.trace("onInitializeServer")

        logger.debug("registering configs")

        AutoConfig.register(NekoConfig::class.java) { definition: Config?, configClass: Class<NekoConfig?>? ->
            GsonConfigSerializer(
                definition, configClass
            )
        }
        rawConfig = AutoConfig.getConfigHolder(NekoConfig::class.java).getConfig()


        rawConfig?.let { NekoConfigParsed.load(it) }

        logger.debug("registering event listeners")
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer? ->
            this.onServerStarting(
                server
            )
        })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping { server: MinecraftServer? ->
            this.onServerStopping(
                server
            )
        })

        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback
            {dispatcher, access, environment ->
                this.onCommandRegistering(
                    dispatcher, environment.dedicated
                )
            })

        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server: MinecraftServer? ->
            server?.let {
                this.onEndTick(
                    it
                )
            }
        })

        logger.debug("NekoEssentials initialized")
    }

    private fun onServerStarting(server: MinecraftServer?) {
        logger.trace("onServerStarting")

        logger.debug("get LuckPerms api")
        luckPermsApi = LuckPermsProvider.get()
    }

    private fun onServerStopping(server: MinecraftServer?) {
        logger.trace("onServerStopping")
    }

    private fun onCommandRegistering(dispatcher: CommandDispatcher<ServerCommandSource?>?, isDedicated: Boolean) {
        logger.trace("onCommandRegistering")

        dispatcher?.let { CommandRegistry.register(it) }
    }

    private fun onEndTick(server: MinecraftServer) {
        if (server.ticks % 64 == 0) {
            var message = "NekoCraft"
            when (server.ticks / 64 % 2) {
                0 -> {
                    val overworld: ServerWorld? = server.getWorld(World.OVERWORLD)
                    if (overworld != null) message = "正在经历第" + overworld.levelProperties.time + "个tick的NekoCraft"
                }

                1 -> message = "正在与" + server.playerManager.currentPlayerCount + "只猫猫玩耍的NekoCraft"
            }
            // TODO send packet to all players
//            for (player in server.playerManager.playerList) {
//                player.networkHandler.sendPacket(
//                    CustomPayloadS2CPacket(
//                        CustomPayloadS2CPacket.BRAND,
//                        PacketByteBuf(Unpooled.buffer()).writeString(message)
//                    )
//                )
//            }
        }
    }

    companion object {
        @JvmField
        var logger: Logger = LogManager.getLogger("NekoEssentials")
        var rawConfig: NekoConfig? = null

        fun getLogger():Logger {
            return logger
        }

        init {
            CarpetServer.manageExtension(NekoEssentialsCarpet())
        }
    }
}
