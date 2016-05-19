package me.dasetwas;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

/**
 * 
 * @author DasEtwas
 *
 */
public class Car {

	// Minecraft
	UUID owner;
	Player passenger;
	UUID cockpitID;
	Minecart car;
	String name;
	Location soundLoc;
	Location climbLoc;
	double x, y, z;
	float yaw;
	double vx, vz, vy;
	float pitch;
	float forw;
	float side;
	Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

	// Physics
	double steerAngle;
	double netForce;
	double engineRPM = 0, lastEngineRPM = 0;
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
	static char[] gearNames = Cars.getGearNames();
	double differentialRatio = Chars4Cars.differentialRatio;
	double currentGearRatio;
	Material currentGround;
	int i;
	boolean isLocked;
	double fuel;
	double brakeAcc;
	float steeringDifference;
	double lastSpeed;
	double G;
	double hitBoxX;
	double hitBoxZ;
	double rSpeed;
	boolean active;

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
	public Car(float yaw, Location spawnLocation, UUID owner, int enginePower, int mass, String name, double fuel) {
		this.owner = owner;
		this.enginePower = enginePower;
		this.mass = mass;
		this.x = spawnLocation.getX() + 0.5d;
		this.y = spawnLocation.getY() + 1.0d;
		this.z = spawnLocation.getZ() + 0.5d;
		this.yaw = yaw;
		this.name = name;
		this.isLocked = false;
		this.fuel = fuel;

		car = (Minecart) spawnLocation.getWorld().spawnEntity(new Location(spawnLocation.getWorld(), x, y, z), EntityType.MINECART);

		car.setCustomName("�aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		car.setDerailedVelocityMod(new Vector(1, 1, 1));
		car.setFlyingVelocityMod(new Vector(1, 1, 1));
		car.setSlowWhenEmpty(false);
		car.setMaxSpeed(((double) Chars4Cars.speedLimit) / 3.6 / 20);
		car.setDisplayBlock(new MaterialData(Material.BARRIER));

		car.teleport(new Location(spawnLocation.getWorld(), x, y, z, yaw - 90, 0));

		this.cockpitID = car.getUniqueId();
	}

	/**
	 * Car constructor for existing car entities
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
	public Car(UUID owner, int enginePower, int mass, String name, Minecart car, double fuel) {
		this.owner = owner;
		this.enginePower = enginePower;
		this.mass = mass;
		this.name = name;
		this.isLocked = false;
		this.car = car;
		this.fuel = fuel;

		car.setCustomName("�aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		car.setDerailedVelocityMod(new Vector(1, 1, 1));
		car.setFlyingVelocityMod(new Vector(1, 1, 1));
		car.setSlowWhenEmpty(false);
		car.setMaxSpeed(((double) Chars4Cars.speedLimit) / 3.6 / 20);
		car.setDisplayBlock(new MaterialData(Material.BARRIER));

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
		this.yaw = car.getLocation().getYaw() + 90;

		speed = Math.sqrt(vx * vx + vz * vz) * 20;
		if (currentGear == -1) {
			speed = -speed;
		}
		rSpeed = speed;

		if (currentGear != 1) {
			clutchPercent = Math.min(1, clutchPercent + 0.075);
		} else {
			if (throttle < 0.75) {
				clutchPercent = Math.min(1, clutchPercent + 0.01);
			} else {
				clutchPercent = Math.min(1, clutchPercent + 0.1);
			}
		}

		// Getting all passenger variables and producing throttle and brake
		// variables
		if (car.getPassenger() instanceof Player) {
			if (owner == null) {
				owner = this.passenger.getUniqueId();
			}

			passenger = (Player) car.getPassenger();
			this.passengerPitch = passenger.getLocation().getPitch();
			this.passengerYaw = passenger.getLocation().getYaw();
			engineRunning = true;

			if (throttle > 0.5) {
				throttle = (float) Math.max(0.5, throttle - 0.02);
			}
			if (throttle < 0.5 && throttle > 0.25) {
				throttle = (float) Math.max(0.25, throttle - 0.01);
			}
			if (throttle < 0.25 && throttle > 0.1) {
				if (currentGear == -1) {
					throttle = (float) Math.max(0.10, throttle - 0.01);
				} else {
					throttle = (float) Math.max(0, throttle - 0.01);
				}
			}
			if (throttle < 0.1) {
				throttle = (float) Math.max(0, throttle - 0.01);
			}

			if (forw > 0) {
				if (brake == 0) {
					throttle = (float) Math.min(1, throttle + 0.075);
				} else {
					brake = (float) Math.max(0, brake - 0.12);
				}
			} else if (forw < 0) {
				if (currentGear == 1 || currentGear == -1) {
					throttle = 0;
					brake = (float) Math.min(1, brake + 0.04);
				} else {
					if (throttle == 0) {
						brake = (float) Math.min(1, brake + 0.04);
					} else {
						throttle = (float) Math.max(0, throttle - 0.05);
					}
				}
			}
			active = true;
		} else {
			if (speed == 0) {
				active = false;
			}

			brake = 1;
			engineRunning = false;
			currentGear = 0;
			throttle = 0;
			passenger = null;
		}
		if (!active) {
			return;
		}

		// Steering and returning to zero
		if (side != 0) {
			if (steerAngle < 0 && side > 0) {
				steerAngle = 0;
			} else if (steerAngle > 0 && side < 0) {
				steerAngle = 0;
			}
			steerAngle = Math.min(25, Math.max(-25, steerAngle + side * 2));
		} else {
			if (steerAngle > 0) {
				steerAngle = Math.max(0, steerAngle - 10);
			} else if (steerAngle < 0) {
				steerAngle = Math.min(0, steerAngle + 10);
			}
		}

		this.yaw = (float) (this.yaw + (-steerAngle * (Math.min(Math.abs(rSpeed) / 20, 1)) / Math.max(1, rSpeed / 5)) * (currentGear == -1 ? -1 : 1) * 0.6);

		// Set gearRatio to current gear's one.
		currentGearRatio = gearRatio[currentGear + 1];

		// NaN Check
		if (speed != speed) {
			speed = 0;
		}

		// Engine simulation in neutral gear, or when in air
		if (currentGear == 0 || !isOnGround()) {

			if ((Chars4Cars.fuel && !(fuel == 0)) || !Chars4Cars.fuel) {
				// Raise engineRPM to desired throttle * maxEngineRPM IF we have
				// fuel left.
				double d = throttle * maxEngineRPM - engineRPM;
				engineRPM = engineRPM - (engineRPM * 0.002) * (1 - throttle);
				if (throttle * maxEngineRPM > engineRPM) {
					engineRPM = engineRPM + (d * 0.05);
					if (engineRPM < 10) {
						engineRPM = 300;
					}
				}
			}

			// Keep engine over 800+ RPM
			if (engineRunning) {
				engineRPM = Math.max(800 + Math.random() * 10, engineRPM);
			} else {
				// If engine cycle collapses, play shutoff sound
				if (engineRPM < 600 && engineRPM != 0) {
					engineRPM = 0;
					car.getLocation().getWorld().playSound(car.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 0.5f);
				}
			}

			// Prevent engine from overshooting maxEngineRPM (rev limiter)
			engineRPM = Math.min(engineRPM, maxEngineRPM);
			brakeAcc = brake * 0.45;

			speed = subUntilZero(speed, brakeAcc);

			// Apply forces with air resistance (drag)
			speed = speed + getAirDrag();

			// Round of the speed to not produce very very small decimal numbers
			if (speed != 0 && Math.abs(speed) < 0.1) {
				speed = 0;
			}

			clutchPercent = 0;
		} else {
			double d = throttle * maxEngineRPM - engineRPM;
			engineRPM = Math.abs((speed / driveWheelCircumference) * currentGearRatio * 60 * differentialRatio) * clutchPercent + (engineRPM + (d * 0.05)) * (1 - clutchPercent);

			// Get torque of engine
			if (engineRPM < 800) {
				engineRPM = 800 + Math.random() * 10;
			}

			engineAcc = getEngineTorque() * enginePower * throttle * clutchPercent * currentGearRatio * differentialRatio / driveWheelCircumference / mass;
			brakeAcc = brake * 0.45;

			// Check if the RPM limiter has to kick in
			if (engineRPM > maxEngineRPM) {
				if (currentGear == -1) {
					speed = maxEngineRPM / currentGearRatio * driveWheelCircumference / differentialRatio / 60;
					engineAcc = 0;
				} else {
					speed = subUntilZero(speed, engineAcc * 2);
				}

			}

			speed = subUntilZero(speed, brakeAcc);

			// Apply forces with air resistance (drag)
			speed = speed + engineAcc + getAirDrag();
		}

		// Fuel handling
		if (Chars4Cars.fuel) {
			if (fuel > Chars4Cars.maxFuel) {
				fuel = Chars4Cars.maxFuel;
			}
			if (fuel > 0) {
				fuel = fuel - (throttle * 0.0000001) - engineRPM * 0.00000001 * (enginePower / 10);
			} else {
				currentGear = 0;
			}
		}
		// ---

		G = (engineAcc - getAirDrag() - brakeAcc) / 16 / (0.05) * Chars4Cars.updateDelta;

		// Some code by storm345 modified to appeal more to me :)
		BlockFace face = getFace(this.yaw);
		if (Math.abs(this.vx) > 1) {
			this.vx = face.getModX();
		}
		if (Math.abs(this.vz) > 1) {
			this.vz = face.getModZ();
		}

		if (this.vx > 0) {
			hitBoxX = 0.5;
		} else if (this.vx < 0) {
			hitBoxX = -0.5;
		} else {
			hitBoxX = 0;
		}

		if (this.vz > 0) {
			hitBoxZ = 0.5;
		} else if (this.vz < 0) {
			hitBoxZ = -0.5;
		} else {
			hitBoxZ = 0;
		}

		climbLoc = car.getLocation().add(this.vx * 2 + hitBoxX, face.getModY() + 0.5, this.vz * 2 + hitBoxZ);

		if (!Chars4Cars.climbBlocks) {
			// Check if to climb from a slab to a block
			if (car.isOnGround() && isOnGround() && Math.abs(speed) > 0.1 && (this.y - ((int) this.y)) > 0.3) {
				if (!Cars.isSlab((car.getLocation().add(this.vx * 2 + hitBoxX, face.getModY(), this.vz * 2 + hitBoxZ)).getBlock().getType())) {
					if (!climbLoc.add(0, 1, 0).getBlock().getType().isSolid()) {
						if (!((car.getLocation().add(this.vx * 2 + hitBoxX, face.getModY(), this.vz * 2 + hitBoxZ)).getBlock().getType().equals(Material.SNOW))) {
							this.vy = 0.3;
						}
					}
				}
			}
			// Is in front of climbable block
			if (Cars.isClimbable(climbLoc.getBlock().getType()) && Math.abs(speed) > 0.1) {
				// Is on slab
				if (!(car.isOnGround() && isOnGround() && Math.abs(speed) > 0.1 && (this.y - ((int) this.y)) > 0.3)) {
					if (!climbLoc.add(0, 1, 0).getBlock().getType().isSolid()) {
						if (Cars.isSlab(climbLoc.getBlock().getType())) {
							this.vy = 0.3;
						} else {
							this.vy = 0.45;
						}
					}
				}
			}
		} else {
			if (Chars4Cars.climbBlocksList.contains(climbLoc.getBlock().getType().toString())) {
				if (!climbLoc.add(0, 1, 0).getBlock().getType().isSolid()) {
					if (Cars.isSlab(climbLoc.getBlock().getType())) {
						this.vy = 0.3;
					} else {
						this.vy = 0.45;
					}
				}
			}
		}
		if (climbLoc.getBlock().getType().equals(Material.CARPET)) {
			this.vy = 0.15;
		}

		// *=*=*=* Settter Block
		soundLoc = new Location(car.getWorld(), this.x + this.vx, this.y + this.vy, this.z + this.vz);

		if (Math.abs(lastEngineRPM - engineRPM) > 3 || (Math.floor(Math.random() * 3) == 0 && engineRPM > 1000)) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_MINECART_RIDING, (float) ((engineRPM / maxEngineRPM * 0.5) + 0.3f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
		}
		if (engineRPM > 0) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_HORSE_JUMP, (float) ((engineRPM / maxEngineRPM * 0.25) + 0.3f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
		}

		if (engineRPM > 3000 && enginePower >= 150) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, (float) (((engineRPM / 3000) - 1f) * 0.45f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM) * 2 - 1);
		}

		if (engineRPM > maxEngineRPM) {
			soundLoc.getWorld().playSound(car.getLocation(), Sound.BLOCK_CHEST_OPEN, 1 * Chars4Cars.volume, 1);
		}

		if ((car.getWorld().getBlockAt(car.getLocation()).getType().equals(Material.WATER) || car.getWorld().getBlockAt(car.getLocation()).getType().equals(Material.STATIONARY_WATER)) && engineRunning) {
			engineRunning = false;
			engineRPM = 0;
			soundLoc.getWorld().playSound(soundLoc, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
		}
		if (Cars.isRail(car.getWorld().getBlockAt(car.getLocation()).getType())) {
			car.getWorld().dropItemNaturally(car.getLocation(), CarGetter.createCar(this.name, this.enginePower, this.mass, this.fuel));
			car.eject();
			remove();
		}

		if (Chars4Cars.exhaustSmoke && engineRPM > 400) {
			Vector flyVec = new Vector(rotateScalar(0.2, yaw + 180).getX(), 0.06 + Math.random() / 2, rotateScalar(0.2, yaw + 180).getZ());
			Location exhaustLoc = new Location(car.getWorld(), this.x + rotateScalar(0.8, yaw + 180).getX(), this.y + 0.1, this.z + rotateScalar(0.8, yaw + 180).getZ());
			ParticleEffect.SMOKE_NORMAL.display(flyVec, 0.4f, exhaustLoc, 20.0);
		}

		// Print debug information
		try {
			if (passenger != null) {
				try {
					sb.getObjective("stats").unregister();
				} catch (Exception e) {

				}
				Objective stats = sb.registerNewObjective("stats", "dummy");
				stats.setDisplaySlot(DisplaySlot.SIDEBAR);
				stats.setDisplayName("Statistics");

				Score scSpeed = stats.getScore("Speed: " + ((int) (speed * 3.6)) + "Kb/h");
				Score scEngineRPM = stats.getScore("Engine RPM: " + ((int) engineRPM));
				Score scCurrentGear = stats.getScore("Gear: " + gearNames[currentGear + 1]);
				Score scThrottle = stats.getScore("Throttle: " + (int) Math.floor(throttle * 100) + "%");
				Score scBrake = stats.getScore("Brake: " + (int) Math.floor(brake * 100) + "%");
				Score scG = stats.getScore("G Force: " + (float) Math.floor(G * 100) / 100 + "*32m/s�");

				if (Chars4Cars.fuel) {
					Score scFuel = stats.getScore("Fuel: " + Math.floor(fuel * 100) / 100);
					scFuel.setScore(0);
				}

				scSpeed.setScore(1);
				scEngineRPM.setScore(2);
				scCurrentGear.setScore(3);
				scThrottle.setScore(4);
				scBrake.setScore(5);
				scG.setScore(6);

				passenger.setScoreboard(sb);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Calculate car's speed
		if (Math.abs(speed) > 0) {
			this.vx = rotateScalar(speed, this.yaw).getX() / 20;
			this.vz = rotateScalar(speed, this.yaw).getZ() / 20;
		} else {
			this.vx = 0;
			this.vz = 0;
		}

		// Check if car is approaching rail
		if (Cars.isRail(climbLoc.getBlock().getType())) {
			// Stop the car
			this.vx = 0;
			this.vz = 0;
		}

		// set car's speed
		car.setVelocity(new Vector(this.vx, this.vy, this.vz));

		// to know engineRPM's velocity
		lastEngineRPM = engineRPM;
		// to know acceleration
		lastSpeed = speed;
		car.setCustomName("�aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		// *=*=*=*
	}

	/**
	 * Shift up
	 */
	public void shiftUp() {
		if (rSpeed < 0 && currentGear == -1) {
		} else {
			this.currentGear++;
		}
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
		if (rSpeed > 0 && currentGear - 1 == -1) {
		} else {
			this.currentGear--;
		}
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
	 * Locks the car
	 */
	public void lock() {
		this.isLocked = true;
	}

	/**
	 * Unlocks the car
	 */
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
		return -speed * 9.2 / mass;

	}

	/**
	 * Removes the car
	 */
	public void remove() {
		car.setDamage(-1);
		car.remove();
		try {
			this.finalize();
		} catch (Throwable e) {
		}
	}

	/**
	 * Sets Side. Used for steering.
	 * 
	 * @param side
	 *            amount
	 */
	public void setSide(float side) {
		this.side = side;
	}

	/**
	 * Sets Forw. Used for accelerating.
	 * 
	 * @param forw
	 *            amount
	 */
	public void setForw(float forw) {
		this.forw = forw;
	}

	/**
	 * Subtracts the amount of a number by b A - B When A is greater than 0, and
	 * B greater than A, returns 0 When A is smaller than 0, and B greater than
	 * B, returns 0
	 * 
	 * @param a
	 * @param b
	 */
	public double subUntilZero(double a, double b) {
		if (b < 0) {
			return 0;
		}
		if (a < 0) {
			if (a + b > 0) {
				return 0;
			} else {
				return a + b;
			}
		} else if (a > 0) {
			if (a - b < 0) {
				return 0;
			} else {
				return a - b;
			}

		}
		return 0;
	}

	/**
	 * Finds the nearest face in the given yaw.
	 * 
	 * @param yaw
	 *            Direction of the face being searched
	 * @return
	 */
	public static BlockFace getFace(float yaw) {
		yaw = yaw % 360;

		if (yaw < 0)
			yaw += 360;
		yaw = Math.round(yaw / 45);

		switch ((int) yaw) {
		case 0:
			return BlockFace.SOUTH;
		case 1:
			return BlockFace.SOUTH_WEST;
		case 2:
			return BlockFace.WEST;
		case 3:
			return BlockFace.NORTH_WEST;
		case 4:
			return BlockFace.NORTH;
		case 5:
			return BlockFace.NORTH_EAST;
		case 6:
			return BlockFace.EAST;
		case 7:
			return BlockFace.SOUTH_EAST;
		case 8:
			return BlockFace.SOUTH;
		default:
			return BlockFace.SOUTH;

		}

	}
}
