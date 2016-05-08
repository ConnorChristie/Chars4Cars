package me.dasetwas;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

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
				String name = theCar.name;

				World world = vee.getVehicle().getLocation().getWorld();

				ItemStack carItem = CarGetter.createCar(name, engineStrength, carMass);

				world.dropItem(vee.getVehicle().getLocation(), carItem);

				theCar.remove();

				Cars.CarMap.remove(vee.getVehicle().getUniqueId());

				vee.setCancelled(true);
			} else {
			}

		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void use(PlayerInteractEvent pie) {

		Player user = pie.getPlayer();
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
			if (creativePlaceCooldown == 1) {
				creativePlaceCooldown = 0;
			} else {
				creativePlaceCooldown = 1;
			}
			// get Item in hand to later test if it is a minecart with lores
			ItemStack car = user.getInventory().getItemInMainHand();

			if (car.getItemMeta().getLore().size() == 2 && (pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && !(user.getGameMode() == GameMode.CREATIVE && creativePlaceCooldown == 0)) {
				List<String> lore = car.getItemMeta().getLore();
				// Use the lore to create a Car object.
				Car newCar = new Car(user.getLocation().getYaw(), pie.getClickedBlock().getLocation(), user.getUniqueId(), Integer.parseInt(lore.get(0)), Integer.parseInt(lore.get(1)), car.getItemMeta().getDisplayName());

				if (!(user.getGameMode() == GameMode.CREATIVE)) {
					user.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
				}

				// get UUID of car entity to save it in the global HashMap
				// of cars
				UUID uuid = newCar.getCockpitID();
				Cars.CarMap.put(uuid, newCar);
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
	 * Don't let you dreams be dreams! um.. Don't let entities displace the car.
	 * 
	 * @param vece
	 */

	@EventHandler
	public void carCollision(VehicleEntityCollisionEvent vece) {
		if (Cars.isCar(vece.getVehicle().getUniqueId())) {
			vece.setCollisionCancelled(true);
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
		}
	}

	@EventHandler
	public void carDamaged(VehicleDamageEvent vde) {
		// Check if the Vehicle is a part of a car
		if (Cars.isCar(vde.getVehicle().getUniqueId())) {
			// Check if the damager is a player

			if (vde.getAttacker().getType().equals(EntityType.PLAYER)) {
				Player damager = (Player) vde.getAttacker();

				if (!damager.hasPermission("c4c.owneroverride")) {
					// Check if the damager is the Owner, which would let him
					// destroy his car

					if (!(damager.getUniqueId().equals(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()))) {
						vde.setCancelled(true);
						damager.sendMessage(Chars4Cars.doNotOwnCar);
						damager.sendMessage(Chars4Cars.owner + Bukkit.getPlayer(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()).getDisplayName());
					}
				} else {
					if (!(damager.getUniqueId().equals(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()))) {
						Bukkit.getPlayer(Cars.CarMap.get(vde.getVehicle().getUniqueId()).getOwner()).sendMessage(Chars4Cars.yourCarDestroyed);
					}
				}
			}
		}
	}
}
