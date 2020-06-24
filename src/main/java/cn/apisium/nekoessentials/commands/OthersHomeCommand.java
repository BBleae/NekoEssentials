package cn.apisium.nekoessentials.commands;

import cn.apisium.nekoessentials.Main;
import cn.apisium.nekoessentials.utils.Serializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("othershome")
public final class OthersHomeCommand extends BasicCommand {
    public OthersHomeCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        final Player p = instance.getPlayer(sender, args[0]);
        if (p == null) return true;
        final byte[] bytes = instance.db.get((p.getUniqueId().toString() + ".home").getBytes());
        if (bytes == null) sender.sendMessage("§c该玩家还没有设置家!");
        else {
            ((Player) sender).teleport(Serializer.deserializeLocation(bytes));
            sender.sendMessage("§a传送成功!");
        }
        return true;
    }
}

