package me.dasetwas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * @author DasEtwas
 *
 */
public class CarGetter {

	/**
	 * Creates car itemstack and gives it to cmdSender
	 * 
	 * @param cmdSender
	 *            Player to give the item to
	 * @param name
	 *            Car Name
	 * @param enginePower
	 *            Car engine Power
	 * @param carMass
	 *            Car mass
	 */
	public static void getCar(Player cmdSender, String name, int enginePower, int carMass, double fuel) {
		cmdSender.getInventory().addItem(createCar(name, enginePower, carMass, fuel));
	}

	/**
	 * Creates car itemstack
	 * 
	 * @param name
	 *            Car name
	 * @param enginePower
	 *            Car engine Power
	 * @param carMass
	 *            Car mass
	 * @return Car ItemStack
	 */
	public static ItemStack createCar(String name, int enginePower, int carMass, double fuel) {

		ItemStack car = new ItemStack(Material.MINECART);
		ItemMeta carMeta = car.getItemMeta();
		carMeta.setDisplayName(name);
		List<String> loreList = new ArrayList<String>();
		loreList.add(ChatColor.DARK_GRAY + "Chars4Cars Car");
		loreList.add(ChatColor.GREEN + Integer.toString(enginePower));
		loreList.add(ChatColor.GREEN + Integer.toString(carMass));
		loreList.add(ChatColor.GREEN + Double.toString(fuel));
		carMeta.setLore(loreList);
		car.setItemMeta(carMeta);
		return car;
	}

}
