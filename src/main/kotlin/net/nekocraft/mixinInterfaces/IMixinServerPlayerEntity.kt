package net.nekocraft.mixinInterfaces

import net.nekocraft.utils.SavedLocation
import net.nekocraft.utils.TpaRequest
import java.util.*

interface IMixinServerPlayerEntity {
    var homeLocation: SavedLocation?

    var lastLocation: SavedLocation

    fun requestedTpa(req: TpaRequest?)

    fun requestTpa(req: TpaRequest?)

    val tpaReqs: HashMap<UUID?, TpaRequest?>?

    val tpaReqds: HashMap<UUID?, TpaRequest?>?

    var acceptedRules: Boolean

    var toggleLocation: SavedLocation?
}