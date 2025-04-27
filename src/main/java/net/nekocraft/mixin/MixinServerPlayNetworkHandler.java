package net.nekocraft.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity;

import java.util.HashSet;
import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Unique
    private final HashSet<String> notified = new HashSet<>();

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() &&
                (packet.getX(player.getX()) != player.getX() ||
                        packet.getY(player.getY()) != player.getY() ||
                        packet.getZ(player.getZ()) != player.getZ())) {
            this.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforeClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void beforePlayerSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String string = packet.chatMessage();
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() &&
                !Objects.equals(string, "/acceptrule") && !Objects.equals(string, "/denyrule")) {
            this.sendPacket(new OverlayMessageS2CPacket(Text.of("§c你还没有打开聊天框点击§a[同意服务器规定]§c!")));
            ci.cancel();
        }
    }

    @Inject(method = "handleMessage", at = @At(value = "HEAD"))
    public void beforeHandleMessage(TextStream.Message message, CallbackInfo ci) {
        notified.clear();
    }

    // TODO: fix "Unable to locate class mapping for @At(NEW.<target>) 'V'"
    @Redirect(method = "handleMessage", at = @At(value = "NEW", target = "Lnet/minecraft/text/TranslatableTextContent;"))
    public TranslatableTextContent onHandleMessageNewTranslatableText(String key, Object... args) {
        if (!Objects.equals(key, "chat.type.text")) return new TranslatableTextContent(key, args);

        Text playerDisplayName = (Text) args[0];
        String message = (String) args[1];

        String playerName = playerDisplayName.getString();
        PlayerManager playerManager = this.server.getPlayerManager();
        ServerPlayerEntity chatPlayer = playerManager.getPlayer(playerName);
        if (chatPlayer == null) return new TranslatableTextContent(key, args);

        MutableText result = MutableText.of(new Literal("[这里]"));
        if (chatPlayer.hasPermissionLevel(2)) {
            result.append(MutableText.of(new Literal("[这里]")).styled(style -> style.withColor(Formatting.GREEN)))
                    .append(playerDisplayName)
                    .append(MutableText.of(new Literal("[这里]")).styled(style -> style.withColor(Formatting.GREEN)));
        } else {
            result.append("<").append(playerDisplayName).append(">");
        }

        String[] parts = message.split("\\s");
        for (String part : parts) {
            result.append(" ");
            ServerPlayerEntity atPlayer = playerManager.getPlayer(part);
            if (atPlayer != null) {
                result.append(MutableText.of(new Literal("[这里]"))
                        .append(atPlayer.getDisplayName())
                        .styled(style -> style
                                .withColor(Formatting.GREEN)));
                if (!notified.contains(atPlayer.getUuidAsString())) {
                    notified.add(atPlayer.getUuidAsString());
                    atPlayer.sendMessage(MutableText.of(new Literal("[这里]"))
                            .append(MutableText.of(new Literal("[这里]")).styled(style -> style
                                    .withColor(Formatting.GREEN)))
                            .append(chatPlayer.getDisplayName())
                            .append(MutableText.of(new Literal("[这里]")).styled(style -> style
                                    .withColor(Formatting.GREEN))));
                }
            } else {
                result.append(MutableText.of(new Literal(part)).styled(style -> style
                        .withColor(Formatting.GRAY)));
            }
        }

        return new TranslatableTextContent("disconnect.genericReason", result.getContent().toString());
    }
}
