package net.nekocraft

import net.fabricmc.api.ClientModInitializer
import net.nekocraft.NekoEssentials.Companion.logger

object NekoEssentialsClient : ClientModInitializer {
    override fun onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        logger.info("NekoEssentials is running on client!")
    }
}