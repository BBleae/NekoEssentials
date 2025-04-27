package net.nekocraft.mixinInterfaces

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.NbtCompound

interface IMixinPlayerSaveHandler {
    fun `nekoEssentials$loadPlayerData`(profile: GameProfile?): NbtCompound?

    fun `nekoEssentials$savePlayerData`(profile: GameProfile?, nbtCompound: NbtCompound?)
}
