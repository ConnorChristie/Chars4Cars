package me.dasetwas;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;

public class Cars {

	public static HashMap<UUID, Car> CarMap = new HashMap<UUID, Car>();

	/**
	 * @param uuid
	 *            UUID of entity to check
	 * @return If UUID belongs to a car
	 */
	public static boolean isCar(UUID uuid) {
		return CarMap.containsKey(uuid);
	}

	/**
	 * @return All 8 Gear ratios (R, N, 1, 2, 3, 4, 5, 6) (Enginestrenght /
	 *         gearRatio = force)
	 */
	public static double[] getGearRatios() {
		// R N 1 2 3 4 5 6
		double[] gearRatio = { -4, 0.00, 4.33, 2.67, 1.81, 1.38, 1.10, 0.91 };
		return gearRatio;
	}

	/**
	 * Cylces through all cars in the HashMap and calls updateCar() on them
	 */
	public static void cycleCars() {
		for (UUID key : CarMap.keySet()) {
			CarMap.get(key).updateCar();
			;
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

	/**
	 * @param type
	 *            Given material
	 * @return If given material is a Slab
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
		}
		return false;
	}
}
