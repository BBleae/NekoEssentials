package net.nekocraft.config

import com.google.common.collect.Lists
import net.nekocraft.utils.SavedLocation

@me.shedaniel.autoconfig.annotation.Config(name = "neko-essentials")
class NekoConfig : me.shedaniel.autoconfig.ConfigData {
    var warpPoints: MutableList<String> =
        Lists.newArrayList("zero:minecraft:overworld,0,0,0")

    @Throws(me.shedaniel.autoconfig.ConfigData.ValidationException::class)
    override fun validatePostLoad() {
        for (point in warpPoints) {
            val pointSplit: Array<String?> = point.split(":".toRegex(), limit = 2).toTypedArray()
            if (pointSplit.size != 2) throw me.shedaniel.autoconfig.ConfigData.ValidationException("Warp point is invalid. ($point)")
            try {
                pointSplit[1]?.let { SavedLocation.load(it) }
            } catch (load: java.lang.RuntimeException) {
                throw me.shedaniel.autoconfig.ConfigData.ValidationException(load.message)
            }
        }
    }
}
