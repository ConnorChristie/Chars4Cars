package me.dasetwas.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.dasetwas.Car;

/**
* Car event storing a car.
* @author DasEtwas
*
*/
public class CarEvent extends Event {

	Car car;

	CarEvent(Car car) {
		this.car = car;
	}

	public Car getCar() {
		return this.car;
	}

	/**
	 * @return Car as a minecart.
	 */
	public Minecart getCarMinecart() {
		return this.car.getCarMinecart();
	}

	/**
	 * @return Car as a minecart as an entity.
	 */
	public Entity getCarEntity() {
		return (Entity) this.car.getCarMinecart();
	}

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
