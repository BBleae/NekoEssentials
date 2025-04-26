package net.nekocraft.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface MixinPlayerManagerAccessor {
    @Accessor
    WorldSaveHandler getSaveHandler();
}
