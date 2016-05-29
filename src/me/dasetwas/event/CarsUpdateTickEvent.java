package me.dasetwas.event;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.dasetwas.Car;
import me.dasetwas.Cars;

/**
* Called every Chars4Cars update tick as set in the config.
* @author DasEtwas
*
*/
public class CarsUpdateTickEvent extends Event {

	int carCount;

	public CarsUpdateTickEvent(int carCount) {
		this.carCount = carCount;
	}

	/**
	 * @return Global Car Hashmap containing all car objects.
	 */
	public HashMap<UUID, Car> getCarsMap() {
		return Cars.CarMap;
	}

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
