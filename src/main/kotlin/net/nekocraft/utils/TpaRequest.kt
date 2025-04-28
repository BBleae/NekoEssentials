package net.nekocraft.utils

import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity
import java.util.*

class TpaRequest @JvmOverloads constructor(
    private val server: MinecraftServer,
    @JvmField val from: UUID?,
    @JvmField val to: UUID?,
    reqTime: Long = net.minecraft.util.Util.getMeasuringTimeMs(),
    here: Boolean = false
) {

    @JvmField
    var reqTime: Long

    @JvmField
    var finished: Boolean = false
    var here: Boolean

    constructor(server: MinecraftServer, from: UUID?, to: UUID?, here: Boolean) : this(
        server, from, to, net.minecraft.util.Util.getMeasuringTimeMs(), here
    )

    init {
        this.reqTime = reqTime
        this.here = here
    }

    fun setFinished() {
        finished = true
    }

    fun refresh() {
        this.reqTime = net.minecraft.util.Util.getMeasuringTimeMs()
    }

    private fun teleportPlayer(a: ServerPlayerEntity, b: ServerPlayerEntity) {
        (a as IMixinServerPlayerEntity).lastLocation = SavedLocation(
            a.world.registryKey.value.toString(), a.x, a.y, a.z, a.yaw, a.pitch
        )

        a.teleport(
            b.world as ServerWorld, b.x, b.y, b.z, EnumSet.noneOf(PositionFlag::class.java), b.yaw, b.pitch, false
        )
    }

    fun execute() {
        if (finished) return
        val from: ServerPlayerEntity? = server.playerManager.getPlayer(this.from)
        val to: ServerPlayerEntity? = server.playerManager.getPlayer(this.to)
        if (from == null || to == null) {
            this.setFinished()
            return
        }
        if (this.here) teleportPlayer(to, from)
        else teleportPlayer(from, to)

        this.setFinished()
    }
}
