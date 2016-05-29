package me.dasetwas.event;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Gets fired when a car thats broken after a reload or restart gets converted from an entity to an item state.
 * Can be used to disable cars temporarily on servers, but not breaking the cars completely.
 * @author DasEtwas
 *
 */
public class CarConvertEvent implements Cancellable{

	boolean isCancelled;
	Minecart car;
	
	public CarConvertEvent(Minecart car) {
		this.car = car;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		isCancelled = arg0;
	}
	
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
