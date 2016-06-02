package me.dasetwas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.milkbowl.vault.economy.Economy;

/**
 * 
 * @author DasEtwas
 *
 */
public class Chars4Cars extends JavaPlugin {

	public static double differentialRatio = 0;
	public static char altColorChar = '&';
	public static String doNotOwnCar;
	public static String doNotOwnCarLock;
	public static String noPerm;
	public static String noEnterCarLocked;
	public static String owner;
	public static String givenCar;
	public static String carLocked;
	public static boolean carLocking;
	public static String yourCarStolen;
	public static String notOnline;
	public static String noFuelBuySelf;
	public static String prefix = ChatColor.translateAlternateColorCodes(altColorChar, "&7[&aC&84&aC&7]&r ");
	public static int ppp = 0;
	public static String yourCarDamaged;
	public static boolean fuel;
	public static Plugin plugin;
	public static String commandSuccess;
	public static String yourCarUnlocked;

	public static int updateDelta;
	public static boolean exhaustSmoke;
	public static boolean limitToWorlds;
	public static List<String> activeWorlds = new ArrayList<String>();
	public static int defaultPower;
	public static int defaultMass;
	public static int speedLimit;
	public static double maxFuel;
	public static String noPlaceRails;
	public static String couldNotConvert;
	public static ProtocolManager protocolManager;
	public static float volume;
	public static boolean climbBlocks;
	public static List<String> climbBlocksList;
	public static float climbBlockSearchFactor;
	public static boolean economyPresent;
	public static Economy economy;
	public static double defaultFuel;
	public static String invalidFuelStation;
	public static String boughtFuel;
	public static boolean scoreBoard;
	public static int MCVersion;

	public static double slabJumpVel = 0.3;
	public static double stairJumpVel = 0.45;

