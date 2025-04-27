package net.nekocraft.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.world.PlayerSaveHandler
import net.nekocraft.NekoEssentials.Companion.logger
import java.util.*
import java.util.function.Predicate

object OpeninvCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("openinv")
                .requires(Predicate { source: ServerCommandSource? -> source.hasPermissionLevel(2) })
                .then(
                    CommandManager.argument<GameProfileArgument?>("player", GameProfileArgumentType.gameProfile())
                        .executes(Command { context: CommandContext<ServerCommandSource?>? ->
                            OpeninvCommand.execute(
                                context.getSource(), context.getSource().getPlayer(),
                                GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                            )
                        })
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, profile: GameProfile): Int {
        val targerPlayer: ServerPlayerEntity? = source.server.playerManager.getPlayer(profile.getId())
        if (targerPlayer != null) {
            return OpeninvCommand.execute(source, player, targerPlayer)
        } else {
            val saveHandler: PlayerSaveHandler =
                (source.server.playerManager as MixinPlayerManagerAccessor).getSaveHandler()
            val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).loadPlayerData(profile)
            if (playerData == null) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()

            val playerEntity: PlayerEntity =
                object : PlayerEntity(source.server.overworld, BlockPos.ORIGIN, 0f, profile) {
                    val isSpectator: Boolean
                        get() = false

                    val isCreative: Boolean
                        get() = false
                }

            val playerInventory = PlayerInventory(playerEntity)
            playerInventory.readNbt(playerData.getList("Inventory", 10))

            val inventory: OpeninvInventory =
                OpeninvOfflineInventory(playerInventory, saveHandler, profile, source.server)
            openinv(player, playerEntity, inventory)
        }

        return 0
    }

    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val inventory: OpeninvInventory = OpeninvOnlineInventory(target.getInventory(), target)
        openinv(player, target, inventory)

        return 0
    }

    private fun openinv(player: ServerPlayerEntity, target: PlayerEntity, inventory: OpeninvInventory) {
        logger.info(String.format("[openinv] %s -> %s", player, target))
        player.openHandledScreen(
            SimpleNamedScreenHandlerFactory(
                ScreenHandlerFactory { syncId: Int, playerInv: PlayerInventory?, playerT: PlayerEntity? ->
                    GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X5,
                        syncId,
                        playerInv,
                        inventory,
                        5
                    )
                },
                Text.of(target.getName().asString() + "'s inventory")
            )
        )
    }
}

internal class OpeninvOfflineInventory(
    playerInv: PlayerInventory,
    private val saveHandler: PlayerSaveHandler,
    profile: GameProfile,
    server: MinecraftServer
) : OpeninvInventory(playerInv) {
    private val profile: GameProfile
    private val server: MinecraftServer

    init {
        this.profile = profile
        this.server = server
    }

    override fun onClose(player: PlayerEntity?) {
        super.onClose(player)
        val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).loadPlayerData(profile)
        if (playerData == null) return
        playerData.put("Inventory", this.playerInventory.writeNbt(NbtList()))
        (saveHandler as IMixinPlayerSaveHandler).savePlayerData(profile, playerData)
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) &&
                !Arrays.asList<String?>(*server.getPlayerManager().getPlayerNames()).contains(profile.getName())
    }
}

internal class OpeninvOnlineInventory(playerInv: PlayerInventory, owner: ServerPlayerEntity) :
    OpeninvInventory(playerInv) {
    private val owner: ServerPlayerEntity

    init {
        this.owner = owner
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) && !owner.isDisconnected()
    }
}

internal open class OpeninvInventory(playerInv: PlayerInventory) : Inventory {
    var playerInventory: PlayerInventory = playerInv

    override fun size(): Int {
        return 45
    }

    val isEmpty: Boolean
        get() = playerInventory.isEmpty()

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