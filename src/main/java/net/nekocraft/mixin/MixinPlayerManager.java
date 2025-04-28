package net.nekocraft.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent.OpenUrl;
import net.minecraft.text.ClickEvent.RunCommand;
import net.minecraft.text.HoverEvent.ShowText;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

import static net.nekocraft.NekoEssentials.logger;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Shadow
    public abstract int getCurrentPlayerCount();

    @Shadow
    public abstract MinecraftServer getServer();

//    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getServerModName()Ljava/lang/String;"))
//    public String onGetServerModName(MinecraftServer server) {
//        return "NekoCraft";
//    }

    @Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
    public void afterPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        MutableText joinMessage = MutableText.of(new Literal(""))
                .append("§7§m                           §r§7 [§eNekoCraft§r§7] §m                          §r\n")
                .append("§7  当前在线玩家: " + this.getCurrentPlayerCount() + "                     当前TPS: " + (int) (1000.0D / Math.max(50, this.getServer().getAverageTickTime() * 1.0E-6D)) + "\n")
                .append("§7  QQ 群: ")
                .append(MutableText.of((new Literal("7923309"))).styled(style -> {
                    try {
                        return style.withColor(Formatting.DARK_AQUA).withHoverEvent(new ShowText(Text.of("https://jq.qq.com/?k=5AzDYNC"))).withClickEvent(new OpenUrl(new URI("https://jq.qq.com/?k=5AzDYNC")));
                    } catch (URISyntaxException e) {
                        logger.warn("Cannot send qq group message to player: {}", e.getMessage());
                    }
                    return style;
                }))
                .append("§7      Telegram 群组: ")
                .append(MutableText.of((new Literal("@NekoCraft"))).styled(style -> {
                            try {
                                return style.withColor(Formatting.DARK_AQUA).withHoverEvent(new ShowText(Text.of("https://t.me/NekoCraft"))).withClickEvent(new OpenUrl(new URI("https://t.me/NekoCraft")));
                            } catch (URISyntaxException e) {
                                logger.warn("Cannot send tg group message to player: {}", e.getMessage());
                            }
                            return style;
                        }))
                .append("\n§7  用户中心 & 大地图: ")
                .append(MutableText.of((new Literal("user.neko-craft.com"))).styled(style -> {
                                    try {
                                        return style.withColor(Formatting.DARK_AQUA).withHoverEvent(new ShowText(Text.of("https://user.neko-craft.com"))).withClickEvent(new OpenUrl(new URI("https://user.neko-craft.com")));
                                    } catch (URISyntaxException e) {
                                        logger.warn("Cannot send user website message to player: {}", e.getMessage());
                                    }
                                    return style;
                                }))

                .append("\n§7  服务器地址 & 官网: ")
                .append(MutableText.of(new Literal("neko-craft.com")).styled(style -> {
                                    try {
                                        return style.withColor(Formatting.DARK_AQUA).withHoverEvent(new ShowText(Text.of("https://neko-craft.com"))).withClickEvent(new OpenUrl(new URI("https://neko-craft.com")));
                                    } catch (URISyntaxException e) {
                                        logger.warn("Cannot send website message to player: {}", e.getMessage());
                                    }
                                    return style;
                                }))
                .append(MutableText.of(new Literal("\n  由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!")).styled(style -> style.withColor(Formatting.YELLOW)))
                .append("\n  §7新 Fabric 服务端仍处于测试阶段, 如遇任何问题请加群反馈")
                .append("\n§7§m                                                                  §r\n");


        if (!((IMixinServerPlayerEntity) player).getAcceptedRules())
            joinMessage = joinMessage.append("  §7欢迎您来到 NekoCraft !\n  §e您需要点击 ").append(MutableText.of(new Literal("[这里]")).styled(style -> {
                try {
                    return style.withColor(Formatting.AQUA).withHoverEvent(new ShowText(Text.of("https://user.neko-craft.com/#/about"))).withClickEvent(new OpenUrl(new URI("https://user.neko-craft.com/#/about")));
                } catch (URISyntaxException e) {
                    logger.warn("Cannot send rules message to player: {}", e.getMessage());
                }
                return style;
            })).append("  §e来阅读服务器规定\n  §7点击确认后则默认您已阅读并遵守服务器规定!\n\n    ").append(MutableText.of(new Literal("[这里]")).styled(style -> style.withColor(Formatting.GREEN).withHoverEvent(new ShowText(Text.of("/acceptRule"))).withClickEvent(new RunCommand("/acceptRule")))).append("§7 或使用指令 /acceptRule\n\n    ").append(MutableText.of(new Literal("[这里]")).styled(style -> style.withColor(Formatting.RED).withHoverEvent(new ShowText(Text.of("/denyrule"))).withClickEvent(new RunCommand("/denyrule")))).append("§7 或使用指令 /denyrule\n");
        player.sendMessage(joinMessage);
    }
}
