package me.dasetwas;

import java.util.UUID;
//ok
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Car {

	// Minecraft
	UUID owner;
	Player passenger;
	UUID cockpitID;
	Minecart car;
	String name;
	Location soundLoc;
	double x, y, z;
	float yaw;
	double vx, vz, vy;
	float pitch;

	// Physics
	double netForce;
	double engineRPM = 800, lastEngineRPM = 800;
	int mass;
	float brake;
	float throttle;
	int maxEngineRPM = 6500;
	double engineAcc;
	boolean engineRunning = true;
	double clutchPercent;
	double speed;
	int currentGear = 0;
	int enginePower; // Newton-meter
	double driveWheelRadius = 0.42;
	double driveWheelCircumference = driveWheelRadius * 2 * Math.PI;
	float passengerYaw = 0, passengerPitch = 0;
	static double[] gearRatio = Cars.getGearRatios();
	static double[] engineTorques = Cars.getEngineTorques();
	double differentialRatio = 10;
	double currentGearRatio;
	Material currentGround;
	int i;
	boolean isLocked;
	double brakeAcc;
	float steeringDifference;

	/**
	 * Car constructor
	 * 
	 * @param yaw
	 *            Yaw of car when placed
	 * @param spawnLocation
	 *            Location of car when placed
	 * @param owner
	 *            UUID of player owning the car
	 * @param enginePower
	 *            Strength of the engine
	 * @param mass
	 *            Mass of the car
	 * @param name
	 *            Name of the car (original item-name)
	 */
	public Car(float yaw, Location spawnLocation, UUID owner, int enginePower, int mass, String name) {
		this.owner = owner;
		this.enginePower = enginePower;
		this.mass = mass;
		this.x = spawnLocation.getX() + 0.5d;
		this.y = spawnLocation.getY() + 1.1d;
		this.z = spawnLocation.getZ() + 0.5d;
		this.yaw = yaw;
		this.name = name;
		this.isLocked = false;

		car = (Minecart) spawnLocation.getWorld().spawnEntity(new Location(spawnLocation.getWorld(), x, y, z), EntityType.MINECART);

		car.setDerailedVelocityMod(new Vector(1, 1, 1));
		car.setFlyingVelocityMod(new Vector(1, 1, 1));
		car.setSlowWhenEmpty(false);
		car.setMaxSpeed(100);
		car.setDisplayBlock(new MaterialData(Material.BARRIER));

		car.teleport(new Location(spawnLocation.getWorld(), x, y, z, yaw, 0));

		this.cockpitID = car.getUniqueId();
	}

	/**
	 * Updates all car functions.
	 */
	public void updateCar() {

		if (car.isDead()) {
			remove();
		}

		// *=*=*=* Getter block
		this.x = car.getLocation().getX();
		this.y = car.getLocation().getY();
		this.z = car.getLocation().getZ();
		this.vx = car.getVelocity().getX();
		this.vz = car.getVelocity().getZ();
		this.vy = car.getVelocity().getY();

		speed = (Math.sqrt(vx * vx + vz * vz)) * 20;

		clutchPercent = Math.min(1, clutchPercent + 0.15);

		if (car.getPassenger() instanceof Player) {
			passenger = (Player) car.getPassenger();
			engineRunning = true;

			this.passengerPitch = passenger.getLocation().getPitch();
			this.passengerYaw = passenger.getLocation().getYaw();

			if (passengerPitch >= -7.5f && passengerPitch <= 7.5f) {
				throttle = 1 - (passengerPitch + 7.5f) / 15;
			} else if ((passengerPitch < -7.5)) {
				throttle = 1;
			} else if (passengerPitch > 7.5) {
				throttle = 0;
			}

			if (passengerPitch > 7.5f && passengerPitch <= 22.5f) {
				brake = (passengerPitch - 7.5f) / 15;
			} else if (passengerPitch > 22.5f) {
				brake = 1;
			} else {
				brake = 0;
			}
		} else {
			brake = 1;
			engineRunning = false;
			currentGear = 0;
			throttle = 0;
			passenger = null;
		}

		// Set gearRatio to current gear's one.
		currentGearRatio = gearRatio[currentGear + 1];

		// NaN Check
		if (speed != speed) {
			speed = 0;
		}

		// Engine simulation in neutral gear, or when in air
		if (currentGear == 0 || !isOnGround()) {
			double d = throttle * maxEngineRPM - engineRPM;
			engineRPM = engineRPM - (engineRPM * 0.002) * (1 - throttle);
			if (throttle * maxEngineRPM > engineRPM) {
				engineRPM = engineRPM + (d * 0.05);
				if (engineRPM < 10) {
					engineRPM = 300;
				}
			}
			if (engineRunning) {
				engineRPM = Math.max(800 + Math.random() * 10, engineRPM);
			} else {
				if (engineRPM < 600 && engineRPM != 0) {
					engineRPM = 0;
					car.getLocation().getWorld().playSound(car.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 0.5f);
				}
			}

			engineRPM = Math.min(engineRPM, maxEngineRPM);
			brakeAcc = (brake * 1300 / mass * speed) * 0.1;

			if (speed - brakeAcc < 0) {
				speed = 0;
				brakeAcc = 0;
			}

			speed = speed + getAirDrag() - brakeAcc;

			if (speed > 0 && Math.abs(speed) < 1) {
				speed = 0;
			} else if (speed < 0 && Math.abs(speed) < 1) {
				speed = 0;
			}

			clutchPercent = 0;
		} else {

			engineRPM = ((speed / driveWheelCircumference) * Math.abs(currentGearRatio) * 60 * differentialRatio) * clutchPercent + engineRPM * (1 - clutchPercent);

			// Get force of engine

			if (engineRPM < 800) {
				engineRPM = 800 + Math.random() * 10;
			}

			engineAcc = getEngineTorque() * enginePower * throttle * clutchPercent * currentGearRatio * differentialRatio / driveWheelCircumference / mass;
			brakeAcc = (brake * 1000 / mass * speed) * 0.1;

			if (engineRPM > maxEngineRPM) {

				if (currentGear == -1) {
					if (speed < 0) {
						engineAcc = engineAcc * 2;
					} else {
						engineAcc = -engineAcc * 2;
					}
				}

				engineAcc = -engineAcc * 2;
			}

			if (currentGear == -1) {
				if (speed - brakeAcc > 0) {
					speed = 0;
					brakeAcc = 0;
				}
			} else {
				if (speed - brakeAcc < 0) {
					speed = 0;
					brakeAcc = 0;
				}
			}

			speed = speed + engineAcc + getAirDrag() - brake;
		}

		if (Cars.isClimbable(car.getWorld().getBlockAt((int) (this.x + this.vx), (int) this.y, (int) (this.z + this.vz)).getType())) {
			this.vy = this.vy + 1;
		}

		System.out.println(car.getWorld().getBlockAt((int) (this.x + this.vx), (int) this.y, (int) (this.z + this.vz)).getType().toString());

		yaw = passengerYaw;

		// *=*=*=* Settter Block

		soundLoc = new Location(car.getWorld(), this.x + this.vx, this.y + this.vy, this.z + this.vz);

		if (Math.abs(lastEngineRPM - engineRPM) > 3 || (Math.floor(Math.random() * 3) == 0 && engineRPM > 1000)) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_MINECART_RIDING, (float) (engineRPM / maxEngineRPM * 0.5) + 0.3f, (float) (engineRPM / maxEngineRPM));
		}
		if (engineRPM > 0) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_HORSE_JUMP, (float) (engineRPM / maxEngineRPM * 0.25) + 0.3f, (float) (engineRPM / maxEngineRPM));
		}

		if (engineRPM > 3000 && enginePower >= 150) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, (float) ((engineRPM / 3000) - 1f) * 0.45f, (float) (engineRPM / maxEngineRPM) * 2 - 1);
		}

		if (engineRPM > maxEngineRPM) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
		}

		if (Chars4Cars.exhaustSmoke.equalsIgnoreCase("true") && engineRPM > 400) {
			car.getWorld().spawnParticle(Particle.SMOKE_NORMAL, new Location(car.getWorld(), this.x + rotateScalar(0.7, -this.yaw).getX(), 0, this.z + rotateScalar(0.7, -this.yaw).getX()), 3);
		}

		try {
			passenger.sendMessage("Gear: " + (int) currentGear);
			passenger.sendMessage("Speed: " + (int) (speed / 1000 * 3600) + " Kb/h");
			passenger.sendMessage("RPM " + (int) engineRPM);
			passenger.sendMessage("Throttle: " + throttle);
			passenger.sendMessage("Brake: " + brake);
			passenger.sendMessage("Clutchpercent: " + clutchPercent);
			passenger.sendMessage("Airdrag: " + getAirDrag());
		} catch (Exception e) {
		}

		// calculate car's speed
		if (Math.abs(speed) > 0) {
			this.vx = rotateScalar(speed, this.yaw).getX() / 20;
			this.vz = rotateScalar(speed, this.yaw).getZ() / 20;
		} else {
			this.vx = 0;
			this.vz = 0;
		}

		// set car's speed
		car.setVelocity(new Vector(this.vx, this.vy, this.vz));

		// to know engineRPM's velocity
		lastEngineRPM = engineRPM;
		// *=*=*=*
	}

	/**
	 * Shift up
	 */
	public void shiftUp() {
		this.currentGear++;
		if (this.currentGear > 6) {
			this.currentGear = 6;
		} else {
			clutchPercent = 0;
		}
	}

	/**
	 * Shift down
	 */
	public void shiftDown() {
		this.currentGear--;
		if (this.currentGear < -1) {
			this.currentGear = -1;
		} else {
			clutchPercent = 0;
		}
	}

	/**
	 * @return UUID of minecart entity (called Cockpit; Entity car)
	 */

	public UUID getCockpitID() {
		return cockpitID;
	}

	/**
	 * @return UUID of the owner of the car
	 */
	public UUID getOwner() {
		return this.owner;
	}

	/**
	 * @param v
	 *            Distance (velocity)
	 * @param yaw
	 *            Rotation on Y axis, degrees
	 * @return Vector(sin(yaw), 0, cos(yaw))
	 */
	public Vector rotateScalar(double v, float yaw) {
		return new Vector(Math.sin(Math.toRadians(yaw)) * -v, 0, Math.cos(Math.toRadians(yaw)) * v);
	}

	/**
	 * @return If the car has a block underneath it. (r = 0.06)
	 */
	public boolean isOnGround() {
		return (car.getWorld().getBlockAt(new Location(car.getWorld(), car.getLocation().getX(), car.getLocation().getY() - 0.06, car.getLocation().getZ())).getType().isSolid());
	}

	/**
	 * @return Locking state of car
	 */
	public boolean isLocked() {
		return this.isLocked;
	}

	/**
	 * Locks car
	 */
	public void lock() {
		this.isLocked = true;
	}

	public void unLock() {
		isLocked = false;
	}

	/**
	 * @return Linear Interpolation of value in array
	 */
	public double getEngineTorque() {
		int i1 = (int) Math.floor(engineRPM / (maxEngineRPM / engineTorques.length));
		int i2 = (int) Math.floor(engineRPM / (maxEngineRPM / engineTorques.length)) + 1;

		double v1 = engineTorques[Math.min(Math.max(i1, 0), engineTorques.length - 1)];
		double v2 = engineTorques[Math.min(engineTorques.length - 1, i2)];

		double ratio = (engineRPM / (maxEngineRPM / engineTorques.length)) - i1;
		return v1 + (v2 - v1) * ratio;
	}

	/**
	 * @return Air Drag of car
	 */
	public double getAirDrag() {
		return -speed * 10 / mass;

	}

	public void remove() {
		car.remove();
	}

}
