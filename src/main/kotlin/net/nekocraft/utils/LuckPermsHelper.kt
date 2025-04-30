package net.nekocraft.utils

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.slf4j.LoggerFactory

object LuckPermsHelper {
    private val logger = LoggerFactory.getLogger("NekoEssentials:LuckPerms")

    val api: LuckPerms by lazy {
        logger.debug("Initializing LuckPerms API")
        LuckPermsProvider.get()
    }
}
