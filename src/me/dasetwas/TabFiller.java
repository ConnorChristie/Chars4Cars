package me.dasetwas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TabFiller implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("c4c") || cmd.getName().equalsIgnoreCase("chars4cars")) {
				List<String> list = new ArrayList<String>();
				list.add("info");
				list.add("reload");
				list.add("perms");
				list.add(" ");
				return list;
			}
			if (cmd.getName().equalsIgnoreCase("killcars")) {
				List<String> list = new ArrayList<String>();
				list.add("all");
				list.add("empty");
				list.add("soft");
				list.add("drop");
				return list;
			}
			if (cmd.getName().equalsIgnoreCase("givecar") && args.length == 5) {
				@SuppressWarnings("unchecked")
				List<Player> plist = (List<Player>) Bukkit.getOnlinePlayers();
				List<String> list = new ArrayList<String>();

				int i = 0;
				for (i = 0; i < plist.size(); i++) {
					list.add(plist.get(i).getName());
				}

				return list;
			}
		}
		return null;
	}

}
