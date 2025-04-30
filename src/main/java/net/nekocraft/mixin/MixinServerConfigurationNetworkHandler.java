package net.nekocraft.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(ServerConfigurationNetworkHandler.class)
public class MixinServerConfigurationNetworkHandler {
    /**
     * Redirects the server mod name to "NekoCraft" when sending configurations.
     *
     * @param server The Minecraft server instance.
     * @return The modified server mod name.
     */
    @Redirect(method = "sendConfigurations", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getServerModName()Ljava/lang/String;"))
    public String onSendConfigurations(MinecraftServer server) {
        return "NekoCraft";
    }
}
