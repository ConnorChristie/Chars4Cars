package me.dasetwas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class AABB {

	public static final double carWidth = 0.95 / 2;

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
}
