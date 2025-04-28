package net.nekocraft.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.Util;
import net.minecraft.world.PlayerSaveHandler;
import net.nekocraft.mixinInterfaces.IMixinPlayerSaveHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

import static net.nekocraft.NekoEssentials.logger;

@Mixin(PlayerSaveHandler.class)
public abstract class MixinPlayerSaveHandler implements IMixinPlayerSaveHandler {
    @Shadow
    @Final
    private File playerDataDir;

    @Nullable
    public NbtCompound nekoEssentials$loadPlayerData(GameProfile profile) {
        NbtCompound nbtCompound = null;
        try {
            File file = new File(this.playerDataDir, profile.getId() + ".dat");
            if (file.exists() && file.isFile()) {
                nbtCompound = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
            }
        } catch (Exception var4) {
            logger.warn("Failed to load player data for {}", profile.getName());
        }
        return nbtCompound;
    }

    public void nekoEssentials$savePlayerData(GameProfile profile, NbtCompound nbtCompound) {
        try {
            File file = File.createTempFile(profile.getId() + "-", ".dat", this.playerDataDir);
            NbtIo.writeCompressed(nbtCompound, file.toPath());
            File file2 = new File(this.playerDataDir, profile.getId() + ".dat");
            File file3 = new File(this.playerDataDir, profile.getId() + ".dat_old");
            Util.backupAndReplace(file2.toPath(), file.toPath(), file3.toPath());
        } catch (Exception var6) {
            logger.warn("Failed to save player data for {}", profile.getName());
        }
    }
}
