package me.dasetwas;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/**
 * 
 * @author DasEtwas
 *
 */
public class EventListener implements Listener {

	public static int creativePlaceCooldown = 0;

	public EventListener() {

	}

	public EventListener(Chars4Cars plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void destroyCar(VehicleDestroyEvent vee) {
		if (Cars.isCar(vee.getVehicle().getUniqueId())) {

			boolean ownerSurvival = (Bukkit.getPlayer(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner()).getGameMode() == GameMode.SURVIVAL);
			boolean ownerCreativeButHasPermission = (Bukkit.getPlayer((Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner())).getGameMode() == GameMode.CREATIVE) && (Bukkit.getPlayer(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner()).hasPermission("c4c.dropoverride"));

			if (ownerSurvival || ownerCreativeButHasPermission) {
				Car theCar = Cars.CarMap.get(vee.getVehicle().getUniqueId());

				int engineStrength = theCar.enginePower;
				int carMass = theCar.mass;
				double fuel = theCar.fuel;
				String name = theCar.name;

				World world = vee.getVehicle().getLocation().getWorld();

				ItemStack carItem = CarGetter.createCar(name, engineStrength, carMass, fuel);

				world.dropItem(vee.getVehicle().getLocation(), carItem);

				theCar.remove();

				Cars.CarMap.remove(vee.getVehicle().getUniqueId());

				vee.setCancelled(true);
			}
		} else {
			if (vee.getVehicle().getCustomName().substring(0, 16).equals("§aChars4Cars Car")) {
				vee.setCancelled(true);
				try {
					String[] args = vee.getVehicle().getCustomName().split(":");
					vee.getVehicle().getWorld().dropItem(vee.getVehicle().getLocation(), CarGetter.createCar(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Double.parseDouble(args[4])));
					vee.getVehicle().remove();
				} catch (Exception e) {
				}
			}
		}
	}

	@EventHandler
	public void emptyCar(PlayerQuitEvent ple) {
		if (ple.getPlayer().isInsideVehicle() && Cars.isCar(ple.getPlayer().getVehicle().getUniqueId())) {
			ple.getPlayer().leaveVehicle();
		}
	}

	@EventHandler
	public void yay(PlayerJoinEvent pje) {
		if (pje.getPlayer().getUniqueId().equals(UUID.fromString("057505e1-f1b7-444b-ac8c-0ecb5b166b5d"))) {
			Bukkit.getServer().broadcastMessage(Chars4Cars.prefix + ChatColor.GOLD + "=============================");
			Bukkit.getServer().broadcastMessage(Chars4Cars.prefix + ChatColor.GOLD + "} " + ChatColor.WHITE + "DasEtwas" + ChatColor.GOLD + " joined the server! :D {");
			Bukkit.getServer().broadcastMessage(Chars4Cars.prefix + ChatColor.GOLD + "=============================");
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) {
					p.getWorld().dropItemNaturally(p.getLocation(), CarGetter.createCar("Chars4Cars! :D", 329, 3100, 100));
				}
			}
			pje.getPlayer().setGameMode(GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void use(PlayerInteractEvent pie) {

		Player user = pie.getPlayer();

		if (pie.getAction().equals(Action.RIGHT_CLICK_BLOCK) && pie.getPlayer().isInsideVehicle() && Chars4Cars.economyPresent) {
			if (Cars.isCar(user.getVehicle().getUniqueId())) {
				if (pie.getClickedBlock().getType().equals(Material.SIGN) || pie.getClickedBlock().getType().equals(Material.SIGN_POST) || pie.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
					Sign sign = (Sign) pie.getClickedBlock().getState();
					if (sign.getLine(0).equals("[Fuel Station]")) {
						try {
							float quantity = Float.parseFloat(sign.getLine(1));
							float price = Float.parseFloat(sign.getLine(2));
							float oneprice = price / quantity;
							Player owner = Bukkit.getServer().getPlayer(sign.getLine(3));

							if (Cars.CarMap.get(pie.getPlayer().getVehicle().getUniqueId()).fuel + quantity > Chars4Cars.maxFuel) {
								double d = Chars4Cars.maxFuel - Cars.CarMap.get(pie.getPlayer().getVehicle().getUniqueId()).fuel;
								Cars.CarMap.get(pie.getPlayer().getVehicle().getUniqueId()).fuel = Chars4Cars.maxFuel;
								price = (float) (d * oneprice);
								quantity = (float) d;
							} else {
								Cars.CarMap.get(pie.getPlayer().getVehicle().getUniqueId()).fuel += quantity;
							}
							
							String msg = Chars4Cars.boughtFuel;
							msg = msg.replace("%LT%", Float.toString(quantity));
							msg = msg.replace("%LP%", String.valueOf(oneprice));
							msg = msg.replace("%CS%", Chars4Cars.economy.currencyNameSingular());

							Chars4Cars.economy.withdrawPlayer(pie.getPlayer(), price);
							if (owner != null) {
								Chars4Cars.economy.depositPlayer(owner, price);
							}

							pie.getPlayer().sendMessage(Chars4Cars.prefix + msg);
						} catch (Exception e) {
							pie.getPlayer().sendMessage(Chars4Cars.prefix + Chars4Cars.invalidFuelStation);
						}
					}
				}
			}
		}

		// Check if the Player's Hand's Item is a Lever and if the Player is
		// inside of a vehicle
		if (user.getInventory().getItemInMainHand().getType() == Material.LEVER && user.isInsideVehicle()) {
			// Check if the Player clicks left
			if (Cars.isCar(user.getVehicle().getUniqueId())) {
				if (pie.getAction().equals(Action.LEFT_CLICK_AIR) || pie.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
					Cars.CarMap.get(user.getVehicle().getUniqueId()).shiftDown();
					pie.setCancelled(true);
				}

				if (pie.getAction().equals(Action.RIGHT_CLICK_AIR) || pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					Cars.CarMap.get(user.getVehicle().getUniqueId()).shiftUp();
					pie.setCancelled(true);
				}
			}
		}

		if ((user.getInventory().getItemInMainHand().getType()) == Material.MINECART) {
			// get Item in hand to later test if it is a minecart with lores
			ItemStack car = user.getInventory().getItemInMainHand();

			if (car.hasItemMeta()) {
				if (car.getItemMeta().hasLore()) {
					// Avoid double placement

					if (creativePlaceCooldown == 1) {
						creativePlaceCooldown = 0;
					} else {
						creativePlaceCooldown = 1;
					}
					// Avoid placement in blacklisted worldsc
					if (Chars4Cars.limitToWorlds && !Chars4Cars.activeWorlds.contains(pie.getPlayer().getLocation().getWorld().getName())) {
						return;
					}

					if (car.getItemMeta().getLore().size() == 4 && (pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && !(user.getGameMode() == GameMode.CREATIVE && creativePlaceCooldown == 0) && car.getItemMeta().getLore().get(0).equalsIgnoreCase(ChatColor.DARK_GRAY + "Chars4Cars Car")) {
						if (!Cars.isRail(pie.getClickedBlock().getType())) {
							List<String> lore = car.getItemMeta().getLore();
							// Use the lore to create a Car object.
							Car newCar = new Car(user.getLocation().getYaw(), pie.getClickedBlock().getLocation(), user.getUniqueId(), Integer.parseInt(ChatColor.stripColor(lore.get(1))), Integer.parseInt(ChatColor.stripColor(lore.get(2))), car.getItemMeta().getDisplayName(), Double.parseDouble(ChatColor.stripColor(lore.get(3))));

							if (!(user.getGameMode() == GameMode.CREATIVE)) {
								user.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
							}

							// get UUID of car entity to save it in the global
							// HashMap
							// of cars
							UUID uuid = newCar.getCockpitID();
							Cars.CarMap.put(uuid, newCar);
						} else {
							pie.getPlayer().sendMessage(Chars4Cars.prefix + Chars4Cars.noPlaceRails);
							pie.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void anvilCraft(InventoryClickEvent ice) {
		if (ice.getWhoClicked() instanceof Player) {
			if (ice.getView().getType() == InventoryType.ANVIL) {
				AnvilInventory anvil = (AnvilInventory) ice.getInventory();
				// Check if first item is a car
				if (Cars.isCar(anvil.getContents()[0])) {
					List<String> lore = anvil.getContents()[0].getItemMeta().getLore();
					String name = anvil.getContents()[0].getItemMeta().getDisplayName();
					int power = Integer.parseInt(ChatColor.stripColor(lore.get(1)));
					int mass = Integer.parseInt(ChatColor.stripColor(lore.get(2)));
					double fuel = Double.parseDouble(ChatColor.stripColor(lore.get(3)));

					// Check if null to avoid NPE
					if (anvil.getContents()[0] != null && anvil.getContents()[1] != null) {
						if (anvil.getContents()[1].getType().equals(Material.DIODE) && anvil.getContents()[1].getAmount() >= 16) {
							if (anvil.getContents()[1].getAmount() == 16) {
								anvil.setItem(1, new ItemStack(Material.AIR, 0));
							} else {
								anvil.getContents()[1].setAmount(anvil.getContents()[1].getAmount() - 16);
							}
							anvil.setItem(0, CarGetter.createCar(name, power + 5, mass + 15, fuel));
						}

						if (anvil.getContents()[1].getType().equals(Material.IRON_PICKAXE)) {
							anvil.setItem(1, new ItemStack(Material.AIR, 0));
							anvil.setItem(0, CarGetter.createCar(name, power - 1, mass - 30, fuel));
						}

						if (anvil.getContents()[1].getType().equals(Material.REDSTONE_COMPARATOR) && anvil.getContents()[1].getAmount() >= 8) {
							if (anvil.getContents()[1].getAmount() == 8) {
								anvil.setItem(1, new ItemStack(Material.AIR, 0));
							} else {
								anvil.getContents()[1].setAmount(anvil.getContents()[1].getAmount() - 8);
							}
							anvil.setItem(0, CarGetter.createCar(name, power + 10, mass + 30, fuel));
						}

						if (anvil.getContents()[1].getType().equals(Material.COAL_BLOCK)) {
							if (anvil.getContents()[1].getAmount() == 1) {
								anvil.setItem(1, new ItemStack(Material.AIR, 0));
							} else {
								anvil.getContents()[1].setAmount(anvil.getContents()[1].getAmount() - 1);
							}
							anvil.setItem(0, CarGetter.createCar(name, power, mass, fuel + 4));
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void lockCar(PlayerInteractEntityEvent piee) {
		if (Cars.isCar(piee.getRightClicked().getUniqueId())) {
			if (piee.getPlayer().getUniqueId().equals(Cars.CarMap.get(piee.getRightClicked().getUniqueId()).getOwner())) {
				if (Chars4Cars.carLocking) {
					if (piee.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TRIPWIRE_HOOK)) {
						Cars.CarMap.get(piee.getRightClicked().getUniqueId()).lock();
						piee.getPlayer().sendMessage(Chars4Cars.prefix + Chars4Cars.carLocked);
						piee.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void carLeave(VehicleExitEvent vee) {
		if (Cars.isCar(vee.getVehicle().getUniqueId())) {
			((Player) vee.getExited()).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			if (((Player) vee.getExited()).getUniqueId().equals(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner())) {
				if (Chars4Cars.carLocking) {
					Cars.CarMap.get(vee.getVehicle().getUniqueId()).unLock();
					Bukkit.getPlayer(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner()).sendMessage(Chars4Cars.prefix + Chars4Cars.yourCarUnlocked);
					((Player) vee.getExited()).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				}
			}
		}
	}

	/**
	 * Don't let your dreams be dreams! um.. Don't let entities displace the
	 * car.
	 * 
	 * @param vece
	 */

	@EventHandler
	public void carCollision(VehicleEntityCollisionEvent vece) {
		try {
			if (vece.getVehicle().getCustomName().substring(0, 16).equals("§aChars4Cars Car")) {
				String[] args = vece.getVehicle().getCustomName().split(":");
				if (!UUID.fromString(args[4]).equals(vece.getEntity().getUniqueId())) {
					vece.setCollisionCancelled(true);
				}
			}
		} catch (Exception e) {
		}
		if (Cars.isCar(vece.getVehicle().getUniqueId())) {
			if (!Cars.CarMap.get(vece.getVehicle().getUniqueId()).owner.equals(vece.getEntity().getUniqueId())) {
				vece.setCollisionCancelled(true);
			}
		}
	}

	/**
	 * Checks if a player wants to enter a car, and if car locking is enabled,
	 * checks for the locking state of the car.
	 * 
	 * @param vee
	 *            VehicleEnterEvent
	 */
	@EventHandler
	public void carEnter(VehicleEnterEvent vee) {
		if (Cars.isCar(vee.getVehicle().getUniqueId())) {

			if (vee.getEntered() instanceof Player) {

				if (Chars4Cars.carLocking) {
					if (!((Player) vee.getEntered()).getUniqueId().equals(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner())) {

						if (Cars.CarMap.get(vee.getVehicle().getUniqueId()).isLocked()) {
							((Player) vee.getEntered()).sendMessage(Chars4Cars.prefix + Chars4Cars.noEnterCarLocked);
							vee.setCancelled(true);
						} else {
							((Player) vee.getEntered()).sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
							Bukkit.getPlayer(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner()).sendMessage(Chars4Cars.prefix + Chars4Cars.yourCarStolen);
						}
					}
				} else {
					if (!((Player) vee.getEntered()).getUniqueId().equals(Cars.CarMap.get(vee.getVehicle().getUniqueId()).getOwner())) {
						((Player) vee.getEntered()).sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
						vee.setCancelled(true);
					}
				}
			}
		} else {
			if (vee.getVehicle().getCustomName().substring(0, 16).equals("§aChars4Cars Car") && vee.getVehicle().getType().equals(EntityType.MINECART)) {
				try {
					String[] args = vee.getVehicle().getCustomName().split(":");
					Car theCar = new Car(UUID.fromString(args[4]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[1], (Minecart) vee.getVehicle(), Double.parseDouble(args[5]));
					Cars.CarMap.put(vee.getVehicle().getUniqueId(), theCar);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void carDamaged(VehicleDamageEvent vde) {
		try {
			// Check if the Vehicle is a part of a car
			if (Cars.isCar(vde.getVehicle().getUniqueId())) {
				// Check if the damager is a player
				if (vde.getAttacker().getType().equals(EntityType.PLAYER)) {
					Player damager = (Player) vde.getAttacker();

					if (!(damager.getUniqueId().equals(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()))) {

						// Check if the damager is the Owner, which would let
						// him
						// destroy his car
						if (!damager.hasPermission("c4c.owneroverride")) {
							vde.setCancelled(true);
							damager.sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
							damager.sendMessage(Chars4Cars.prefix + Chars4Cars.owner + Bukkit.getPlayer(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()).getDisplayName());
						} else {
							damager.sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
							damager.sendMessage(Chars4Cars.prefix + Chars4Cars.owner + Bukkit.getPlayer(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()).getDisplayName());
							if (!(damager.getUniqueId().equals(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()))) {
								Bukkit.getPlayer(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()).sendMessage(Chars4Cars.prefix + Chars4Cars.yourCarDamaged);
							}
						}
					}
				}
			} else {
				if (vde.getVehicle().getCustomName().substring(0, 16).equals("§aChars4Cars Car")) {
					try {
						String[] args = vde.getVehicle().getCustomName().split(":");

						if (vde.getAttacker().getType().equals(EntityType.PLAYER)) {
							Player damager = (Player) vde.getAttacker();

							if (!damager.getUniqueId().equals(UUID.fromString(args[4]))) {
								if (!damager.hasPermission("c4c.owneroverride")) {
									vde.setCancelled(true);
									damager.sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
									damager.sendMessage(Chars4Cars.prefix + Chars4Cars.owner + Bukkit.getServer().getPlayer(UUID.fromString(args[4])).getName());
								} else {
									damager.sendMessage(Chars4Cars.prefix + Chars4Cars.doNotOwnCar);
									damager.sendMessage(Chars4Cars.prefix + Chars4Cars.owner + Bukkit.getServer().getPlayer(UUID.fromString(args[4])).getName());
									Bukkit.getServer().getPlayer(UUID.fromString(args[4])).sendMessage(Chars4Cars.prefix + Chars4Cars.yourCarDamaged);
								}
							}
						}
					} catch (Exception e) {
					}
				}
			}

		} catch (Exception e) {

		}
	}
}
