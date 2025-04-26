package net.nekocraft.utils

import net.minecraft.nbt.NbtCompound

class SavedLocation(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {

    fun save(): String {
        return String.format("%s,%f,%f,%f,%f,%f", world, x, y, z, yaw, pitch)
    }

    fun asString(): String {
        return String.format("[%s, %.2f, %.2f, %.2f]", world, x, y, z)
    }

    fun asFullString(): String {
        return String.format("[%s, %f, %f, %f, %f, %f]", world, x, y, z, yaw, pitch)
    }

    fun asNBT(): NbtCompound {
        val nbtCompound = NbtCompound()
            nbtCompound.putString("world", this.world)
            nbtCompound.putDouble("x", this.x)
            nbtCompound.putDouble("y", this.y)
            nbtCompound.putDouble("z", this.z)
            nbtCompound.putFloat("yaw", this.yaw)
            nbtCompound.putFloat("pitch", this.pitch)
        return nbtCompound
    }

    companion object {
        fun load(str: String): SavedLocation {
            val strings: Array<String?> = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return when (strings.size) {
                4 -> {
                    SavedLocation(
                        strings[0]!!,
                        strings[1]!!.toDouble(), strings[2]!!.toDouble(), strings[3]!!.toDouble(),
                        0f, 0f
                    )
                }
                6 -> {
                    SavedLocation(
                        strings[0]!!,
                        strings[1]!!.toDouble(), strings[2]!!.toDouble(), strings[3]!!.toDouble(),
                        strings[4]!!.toFloat(), strings[5]!!.toFloat()
                    )
                }
                else -> {
                    throw RuntimeException("Location string is invalid. ($str)")
                }
            }
        }

        @JvmStatic
        fun formNBT(nbt: NbtCompound): SavedLocation {
            return SavedLocation(
                nbt.getString("world").orElse(""),
                nbt.getDouble("x").orElse(.0), nbt.getDouble("y").orElse(.0), nbt.getDouble("z").orElse(.0),
                nbt.getFloat("yaw").orElse(0f), nbt.getFloat("pitch").orElse(0f)
            )
        }
    }
}
