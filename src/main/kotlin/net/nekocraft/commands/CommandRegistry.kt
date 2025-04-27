package net.nekocraft.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object CommandRegistry {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        ToggleCommand.register(dispatcher)
        SethomeCommand.register(dispatcher)
        HomeCommand.register(dispatcher)
        TpaCommand.register(dispatcher)
        TpacceptCommand.register(dispatcher)
        TpadenyCommand.register(dispatcher)
        HeadCommand.register(dispatcher)
        HandCommand.register(dispatcher)
        WarpCommand.register(dispatcher)
        BackCommand.register(dispatcher)
        AcceptRuleCommand.register(dispatcher)
        DenyruleCommand.register(dispatcher)
        TpahereCommand.register(dispatcher)
        HatCommand.register(dispatcher)
        OpenInventoryCommand.register(dispatcher)
        OpenenderCommand.register(dispatcher)
    }
}
