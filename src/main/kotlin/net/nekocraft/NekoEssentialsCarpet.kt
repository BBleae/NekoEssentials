package net.nekocraft

import net.minecraft.text.PlainTextContent.Literal
import carpet.CarpetExtension
import carpet.logging.HUDController
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText

class NekoEssentialsCarpet : CarpetExtension {
    override fun registerLoggers() {
        HUDController.register { server: MinecraftServer? ->
            server?.playerManager?.playerList?.forEach(java.util.function.Consumer { player: ServerPlayerEntity? ->
                HUDController.scarpet_headers.put(
                    player?.name?.string,
                    MutableText.of(Literal("§3§m            §r §a[§6NekoCraft§a] §3§m            \n§7Telegram:§3@NekoCraft  §7QQ:§37923309§r"))
                )
                if (!HUDController.player_huds.containsKey(player)) HUDController.addMessage(
                    player,
                    MutableText.of(Literal("§7使用 /log 监听的信息会显示在这里§r"))
                )
                HUDController.addMessage(player, MutableText.of(Literal("§3§m                                    §r")))
            })
        }
    }
}
