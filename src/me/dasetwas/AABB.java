package me.dasetwas;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * 
 * @author DasEtwas
 *
 */
public class AABB {

	public static final double carWidth = 0.95 / 2;

	/**
	 * @param loc
	 *            Location of Car Entity to simulate collision with.
	 * @return Material[4] with Block materials at edges of hitbox
	 */
	public static Material[] collide(Location loc) {
		double x = loc.getX(), xx;
		double y = loc.getY(), yy;
		double z = loc.getZ(), zz;
		Material[] types = new Material[4];

		int i = 0;
		for (i = 0; i < 4; i++) {
			xx = x + ((i % 2) * 2 - 1) * carWidth;
			zz = z + ((int) (i / 2) * 2 - 1) * carWidth;
			yy = y;
			types[i] = new Location(loc.getWorld(), (int) (xx), (int) (yy), (int) (zz)).getBlock().getType();
		}

		return types;
	}

	/**
	 * @param loc
	 *            Location of Car Entity to simulate collision with.
	 * @return If hitbox collides with a Slab.
	 */
	public static boolean hasSlab(Location loc) {
		int i = 0;
		Material[] types = collide(loc);

		for (i = 0; i < 4; i++) {
			if (Cars.isSlab(types[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param loc
	 *            Location of Car Entity to simulate collision with.
	 * @return If hitbox collides with a climbable block.
	 */
	public static boolean hasClimbable(Location loc) {
		int i = 0;
		Material[] types = collide(loc);

		for (i = 0; i < 4; i++) {
			if (Cars.isClimbable(types[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param loc
	 *            Location of Car Entity to simulate collision with.
	 * @return If hitbox collides with a solid block.
	 */
	public static boolean hasSolid(Location loc) {
		int i = 0;
		Material[] types = collide(loc);

		for (i = 0; i < 4; i++) {
			if (types[i].isSolid()) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasClimbList(Location loc) {
		int i = 0;
		Material[] types = collide(loc);

		for (i = 0; i < 4; i++) {
			if (Chars4Cars.climbBlocksList != null) {
				if (Chars4Cars.climbBlocksList.contains(types[i].toString().toUpperCase())) {
					return true;
				}
			}
		}
		return false;
	}
}
