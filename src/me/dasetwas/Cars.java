package me.dasetwas;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.dasetwas.event.CarsUpdateTickEvent;
import net.md_5.bungee.api.ChatColor;

/**
 * 
 *
 * @author DasEtwas
 *
 */
public class Cars {
	public static double[] gearRatio = { -5, 0.00, 4.33, 2.67, 1.81, 1.38, 1.10, 0.91 };
	public static char[] gearNames = { 'R', 'N', '1', '2', '3', '4', '5', '6' };
	public static short maxGear = 6;
	public static short minGear = -1;
	public static double[] engineTorques = { 0.35, 0.39, 0.45, 0.56, 0.67, 0.89, 1.00, 0.95, 0.76, 0.65 };
	public static HashMap<Integer, UUID> disposal = new HashMap<Integer, UUID>();
	public static HashMap<UUID, Car> CarMap = new HashMap<UUID, Car>();

	/**
	 * @param uuid
	 *            UUID of entity to check
	 * @return If UUID belongs to a car
	 */
	public static boolean isCar(UUID uuid) {
		return CarMap.containsKey(uuid);
	}

	public static boolean isCar(ItemStack car) {
		if (car == null) {
			return false;
		}
		if (!car.hasItemMeta()) {
			return false;
		}
		if (!car.getItemMeta().hasLore()) {
			return false;
		}
		return (car.getItemMeta().getLore().get(0).equals(ChatColor.DARK_GRAY + "Chars4Cars Car"));
	}

	/**
	 * @return All 8 Gear ratios (R, N, 1, 2, 3, 4, 5, 6) (Enginestrenght /
	 *         gearRatio = force)
	 */
	public static double[] getGearRatios() {
		return gearRatio;
	}

	/**
	 * Cylces through all cars in the HashMap and calls updateCar() on them
	 */
	public static void cycleCars() {
		int i = 0;
		for (UUID key : CarMap.keySet()) {
			CarMap.get(key).updateCar();
			i++;
		}

		Bukkit.getServer().getPluginManager().callEvent(new CarsUpdateTickEvent(i));
	}

	/**
	 * @return Gets Graph of engineTorque at given RPMs (using
	 *         linearInterpolation)
	 */
	public static double[] getEngineTorques() {
		return engineTorques;
	}

	public static char[] getGearNames() {
		return gearNames;
	}

	/**
	 * @param type
	 *            Given material
	 * @return If given material is Climbable
	 */
	public static boolean isClimbable(Material type) {
		if (type.equals(Material.STONE_SLAB2)) {
			return true;
		} else if (type.equals(Material.STEP)) {
			return true;
		} else if (type.equals(Material.WOOD_STEP)) {
			return true;
		} else if (type.equals(Material.ACACIA_STAIRS)) {
			return true;
		} else if (type.equals(Material.BIRCH_WOOD_STAIRS)) {
			return true;
		} else if (type.equals(Material.BRICK_STAIRS)) {
			return true;
		} else if (type.equals(Material.COBBLESTONE_STAIRS)) {
			return true;
		} else if (type.equals(Material.DARK_OAK_STAIRS)) {
			return true;
		} else if (type.equals(Material.JUNGLE_WOOD_STAIRS)) {
			return true;
		} else if (type.equals(Material.NETHER_BRICK_STAIRS)) {
			return true;
		} else if (type.equals(Material.QUARTZ_STAIRS)) {
			return true;
		} else if (type.equals(Material.RED_SANDSTONE_STAIRS)) {
			return true;
		} else if (type.equals(Material.SANDSTONE_STAIRS)) {
			return true;
		} else if (type.equals(Material.SMOOTH_STAIRS)) {
			return true;
		} else if (type.equals(Material.SPRUCE_WOOD_STAIRS)) {
			return true;
		} else if (type.equals(Material.WOOD_STAIRS)) {
			return true;
		} else if (type.equals(Material.STEP)) {
			return true;
		} else if (type.equals(Material.DAYLIGHT_DETECTOR) || type.equals(Material.DAYLIGHT_DETECTOR_INVERTED)) {
			return true;
		}
		if (Material.getMaterial("PURPUR_SLAB") != null) {
			if (type.equals(Material.valueOf("PURPUR_SLAB")) || type.equals(Material.valueOf("PURPUR_STAIRS"))) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRail(Material type) {
		if (type.equals(Material.RAILS))
			return true;
		if (type.equals(Material.ACTIVATOR_RAIL))
			return true;
		if (type.equals(Material.DETECTOR_RAIL)) {
			return true;
		}
		return (type.equals(Material.POWERED_RAIL));
	}

	public static boolean isSlab(Material type) {
		if (type.equals(Material.STONE_SLAB2)) {
			return true;
		} else if (type.equals(Material.WOOD_STEP)) {
			return true;
		} else if (type.equals(Material.STEP)) {
			return true;
		} else if (type.equals(Material.DAYLIGHT_DETECTOR) || type.equals(Material.DAYLIGHT_DETECTOR_INVERTED)) {
			return true;
		}
		if (Material.getMaterial("PURPUR_SLAB") != null) {
			if (type.equals(Material.valueOf("PURPUR_SLAB"))) {
				return true;
			}
		}
		return false;
	}

	public static void removeAllCars() {
		for (UUID key : CarMap.keySet()) {
			CarMap.get(key).remove();
			disposal.put(disposal.size(), key);
		}
		dispose();
	}

	public static void removeEmptyCars() {
		for (UUID key : CarMap.keySet()) {
			if (CarMap.get(key).passenger == null) {
				CarMap.get(key).remove();
				disposal.put(disposal.size(), key);
			}
		}
		dispose();
	}

	public static void removeSoftCars(boolean drop) {
		for (UUID key : CarMap.keySet()) {
			if (drop) {
				Location dropLoc = new Location(CarMap.get(key).car.getWorld(), CarMap.get(key).x, CarMap.get(key).y, CarMap.get(key).z);
				dropLoc.getWorld().dropItemNaturally(dropLoc, CarGetter.createCar(CarMap.get(key).name, CarMap.get(key).enginePower, CarMap.get(key).mass, CarMap.get(key).fuel));
				disposal.put(disposal.size(), key);
			} else {
				if (Bukkit.getPlayer(CarMap.get(key).owner) != null) {
					Bukkit.getPlayer(CarMap.get(key).owner).getInventory().addItem(CarGetter.createCar(CarMap.get(key).name, CarMap.get(key).enginePower, CarMap.get(key).mass, CarMap.get(key).fuel));
				} else {
					if (CarMap.get(key).passenger != null) {
						CarMap.get(key).passenger.getInventory().addItem(CarGetter.createCar(CarMap.get(key).name, CarMap.get(key).enginePower, CarMap.get(key).mass, CarMap.get(key).fuel));
					} else {
						Location dropLoc = new Location(CarMap.get(key).car.getWorld(), CarMap.get(key).x, CarMap.get(key).y, CarMap.get(key).z);
						dropLoc.getWorld().dropItemNaturally(dropLoc, CarGetter.createCar(CarMap.get(key).name, CarMap.get(key).enginePower, CarMap.get(key).mass, CarMap.get(key).fuel));
					}
				}
			}
			CarMap.get(key).remove();
			disposal.put(disposal.size(), key);
		}
		dispose();
	}

	private static void dispose() {
		for (Integer i : disposal.keySet()) {
			CarMap.remove(disposal.get(i));
		}
		disposal.clear();
	}

	public static void dropCar(Car car, Location loc) {
		loc.getWorld().dropItemNaturally(loc, CarGetter.createCar(car.name, car.enginePower, car.mass, car.fuel));
	}
}
