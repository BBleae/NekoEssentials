package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object CommandRegistry {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        ToggleCommand.register(dispatcher)
        SetHomeCommand.register(dispatcher)
        HomeCommand.register(dispatcher)
        TpaCommand.register(dispatcher)
        TpAcceptCommand.register(dispatcher)
        TpaDenyCommand.register(dispatcher)
        HeadCommand.register(dispatcher)
        HandCommand.register(dispatcher)
        WarpCommand.register(dispatcher)
        BackCommand.register(dispatcher)
        AcceptRuleCommand.register(dispatcher)
        DenyruleCommand.register(dispatcher)
        TpaHereCommand.register(dispatcher)
        HatCommand.register(dispatcher)
        OpenInventoryCommand.register(dispatcher)
        OpenEnderCommand.register(dispatcher)
    }
}
