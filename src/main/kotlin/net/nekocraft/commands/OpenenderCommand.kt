package net.nekocraft.commands

import com.mojang.brigadier.Command
import net.minecraft.text.Text
import net.minecraft.world.PlayerSaveHandler
import java.util.*
import java.util.function.Predicate

object OpenenderCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("openender")
                .requires(Predicate { source: ServerCommandSource? -> source.hasPermissionLevel(2) })
                .then(
                    CommandManager.argument<GameProfileArgument?>("player", GameProfileArgumentType.gameProfile())
                        .executes(Command { context: CommandContext<ServerCommandSource?>? ->
                            OpenenderCommand.execute(
                                context.getSource(), context.getSource().getPlayer(),
                                GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()
                            )
                        })
                )
        )
    }

    @Throws(CommandSyntaxException::class)
    private fun execute(source: ServerCommandSource, player: ServerPlayerEntity, profile: GameProfile): Int {
        val targerPlayer: ServerPlayerEntity? = source.getServer().getPlayerManager().getPlayer(profile.getId())
        if (targerPlayer != null) {
            return OpenenderCommand.execute(source, player, targerPlayer)
        } else {
            val saveHandler: PlayerSaveHandler =
                (source.getServer().getPlayerManager() as MixinPlayerManagerAccessor).getSaveHandler()
            val playerData: NbtCompound? = (saveHandler as IMixinPlayerSaveHandler).loadPlayerData(profile)
            if (playerData == null) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create()

            val enderInventory = EnderChestInventory()
            enderInventory.readNbtList(playerData.getList("EnderItems", 10))

            val inventory = OpenenderOfflineInventory(enderInventory, saveHandler, profile, source.getServer())
            openender(player, profile, inventory)
        }

        return 0
    }

    private fun execute(source: ServerCommandSource?, player: ServerPlayerEntity, target: ServerPlayerEntity): Int {
        val inventory = OpenenderOnlineInventory(target.getEnderChestInventory(), target)
        openender(player, target.getGameProfile(), inventory)

        return 0
    }

    private fun openender(player: ServerPlayerEntity, target: GameProfile, inventory: OpenenderInventory) {
        logger.info(String.format("[openender] %s -> %s", player, target))
        player.openHandledScreen(
            SimpleNamedScreenHandlerFactory(
                ScreenHandlerFactory { syncId: Int, playerInv: PlayerInventory?, playerT: PlayerEntity? ->
                    GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X3,
                        syncId,
                        playerInv,
                        inventory,
                        3
                    )
                },
                Text.of(target.getName() + "'s ender chest")
            )
        )
    }
}

internal class OpenenderOfflineInventory(
    playerInv: EnderChestInventory,
    private val saveHandler: PlayerSaveHandler,
    profile: GameProfile,
    server: MinecraftServer
) : OpenenderInventory(playerInv) {
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
        playerData.put("EnderItems", this.enderInventory.toNbtList())
        (saveHandler as IMixinPlayerSaveHandler).savePlayerData(profile, playerData)
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) &&
                !Arrays.asList<String?>(*server.getPlayerManager().getPlayerNames()).contains(profile.getName())
    }
}

internal class OpenenderOnlineInventory(playerInv: EnderChestInventory, owner: ServerPlayerEntity) :
    OpenenderInventory(playerInv) {
    private val owner: ServerPlayerEntity

    init {
        this.owner = owner
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return super.canPlayerUse(player) && !owner.isDisconnected()
    }
}

internal open class OpenenderInventory(playerInv: EnderChestInventory) : Inventory {
    var enderInventory: EnderChestInventory = playerInv

    override fun size(): Int {
        return 27
    }

    val isEmpty: Boolean
        get() = enderInventory.isEmpty()

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