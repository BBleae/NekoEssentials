package net.nekocraft.config

import net.nekocraft.utils.SavedLocation

object NekoConfigParsed {
    var warpPoints: HashMap<String?, SavedLocation?>? = null

    fun load(config: NekoConfig) {
        warpPoints = HashMap()
        for (point in config.warpPoints) {
            val pointSplit: Array<String?> = point.split(":".toRegex(), limit = 2).toTypedArray()
            pointSplit[1]?.let { warpPoints!!.put(pointSplit[0], SavedLocation.load(it)) }
        }
    }
}
