package net.nekocraft.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.EnderChestInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.PlayerSaveHandler
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixin.MixinPlayerManagerAccessor
import net.nekocraft.mixinInterfaces.IMixinPlayerSaveHandler

object OpenEnderCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("openender")
                .requires { source: ServerCommandSource? -> source?.hasPermissionLevel(2) == true }.then(
                    CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes { context: CommandContext<ServerCommandSource?>? ->
                            context?.source?.let { source ->
                                source.player?.let { player ->
                                    execute(
                                        source,
                                        player,
                                        GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                                    )
                                }
                            } ?: -1
                        })
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, profile: GameProfile): Int {
        val targetPlayer: ServerPlayerEntity? = source.server.playerManager.getPlayer(profile.id)
        if (targetPlayer != null) {
            return execute(source, player, targetPlayer)
        } else {
            val saveHandler: PlayerSaveHandler =
                (source.server.playerManager as MixinPlayerManagerAccessor).getSaveHandler()
            val playerData: NbtCompound? =
                (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$loadPlayerData`(profile)
            if (playerData == null) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()

            val enderInventory = EnderChestInventory()
            playerData.getList("EnderItems").ifPresent { enderInventory.readNbtList(it, player.registryManager) }

            val inventory = OpenEnderOfflineInventory(enderInventory, saveHandler, profile, source.server)
            openEnder(player, profile, inventory)
        }

        return 0
    }

    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val inventory = OpenEnderOnlineInventory(target.getEnderChestInventory(), target)
        openEnder(player, target.gameProfile, inventory)
        return 0
    }

    private fun openEnder(player: ServerPlayerEntity, target: GameProfile, inventory: OpenEnderInventory) {
        logger.info(String.format("[openEnder] %s -> %s", player, target))
        player.openHandledScreen(
            SimpleNamedScreenHandlerFactory(
                { syncId: Int, playerInv: PlayerInventory?, playerT: PlayerEntity? ->
                    GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X3, syncId, playerInv, inventory, 3
                    )
                }, Text.of(target.name + "'s ender chest")
            )
        )
    }
}

internal class OpenEnderOfflineInventory(
    playerInv: EnderChestInventory,
    private val saveHandler: PlayerSaveHandler,
    private val profile: GameProfile,
    private val server: MinecraftServer
) : OpenEnderInventory(playerInv) {

    override fun onClose(player: PlayerEntity?) {
        super.onClose(player)
        val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$loadPlayerData`(profile)
        if (playerData == null) return
        player?.let { playerData.put("EnderItems", this.enderInventory.toNbtList(it.registryManager)) }
        (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$savePlayerData`(profile, playerData)
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) && !listOf<String?>(*server.playerManager.getPlayerNames()).contains(profile.name)
    }
}

internal class OpenEnderOnlineInventory(playerInv: EnderChestInventory, private val owner: ServerPlayerEntity) :
    OpenEnderInventory(playerInv) {

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) && !owner.isDisconnected
    }
}

internal open class OpenEnderInventory(playerInv: EnderChestInventory) : Inventory {
    var enderInventory: EnderChestInventory = playerInv

    override fun size(): Int {
        return 27
    }

    override fun isEmpty(): Boolean {
        return enderInventory.isEmpty()
    }

    override fun getStack(slot: Int): ItemStack? {
        return enderInventory.getStack(slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack? {
        return enderInventory.removeStack(slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack? {
        return enderInventory.removeStack(slot)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        enderInventory.setStack(slot, stack)
    }

    override fun markDirty() {
        enderInventory.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

    override fun clear() {
        enderInventory.clear()
    }

    override fun isValid(slot: Int, stack: ItemStack?): Boolean {
        return true
    }
}