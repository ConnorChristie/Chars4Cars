package me.dasetwas;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;

public class Cars {

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
		// R N 1 2 3 4 5 6
		double[] gearRatio = { -5, 0.00, 4.33, 2.67, 1.81, 1.38, 1.10, 0.91 };
		return gearRatio;
	}

	/**
	 * Cylces through all cars in the HashMap and calls updateCar() on them
	 */
	public static void cycleCars() {
		for (UUID key : CarMap.keySet()) {
			CarMap.get(key).updateCar();
		}
	}

	/**
	 * @return Gets Graph of engineTorque at given RPMs (using
	 *         linearInterpolation)
	 */
	public static double[] getEngineTorques() {
		// 1 2 3 4 5 6 7 8 9 10
		double[] engineTorques = { 0.35, 0.39, 0.45, 0.56, 0.67, 0.89, 1.00, 0.95, 0.46, 0.35 };
		return engineTorques;
	}

	public static char[] getGearNames() {
		char[] gearNames = { 'R', 'N', '1', '2', '3', '4', '5', '6' };
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
		} else if (type.equals(Material.PURPUR_SLAB)) {
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
		} else if (type.equals(Material.PURPUR_STAIRS)) {
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
		} else if (type.equals(Material.PURPUR_SLAB)) {
			return true;
		} else if (type.equals(Material.STEP)) {
			return true;
		} else if (type.equals(Material.WOOD_STEP)) {
			return true;
		} else if (type.equals(Material.STEP)) {
			return true;
		} else if (type.equals(Material.DAYLIGHT_DETECTOR) || type.equals(Material.DAYLIGHT_DETECTOR_INVERTED)) {
			return true;
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
}
