package net.nekocraft.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.world.WorldSaveHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.nekocraft.mixinInterfaces.IMixinWorldSaveHandler;

import java.io.File;

import static net.nekocraft.NekoEssentials.logger;

@Mixin(WorldSaveHandler.class)
public abstract class MixinWorldSaveHandler implements IMixinWorldSaveHandler {
    @Shadow
    @Final
    private File playerDataDir;

    @Nullable
    public NbtCompound loadPlayerData(GameProfile profile) {
        NbtCompound nbtCompound = null;
        try {
            File file = new File(this.playerDataDir, profile.getId() + ".dat");
            if (file.exists() && file.isFile()) {
                nbtCompound = NbtIo.readCompressed(file);
            }
        } catch (Exception var4) {
            logger.warn("Failed to load player data for {}", profile.getName());
        }
        return nbtCompound;
    }

    public void savePlayerData(GameProfile profile, NbtCompound nbtCompound) {
        try {
            File file = File.createTempFile(profile.getId() + "-", ".dat", this.playerDataDir);
            NbtIo.writeCompressed(nbtCompound, file);
            File file2 = new File(this.playerDataDir, profile.getId() + ".dat");
            File file3 = new File(this.playerDataDir, profile.getId() + ".dat_old");
            Util.backupAndReplace(file2, file, file3);
        } catch (Exception var6) {
            logger.warn("Failed to save player data for {}", profile.getName());
        }
    }
}
