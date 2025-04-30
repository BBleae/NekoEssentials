package net.nekocraft.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler extends ServerCommonNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    public MixinServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Shadow
    public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @Unique
    private final HashSet<String> notified = new HashSet<>();

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() && (packet.getX(player.getX()) != player.getX() || packet.getY(player.getY()) != player.getY() || packet.getZ(player.getZ()) != player.getZ())) {
            this.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforeClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void beforePlayerSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String string = packet.chatMessage();
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() && !Objects.equals(string, "/acceptRule") && !Objects.equals(string, "/denyRule")) {
            this.sendPacket(new OverlayMessageS2CPacket(Text.of("§c你还没有打开聊天框点击§a[同意服务器规定]§c!")));
            ci.cancel();
        }
    }

    @Inject(method = "handleDecoratedMessage", at = @At(value = "HEAD"))
    public void beforeHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        notified.clear();
    }

    // TODO: 重新设置聊天格式
    @Inject(method = "handleDecoratedMessage", at = @At(value = "TAIL"))
    public void afterHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        Text playerDisplayName = player.getDisplayName();

        String playerName = playerDisplayName != null ? playerDisplayName.getString() : "";
        PlayerManager playerManager = this.server.getPlayerManager();
        ServerPlayerEntity chatPlayer = playerManager.getPlayer(playerName);
        if (chatPlayer == null) return;

        MutableText result = MutableText.of(new Literal(""));
        if (chatPlayer.hasPermissionLevel(2)) {
            result.append(MutableText.of(new Literal("<")).styled(style -> style.withColor(Formatting.GREEN))).append(playerDisplayName).append(MutableText.of(new Literal(">")).styled(style -> style.withColor(Formatting.GREEN)));
        } else {
            result.append("<").append(playerDisplayName).append(">");
        }

        String[] parts = message.getContent().getString().split("\\s");
        for (String part : parts) {
            result.append(" ");
            ServerPlayerEntity atPlayer = playerManager.getPlayer(part);
            if (atPlayer != null) {
                result.append(MutableText.of(new Literal("@")).append(atPlayer.getDisplayName()).styled(style -> style.withColor(Formatting.GREEN)));
                if (!notified.contains(atPlayer.getUuidAsString())) {
                    notified.add(atPlayer.getUuidAsString());
                    atPlayer.sendMessage(MutableText.of(new Literal("")).append(MutableText.of(new Literal("有一位叫 ")).styled(style -> style.withColor(Formatting.GREEN))).append(chatPlayer.getDisplayName()).append(MutableText.of(new Literal(" 的小朋友@了你")).styled(style -> style.withColor(Formatting.GREEN))));
                }
            } else {
                result.append(MutableText.of(new Literal(part)).styled(style -> style.withColor(Formatting.GRAY)));
            }
        }
//        var args = new ArrayList();
//        args.add(result.getContent().toString());
//        return new TranslatableTextContent("disconnect.genericReason", null, args);
    }
}
