package me.dasetwas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Compat {

	public static Sound FireExtinguish;
	public static Sound ChestOpen;
	public static Sound FireworkLaunch;
	public static Sound HorseJump;
	public static Sound MinecartRoll;
	public static Sound BatTakeoff;

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
			FireExtinguish = Sound.valueOf("BLOCK_LAVA_EXTINGUISH");
			ChestOpen = Sound.valueOf("BLOCK_CHEST_OPEN");
			FireworkLaunch = Sound.valueOf("ENTITY_FIREWORK_LAUNCH");
			HorseJump = Sound.valueOf("ENTITY_HORSE_JUMP");
			MinecartRoll = Sound.valueOf("ENTITY_MINECART_RIDING");
			BatTakeoff = Sound.valueOf("ENTITY_BAT_TAKEOFF");
		} catch (Exception e) {
		}
	}

	private static void setSounds18() {
		try {
			FireExtinguish = Sound.valueOf("FIZZ");
			ChestOpen = Sound.valueOf("CHEST_OPEN");
			FireworkLaunch = Sound.valueOf("FIREWORK_LAUNCH");
			HorseJump = Sound.valueOf("HORSE_JUMP");
			MinecartRoll = Sound.valueOf("MINECART_BASE");
			BatTakeoff = Sound.valueOf("BAT_TAKEOFF");
		} catch (Exception e) {
		}
	}

}
