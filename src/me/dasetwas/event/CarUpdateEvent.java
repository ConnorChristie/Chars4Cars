package me.dasetwas.event;

import me.dasetwas.Car;

/**
 * When a car with 'notifyUpdate' set to true calls for updates! nom nom nom :D
 * 
 * @author DasEtwas
 *
 */
public class CarUpdateEvent extends CarEvent {

	public CarUpdateEvent(Car car) {
		super(car);
	}

}
