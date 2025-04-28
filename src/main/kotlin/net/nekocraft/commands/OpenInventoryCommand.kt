package net.nekocraft.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.entity.EntityEquipment
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode
import net.minecraft.world.PlayerSaveHandler
import net.nekocraft.NekoEssentials.Companion.logger
import net.nekocraft.mixin.MixinPlayerManagerAccessor
import net.nekocraft.mixinInterfaces.IMixinPlayerSaveHandler

object OpenInventoryCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("openinv")
                .requires { source: ServerCommandSource? -> source?.hasPermissionLevel(2) == true }
                .then(
                    CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes { context: CommandContext<ServerCommandSource?>? ->
                            context?.source?.let {
                                context.source?.let { source ->
                                    it.player?.let { player ->
                                        execute(
                                            source, player,
                                            GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                                        )
                                    }
                                }
                            } ?: -1
                        }
                )
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
            val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$loadPlayerData`(profile)
            if (playerData == null) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()

            val playerEntity: PlayerEntity =
                object : PlayerEntity(source.server.overworld, BlockPos.ORIGIN, 0f, profile) {
                    override fun getGameMode(): GameMode? {
                        return GameMode.SURVIVAL
                    }
                }
            val equipment = EntityEquipment()
            val playerInventory = PlayerInventory(playerEntity, equipment)
            playerData.getList("Inventory").ifPresent { playerInventory.readNbt(it) }

            val inventory: OpenableInventory =
                OpenOfflineInventory(playerInventory, saveHandler, profile, source.server)
            openinv(player, playerEntity, inventory)
        }

        return 0
    }

    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val inventory: OpenableInventory = OpenableOnlineInventory(target.getInventory(), target)
        openinv(player, target, inventory)

        return 0
    }

    private fun openinv(player: ServerPlayerEntity, target: PlayerEntity, inventory: OpenableInventory) {
        logger.info(String.format("[openinv] %s -> %s", player, target))
        player.openHandledScreen(
            SimpleNamedScreenHandlerFactory(
                { syncId: Int, playerInv: PlayerInventory?, playerT: PlayerEntity? ->
                    GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X5,
                        syncId,
                        playerInv,
                        inventory,
                        5
                    )
                },
                Text.of(target.name.string + "'s inventory")
            )
        )
    }
}

internal class OpenOfflineInventory(
    playerInv: PlayerInventory,
    private val saveHandler: PlayerSaveHandler,
    private val profile: GameProfile,
    private val server: MinecraftServer
) : OpenableInventory(playerInv) {

    override fun onClose(player: PlayerEntity?) {
        super.onClose(player)
        val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$loadPlayerData`(profile)
        if (playerData == null) return
        playerData.put("Inventory", this.playerInventory.writeNbt(NbtList()))
        (saveHandler as IMixinPlayerSaveHandler).`nekoEssentials$savePlayerData`(profile, playerData)
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) &&
                !listOf<String?>(*server.playerManager.getPlayerNames()).contains(profile.name)
    }
}

internal class OpenableOnlineInventory(playerInv: PlayerInventory, private val owner: ServerPlayerEntity) :
    OpenableInventory(playerInv) {

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) && !owner.isDisconnected
    }
}

internal abstract class OpenableInventory(playerInv: PlayerInventory) : Inventory {
    var playerInventory: PlayerInventory = playerInv

    override fun size(): Int {
        return 45
    }

    override fun isEmpty(): Boolean {
        return playerInventory.isEmpty()
    }

    override fun getStack(slot: Int): ItemStack? {
        var slot = slot
        slot = mapSlot(slot)
        if (slot == -1) return ItemStack(Items.BARRIER)
        return playerInventory.getStack(slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack? {
        var slot = slot
        slot = mapSlot(slot)
        if (slot == -1) return ItemStack.EMPTY
        return playerInventory.removeStack(slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack? {
        var slot = slot
        slot = mapSlot(slot)
        if (slot == -1) return ItemStack.EMPTY
        return playerInventory.removeStack(slot)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        var slot = slot
        slot = mapSlot(slot)
        if (slot == -1) return
        playerInventory.setStack(slot, stack)
    }

    override fun markDirty() {
        playerInventory.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

    override fun clear() {
        playerInventory.clear()
    }

    override fun isValid(slot: Int, stack: ItemStack?): Boolean {
        return playerInventory.isValid(slot, stack) && mapSlot(slot) != -1
    }

    private fun mapSlot(slot: Int): Int {
        if (slot < 5) {
            when (slot) {
                0 -> {
                    return 39
                }

                1 -> {
                    return 38
                }

                2 -> {
                    return 37
                }

                3 -> {
                    return 36
                }

                4 -> {
                    return 40
                }

                else -> {
                    return -1
                }
            }
        } else if (slot < 9) {
            return -1
        } else if (slot < 36) {
            return slot
        } else {
            return slot - 36
        }
    }
}