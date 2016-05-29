package me.dasetwas.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import me.dasetwas.Car;

/**
* Called when a car gets spawned somehow.
* @author DasEtwas
*
*/
public class CarSpawnEvent extends CarEvent implements Cancellable {

	Car car;
	Location loc;
	boolean revived;
	Player user;
	boolean isCancelled = false;

	public CarSpawnEvent(Car car, Location loc, boolean revived, Player user) {
		super(car);
		this.car = car;
		this.loc = loc;
		this.revived = revived;
		this.user = user;
	}

	public Location getLocation() {
		return loc;
	}

	public boolean isRevived() {
		return revived;
	}

	/**
	 * @return User that spawned the car. NOTE: If revived, the passenger is
	 *         given, if locked the owner is given and if the car got spawned
	 *         through code, player = null.
	 */
	public Player getPlayer() {
		return user;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	/**
	 * Removes the car when cancelled.
	 */
	@Override
	public void setCancelled(boolean arg0) {
		this.isCancelled = arg0;
	}

}