	@Override
	public void onEnable() {
		try {
			MCVersion = Integer.parseInt(Bukkit.getVersion().split(" ")[2].replace(")", "").replace(".", "").substring(0, 2));
		} catch (Exception e) {
			MCVersion = 0;
		}
		Compat.setup();

		Bukkit.getLogger().info("Found minecraft version: " + MCVersion);

		if (!setupProtocolLib()) {
			getLogger().warning("DID NOT FIND ProtocolLib. DISABLING PLUGIN");
			getLogger().warning("DID NOT FIND ProtocolLib. DISABLING PLUGIN");
			getLogger().warning("DID NOT FIND ProtocolLib. DISABLING PLUGIN");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}

		economyPresent = setupEconomy();
		if (!economyPresent) {
			getLogger().warning("Can't find Vault. Money related tasks disabled.");
		} else {
			getLogger().info("Hooked into Vault.");
		}

		getCommand("c4c").setTabCompleter(new TabFiller());
		getCommand("chars4cars").setTabCompleter(new TabFiller());
		getCommand("killcars").setTabCompleter(new TabFiller());
		loadConfig();

		plugin = this;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				Cars.cycleCars();
				if (ppp == 1) {
					ppp = 0;
				} else {
					ppp = 1;
				}
			}
		}, 0, updateDelta);

		new EventListener(this);

		protocolManager.addPacketListener(new PacketAdapter(Chars4Cars.plugin, PacketType.Play.Client.STEER_VEHICLE) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType().equals(PacketType.Play.Client.STEER_VEHICLE)) {
					PacketContainer packet = event.getPacket();
					float side = packet.getFloat().read(0);
					float forw = packet.getFloat().read(1);
					if (event.getPlayer().isInsideVehicle()) {
						if (Cars.isCar(event.getPlayer().getVehicle().getUniqueId())) {
							Cars.CarMap.get(event.getPlayer().getVehicle().getUniqueId()).setSide(side / 0.98f);
							Cars.CarMap.get(event.getPlayer().getVehicle().getUniqueId()).setForw(forw / 0.98f);
						}
					}
				}
			}
		});

		/*
		 * Crafting of car
		 */

		ShapedRecipe carRecipe = new ShapedRecipe(CarGetter.createCar("Car", defaultPower, defaultMass, defaultFuel));

		carRecipe.shape(" LG", "IPI", "FMI");
		carRecipe.setIngredient('L', Material.LEATHER); // Leather
		carRecipe.setIngredient('G', Material.GLASS); // Glass
		carRecipe.setIngredient('I', Material.IRON_INGOT); // Iron ingot
		carRecipe.setIngredient('P', Material.STONE_PLATE); // Pressure plate
		carRecipe.setIngredient('F', Material.FURNACE); // Furnace
		carRecipe.setIngredient('M', Material.MINECART); // Minecart

		Bukkit.getServer().addRecipe(carRecipe);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll();
	}

	// Message that indicates that the plugin is working.
	String funcMSG = "- Working -";

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((label.equalsIgnoreCase("c4c") || label.equalsIgnoreCase("chars4cars"))) {

			/*
			 * 
			 * \ /c4c
			 * 
			 */
			if (args.length == 0) {
				if (sender.hasPermission("c4c.info")) {
					sender.sendMessage(prefix + funcMSG);
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&a===< &8Chars4Cars command information &a>==="));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/givecar &a<&7name&a>&7 &a<&7power&a>&7 &a<&7mass&a>&7 &a<&7fuel&a>&7 &e[&7player&e]"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8Gives you or &e[&7player&e]&8 a car with the given properties."));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/c4c &a<&7info&a>&7 | &a<&7reload&a>&7 | &a<&7perms&a>"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8Reloads the plugin, shows info or shows permission nodes."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8When no argument is given, this info will show."));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/killcars &a<&7all&a>&7 | &a<&7empty&a>&7 | &a<&7soft&a>&7 | &a<&7drop&a>"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8Kills all cars:"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8all     - All cars get deleted completely."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8empty - All empty cars get deleted completely."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8drop  - All empty cars get dropped."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8soft   - All car entities get removed, but:"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8        - If the owner is online, he gets his car."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8        - If not, the pasenger gets the car."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8        - If no passenger is present, the car gets dropped."));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/getitemname"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&8Displays the name of the item in your hand, which can be used in the config."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&a<> &8= &aNecessary&8,&e [] &8= &eOptional, &r| &8= &for"));
				} else {
					sender.sendMessage(noPerm);
				}
			} else if (args.length > 0) {

				if (args[0].equalsIgnoreCase("reload") && args.length == 1) {
					if (sender.hasPermission("c4c.reload")) {
						reloadConfig();
						loadConfig();
						sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, prefix + "&8Config reloaded."));
					} else {
						sender.sendMessage(noPerm);
					}
				} else if (args[0].equalsIgnoreCase("info") && args.length == 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar,"&aChars&74&aCars &8- (&7Characters for Cars&8)"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar,"       &8Made by &fDasEtwas          "));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar,"          Version " + this.getDescription().getVersion()));

				} else if (args[0].equalsIgnoreCase("perms") && args.length == 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&a===< Chars4Cars command permissions >==="));
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/givecar &8- &ac4c.givecar"));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/c4c &8- &ac4c.info"));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/c4c reload &8- &ac4c.reload"));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/c4c perms &8- &ac4c.perms"));
					sender.sendMessage("");
					sender.sendMessage(ChatColor.translateAlternateColorCodes(altColorChar, "&7/givecar &8- &ac4c.givecar"));
					sender.sendMessage("");

				} else {
					sender.sendMessage(prefix + "Usage: /c4c <info> | <reload> | <perms>");
				}

			} else {
				sender.sendMessage(noPerm);
			}
		}

		/*
		 * \ /givecar, /killcars
		 * 
		 */

		if ((label.equalsIgnoreCase("givecar"))) {
			if (sender.hasPermission("c4c.givecar")) {
				try {
					if (args.length == 4) {
						if (!(sender instanceof Player)) {
							sender.sendMessage(prefix + "The argument [Player] has to be used in the console.");
						} else {
							try {
								CarGetter.getCar(((Player) sender), args[0].replace("_", " "), (int) Double.parseDouble(args[1]), (int) Double.parseDouble(args[2]), Double.parseDouble(args[3]));
								sender.sendMessage(prefix + commandSuccess);
							} catch (NumberFormatException nfe) {
								sender.sendMessage(prefix + "Usage: /givecar <name> <power> <mass> <fuel> [player]");
							}
						}

					} else if (args.length == 5) {
						if (Bukkit.getPlayer(args[4]) != null) {
							try {
								CarGetter.getCar(Bukkit.getPlayer(args[4]), args[0], (int) Double.parseDouble(args[1]), (int) Double.parseDouble(args[2]), Double.parseDouble(args[3]));
								sender.sendMessage(prefix + commandSuccess);
								if (!sender.getName().equals(args[4])) {
									Bukkit.getPlayer(args[4]).sendMessage(prefix + givenCar);
								}
							} catch (NumberFormatException nfe) {
								sender.sendMessage(prefix + "Usage: /givecar <name> <power> <mass> <fuel> [player]");
							}
						} else {
							sender.sendMessage(prefix + notOnline);
						}
					} else {
						sender.sendMessage(prefix + "Usage: /givecar <name> <power> <mass> <fuel> [player]");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage(noPerm);
			}

		}

		if (label.equalsIgnoreCase("killcars")) {
			if (sender.hasPermission("c4c.killcars")) {
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("all") && args.length == 1) {
						Cars.removeAllCars();
						sender.sendMessage(prefix + commandSuccess);
					} else if (args[0].equalsIgnoreCase("empty") && args.length == 1) {
						Cars.removeEmptyCars();
						sender.sendMessage(prefix + commandSuccess);
					} else if (args[0].equalsIgnoreCase("drop") && args.length == 1) {
						Cars.removeSoftCars(true);
						sender.sendMessage(prefix + commandSuccess);
					} else if (args[0].equalsIgnoreCase("soft") && args.length == 1) {
						Cars.removeSoftCars(false);
						sender.sendMessage(prefix + commandSuccess);
					} else {
						sender.sendMessage(prefix + "Usage: /killcars <all> | <empty> | <drop> | <soft>");
					}
				} else {
					sender.sendMessage(prefix + "Usage: /killcars <all> | <empty> | <drop> | <soft>");
				}
			} else {
				sender.sendMessage(noPerm);
			}
		}
		if (label.equalsIgnoreCase("getitemname")) {
			if (sender instanceof Player) {
				sender.sendMessage(Compat.getItemInMainHand(((Player) sender).getInventory()).getType().toString());
			} else {
				sender.sendMessage(prefix + "That command is only for players!");
			}

		}

		return true;
	}

	/**
	 * Loads configuration
	 * 
	 * @SuppressWarnings ("unchecked") because of cast from List<?> to List
	 *                   <String>
	 */
	@SuppressWarnings("unchecked")
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		getConfig().options().header("Chars4Cars config");

		// Perm
		// Warn
		// Info
		// Game

		getConfig().addDefault("perm.doNotOwnCar", "&4You do not own that car.");
		getConfig().addDefault("perm.noPerm", "&cYou have no permission to do that.");
		getConfig().addDefault("warn.notOnline", "&8That player is not online.");
		getConfig().addDefault("warn.doNotOwnCarLock", "&4You can't lock that car.");
		getConfig().addDefault("warn.noEnterCarLocked", "&8That car is locked.");
		getConfig().addDefault("warn.noPlaceRails", "&8You can't place a car on rails.");
		getConfig().addDefault("warn.invalidFuelStation", "&7That's not a valid Fuel Station.");
		getConfig().addDefault("warn.noFuelBuySelf", "&8You can't buy fuel from yourself!");
		getConfig().addDefault("info.owner", "&8Owner: &r");
		getConfig().addDefault("info.givenCar", "&8You have been given a car.");
		getConfig().addDefault("info.carLocked", "&8Your car is now locked.");
		getConfig().addDefault("info.yourCarDamaged", "&cYour car got damaged.");
		getConfig().addDefault("info.yourCarStolen", "&k!!! &4Your car got Stolen! &r&k!!!");
		getConfig().addDefault("info.yourCarUnlocked", "&8Your car is now unlocked.");
		getConfig().addDefault("info.commandSuccess", "&8Command executed successfully.");
		getConfig().addDefault("info.boughtFuel", "&8You bought &f%LT%&8L of fuel for &f%LP%&7%CS%&8 per Liter.");

		getConfig().addDefault("game.limitToWorlds.enable", false);
		List<String> wl = new ArrayList<String>();
		wl.add("world");
		wl.add("example");
		getConfig().addDefault("game.limitToWorlds.worlds", wl);
		List<String> cb = new ArrayList<String>();
		cb.add("CAPITAL_LETTERS_ITEM_NAME");
		cb.add("(/getitemname)");
		getConfig().addDefault("game.climbBlocks.enable", false);
		getConfig().addDefault("game.climbBlocks.climb", cb);
		getConfig().addDefault("game.climbBlocks.searchFactor", 2.3);
		getConfig().addDefault("game.carLocking", true);
		getConfig().addDefault("game.exhaustSmoke", true);
		getConfig().addDefault("game.volume", 100);
		getConfig().addDefault("game.defaultPower", 80);
		getConfig().addDefault("game.defaultMass", 1300);
		getConfig().addDefault("game.defaultFuel", 40);
		getConfig().addDefault("game.updateDelta", 1);
		getConfig().addDefault("game.speedLimit", 100);
		getConfig().addDefault("game.differentialRatio", 10);
		getConfig().addDefault("game.fuel", true);
		getConfig().addDefault("game.maxFuel", 80);
		getConfig().addDefault("game.scoreBoard", true);
		saveConfig();

		// Perm
		// Warn
		// Info
		// Game

		doNotOwnCar = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("perm.doNotOwnCar"));
		noPerm = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("perm.noPerm"));
		notOnline = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.notOnline"));
		doNotOwnCarLock = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.doNotOwnCarLock"));
		noEnterCarLocked = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.noEnterCarLocked"));
		noPlaceRails = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.noPlaceRails"));
		invalidFuelStation = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.invalidFuelStation"));
		noFuelBuySelf = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("warn.noFuelBuySelf"));
		owner = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.owner"));
		givenCar = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.givenCar"));
		carLocked = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.carLocked"));
		yourCarDamaged = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.yourCarDamaged"));
		yourCarStolen = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.yourCarStolen"));
		yourCarUnlocked = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.yourCarUnlocked"));
		commandSuccess = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.commandSuccess"));
		boughtFuel = ChatColor.translateAlternateColorCodes(altColorChar, getConfig().getString("info.boughtFuel"));

		climbBlocks = getConfig().getBoolean("game.climbBlocks.enable");
		climbBlocksList = (List<String>) getConfig().getList("climbBlocks.climb");
		climbBlockSearchFactor = (float) getConfig().getDouble("game.climbBlocks.searchFactor");
		defaultPower = getConfig().getInt("game.defaultPower");
		defaultMass = getConfig().getInt("game.defaultMass");
		defaultFuel = getConfig().getDouble("game.defaultFuel");
		limitToWorlds = getConfig().getBoolean("game.limitToWorlds.enable");
		activeWorlds = (List<String>) getConfig().getList("game.limitToWorlds.worlds");
		carLocking = getConfig().getBoolean("game.carLocking");
		exhaustSmoke = getConfig().getBoolean("game.exhaustSmoke");
		volume = (float) Math.min(1, Math.max(0, (double) getConfig().getInt("game.volume") / 100));
		speedLimit = getConfig().getInt("game.speedLimit");
		differentialRatio = getConfig().getDouble("game.differentialRatio");
		fuel = getConfig().getBoolean("game.fuel");
		maxFuel = getConfig().getDouble("game.maxFuel");
		scoreBoard = getConfig().getBoolean("game.scoreBoard");
		if (speedLimit < 1) {
			speedLimit = 1;
			getConfig().set("game.speedLimit", 1);
		}
		updateDelta = getConfig().getInt("game.updateDelta");
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	private boolean setupProtocolLib() {
		if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
			return false;
		}
		try {
			protocolManager = ProtocolLibrary.getProtocolManager();
		} catch (Exception e) {
		}
		return protocolManager != null;
	}
}
