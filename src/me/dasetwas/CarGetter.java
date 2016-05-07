package me.dasetwas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
	public static void getCar(Player cmdSender, String name, int enginePower, int carMass) {

		ItemStack car = new ItemStack(Material.MINECART, 1);
		ItemMeta carMeta = car.getItemMeta();
		carMeta.setDisplayName(name);
		List<String> loreList = new ArrayList<String>();
		loreList.add(Integer.toString(enginePower));
		loreList.add(Integer.toString(carMass));
		carMeta.setLore(loreList);
		car.setItemMeta(carMeta);
		cmdSender.getInventory().addItem(car);
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
	public static ItemStack createCar(String name, int enginePower, int carMass) {

		ItemStack car = new ItemStack(Material.MINECART, 1);
		ItemMeta carMeta = car.getItemMeta();
		carMeta.setDisplayName(name);
		List<String> loreList = new ArrayList<String>();
		loreList.add(Integer.toString(enginePower));
		loreList.add(Integer.toString(carMass));
		carMeta.setLore(loreList);
		car.setItemMeta(carMeta);
		return car;
	}

}
