package net.nekocraft.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.nekocraft.mixinInterfaces.IMixinServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Shadow
    public abstract int getCurrentPlayerCount();

    @Shadow
    public abstract MinecraftServer getServer();

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getServerModName()Ljava/lang/String;"))
    public String onGetServerModName(MinecraftServer server) {
        return "NekoCraft";
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
    public void afterPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        MutableText joinMessage = MutableText.of(new LiteralTextContent(""))
                .append("§7§m                           §r§7 [§eNekoCraft§r§7] §m                          §r\n")
                .append("§7  当前在线玩家: " + this.getCurrentPlayerCount() + "                     当前TPS: "
                        + (int) (1000.0D / Math.max(50, MathHelper.average(this.getServer().lastTickLengths) * 1.0E-6D)) + "\n")
                .append("§7  QQ 群: ")
                .append(MutableText.of(new LiteralTextContent("7923309")).styled(style -> style
                        .withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("https://jq.qq.com/?k=5AzDYNC")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?k=5AzDYNC"))))
                .append("§7      Telegram 群组: ")
                .append(MutableText.of(new LiteralTextContent("@NekoCraft")).styled(style -> style
                        .withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("https://t.me/NekoCraft")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/NekoCraft"))))
                .append("\n§7  用户中心 & 大地图: ")
                .append(MutableText.of(new LiteralTextContent("user.neko-craft.com")).styled(style -> style
                        .withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("https://user.neko-craft.com")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://user.neko-craft.com"))))
                .append("\n§7  服务器地址 & 官网: ")
                .append(MutableText.of(new LiteralTextContent("neko-craft.com")).styled(style -> style
                        .withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("https://neko-craft.com")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://neko-craft.com"))))
                .append(MutableText.of(new LiteralTextContent("\n  由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!"))
                        .styled(style -> style.withColor(Formatting.YELLOW)))
                .append("\n  §7新 Fabric 服务端仍处于测试阶段, 如遇任何问题请加群反馈")
                .append("\n§7§m                                                                  §r\n");
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules())
            joinMessage = joinMessage
                    .append("  §7欢迎您来到 NekoCraft !\n  §e您需要点击 ")
                    .append(MutableText.of(new LiteralTextContent("[这里]")).styled(style -> style
                            .withColor(Formatting.AQUA)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("https://user.neko-craft.com/#/about")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://user.neko-craft.com/#/about"))))
                    .append("  §e来阅读服务器规定\n  §7点击确认后则默认您已阅读并遵守服务器规定!\n\n    ")
                    .append(MutableText.of(new LiteralTextContent("[这里]")).styled(style -> style
                            .withColor(Formatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/acceptrule")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptrule"))))
                    .append("§7 或使用指令 /acceptrule\n\n    ")
                    .append(MutableText.of(new LiteralTextContent("[这里]")).styled(style -> style
                            .withColor(Formatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/denyrule")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyrule"))))
                    .append("§7 或使用指令 /denyrule\n");
        player.sendMessage(joinMessage);
    }
}
