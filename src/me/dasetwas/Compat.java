package me.dasetwas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Compat {

	public static Sound Drown;
	public static Sound Limit;
	public static Sound Turbo;
	public static Sound Idle;
	public static Sound Run;
	public static Sound Shutoff;
	public static Sound Pop;

	public static void setup() {
		if (Chars4Cars.MCVersion == 19) {
			setSounds19();
		} else {
			setSounds18();
		}
	}

	public static ItemStack getItemInMainHand(PlayerInventory inv) {
		if (inv.getItem(inv.getHeldItemSlot()) == null) {
			return new ItemStack(Material.AIR, 0);
		}
		return inv.getItem(inv.getHeldItemSlot());
	}

	public static void setItemInMainHand(PlayerInventory inv, ItemStack item) {
		inv.setItem(inv.getHeldItemSlot(), item);
	}

	private static void setSounds19() {
		try {
			Drown = Sound.valueOf("BLOCK_LAVA_EXTINGUISH");
			Limit = Sound.valueOf("UI_BUTTON_CLICK");
			Turbo = Sound.valueOf("ENTITY_FIREWORK_LAUNCH");
			Run = Sound.valueOf("ENTITY_HORSE_JUMP");
			Idle = Sound.valueOf("ENTITY_MINECART_RIDING");
			Shutoff = Sound.valueOf("ENTITY_BAT_TAKEOFF");
			Pop = Sound.valueOf("BLOCK_LAVA_POP");
		} catch (Exception e) {
		}
	}

	private static void setSounds18() {
		try {
			Drown = Sound.valueOf("FIZZ");
			Limit = Sound.valueOf("CLICK");
			Turbo = Sound.valueOf("FIREWORK_LAUNCH");
			Run = Sound.valueOf("HORSE_JUMP");
			Idle = Sound.valueOf("MINECART_BASE");
			Shutoff = Sound.valueOf("BAT_TAKEOFF");
			Pop = Sound.valueOf("LAVA_POP");
		} catch (Exception e) {
		}
	}

}
