package me.dasetwas;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.dasetwas.event.CarSpawnEvent;

/**
 * Official Chars4Cars API. Gives complete access to all important functions
 * besides permission handling or placement.
 * 
 * @author DasEtwas
 */

public class CarsAPI {

	/**
	 * Gets HashMap of existing Car instances (not unrevived cars) Key: UUID of
	 * minecart entity
	 * 
	 * @return CarMap
	 */
	public HashMap<UUID, me.dasetwas.Car> getCarsMap() {
		return Cars.CarMap;
	}

	/**
	 * Gets Car out of getCarsMap() HashMap
	 * 
	 * @param uuid
	 *            UUID of minecart entity
	 * @return Car or null if UUID not present in list.
	 */
	public Car getCar(UUID uuid) {
		if (Cars.CarMap.containsKey(uuid)) {
			return Cars.CarMap.get(uuid);
		}
		return null;
	}

	/**
	 * Spawns a Car while creating a Car object.
	 * 
	 * @param spawnLocation
	 *            Location for car to appear (Note: Should be block coordinate.
	 *            X+0.5, Y+1, Z+0.5)
	 * @param name
	 *            Name of Car
	 * @param mass
	 *            Mass of Car
	 * @param power
	 *            Power of Car
	 * @param fuel
	 *            Fuel of Car
	 * @param ghost
	 *            If true, the Car will not be added to the global CarMap to
	 *            update it, you can then do it yourself.
	 * @return Car object
	 */
	public Car createCar(Location spawnLocation, String name, int enginePower, int carMass, double fuel, boolean ghost) {
		Car car = new Car(carMass, spawnLocation, null, carMass, carMass, name, fuel);

		Bukkit.getServer().getPluginManager().callEvent(new CarSpawnEvent(car, spawnLocation, false, null));

		if (!ghost) {
			Cars.CarMap.put(car.getCockpitID(), car);
		}

		return car;
	}

	/**
	 * Creates a car ItemStack (amount 1)
	 * 
	 * @param name
	 *            Name of Car
	 * @param mass
	 *            Mass of Car
	 * @param power
	 *            Power of Car
	 * @param fuel
	 *            Fuel of Car
	 * @return ItemStack of created car, like in /givecar x x x x
	 */
	public ItemStack createCar(String name, int enginePower, int carMass, double fuel) {
		return CarGetter.createCar(name, enginePower, carMass, fuel);
	}
}
