package net.nekocraft.mixinInterfaces

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.NbtCompound

interface IMixinWorldSaveHandler {
    fun loadPlayerData(profile: GameProfile?): NbtCompound?

    fun savePlayerData(profile: GameProfile?, nbtCompound: NbtCompound?)
}
