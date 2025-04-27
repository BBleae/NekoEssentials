package net.nekocraft.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity;
import net.nekocraft.utils.SavedLocation;
import net.nekocraft.utils.TpaRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

import static net.nekocraft.NekoEssentials.logger;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements IMixinServerPlayerEntity {
    @Unique
    private final HashMap<UUID, TpaRequest> tpaReqs = new HashMap<>();
    @Unique
    private final HashMap<UUID, TpaRequest> tpaReqds = new HashMap<>();
    @Unique
    @Nullable
    private SavedLocation homeLocation;
    @Unique
    @Nullable
    private SavedLocation lastLocation;
    @Unique
    @Nullable
    private SavedLocation toggleLocation;
    @Unique
    private boolean acceptedRules = false;

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void sendMessage(Text message);

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void afterReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.getCompound("homeLocation").ifPresent(it ->
                homeLocation = SavedLocation.formNBT(it));

        nbt.getCompound("lastLocation").ifPresent(it ->
                lastLocation = SavedLocation.formNBT(it));

        nbt.getCompound("toggleLocation").ifPresent(it ->
                toggleLocation = SavedLocation.formNBT(it));

        nbt.getCompound("acceptedRules").ifPresent(it ->
                acceptedRules = it.getBoolean("acceptedRules").orElse(false));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void afterWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (homeLocation != null)
            nbt.put("homeLocation", homeLocation.asNBT());
        if (lastLocation != null)
            nbt.put("lastLocation", lastLocation.asNBT());
        if (toggleLocation != null)
            nbt.put("toggleLocation", toggleLocation.asNBT());
        if (acceptedRules)
            nbt.putBoolean("acceptedRules", true);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTick(CallbackInfo ci) {
        long now = Util.getMeasuringTimeMs();
        Iterator<TpaRequest> iterator = tpaReqds.values().iterator();
        while (iterator.hasNext()) {
            TpaRequest n = iterator.next();
            if (n.finished) {
                iterator.remove();
            } else if (n.reqTime + 30 * 1000L < now) {
                n.setFinished();
                iterator.remove();
                ServerPlayerEntity to = Objects.requireNonNull(this.getWorld().getServer(), "cannot get server?").getPlayerManager().getPlayer(n.to);
                if (to == null) continue;
                logger.info("[tpa][timeout] {} -> {}", this, to);
                this.sendMessage(MutableText.of(new Literal("[这里]")), true);
                to.sendMessage(MutableText.of(new Literal("[这里]")), true);
            }
        }
        tpaReqs.values().removeIf(n -> n.finished);
    }

    @Inject(method = "onDeath", at = @At("RETURN"))
    public void afterDeath(DamageSource source, CallbackInfo ci) {
        lastLocation = new SavedLocation(this.getWorld().getRegistryKey().getValue().toString(),
                this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void afterCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.acceptedRules = ((IMixinServerPlayerEntity) oldPlayer).getAcceptedRules();
        this.lastLocation = ((IMixinServerPlayerEntity) oldPlayer).getLastLocation();
        this.homeLocation = ((IMixinServerPlayerEntity) oldPlayer).getHomeLocation();
        this.toggleLocation = ((IMixinServerPlayerEntity) oldPlayer).getToggleLocation();
    }

    @Override
    public @Nullable SavedLocation getHomeLocation() {
        return homeLocation;
    }

    @Override
    public void setHomeLocation(@Nullable SavedLocation i) {
        homeLocation = i;
    }

    @Override
    public @Nullable SavedLocation getToggleLocation() {
        return toggleLocation;
    }

    @Override
    public void setToggleLocation(@Nullable SavedLocation i) {
        toggleLocation = i;
    }

    @Override
    public @Nullable SavedLocation getLastLocation() {
        return lastLocation;
    }

    @Override
    public void setLastLocation(@Nullable SavedLocation i) {
        lastLocation = i;
    }

    public void requestedTpa(TpaRequest req) {
        tpaReqds.put(req.to, req);
    }

    public void requestTpa(TpaRequest req) {
        tpaReqs.put(req.from, req);
    }

    public HashMap<UUID, TpaRequest> getTpaReqs() {
        return tpaReqs;
    }

    public HashMap<UUID, TpaRequest> getTpaReqds() {
        return tpaReqds;
    }

    public boolean getAcceptedRules() {
        return acceptedRules;
    }

    public void setAcceptedRules(boolean i) {
        acceptedRules = i;
    }
}
