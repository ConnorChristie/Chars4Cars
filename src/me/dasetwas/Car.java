package me.dasetwas;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import me.dasetwas.event.CarUpdateEvent;

/**
 * Representation of a car
 * 
 * @author DasEtwas
 *
 */
public class Car {

	// Minecraft
	/**
	 * The UUID of the owner of the car.
	 */
	UUID owner;
	/**
	 * The current passenger of the minecart, null when empty.
	 */
	Player passenger;
	/**
	 * The UUID of the car minecart Entity.
	 */
	UUID cockpitID;
	/**
	 * The Minecart of the car.
	 */
	Minecart car;
	/**
	 * The name of the car.
	 */
	String name;
	/**
	 * climbLoc - Location for the car to check wether it should jump or not.
	 * overLoc - Location for the car to check if it can jump or not by looking
	 * at the ceiling (if present). soundLoc - Location for the car to play
	 * various sounds.
	 */
	Location climbLoc, overLoc, soundLoc;
	/**
	 * x, y, z coordinates of the car minecart Entity.
	 */
	double x, y, z;
	double vx, vz, vy;
	/**
	 * Yaw and Pitch of the car minecart Entity.
	 */
	float yaw, pitch;
	/**
	 * Percentage of the movement key controls. Updated onPacketReceive. Front
	 * left is 1,1
	 */
	float forw = 0, side = 0;
	/**
	 * Scoreboard for displaying statistics.
	 */
	Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

	// API
	/**
	 * If the car should call an CarUpdateEvent everytime it gets updated.
	 */
	boolean notifyUpdate = false;

	// Physics
	/**
	 * Angle which to add to yaw every update.
	 */
	double steerAngle;
	/**
	 * Engine Revolutions per Minute and RPM from last Update.
	 */
	double engineRPM = 0, lastEngineRPM = 0;
	/**
	 * Max amount of EngineRPM.
	 */
	int maxEngineRPM = 6500;
	/**
	 * Mass of car.
	 */
	int mass;
	/**
	 * Brake and Throttle pedal percentage. 1 = 100%
	 */
	float brake = 0, throttle = 0;
	/**
	 * Acceleration of car contributed by the engine or brake. Seperate.
	 */
	double engineAcc, brakeAcc;
	/**
	 * If engine is running. (Not if engineRPM > 0, just if the RPM is held over
	 * 800)
	 */
	boolean engineRunning = true;
	/**
	 * Percentage of clutch friction. (Friction is 100%)
	 */
	double clutchPercent;
	/**
	 * Current speed and speed of last update. vy is ignored.
	 */
	double speed = 0, lastSpeed = 0;
	/**
	 * Current gear.
	 */
	int currentGear = 0;
	/**
	 * The engine power of the car in Newton-Meters (Torque). NOTE: NOT THE
	 * ACTUAL POWER (Torque * RPM)
	 */
	int enginePower; // Newton-meter
	/**
	 * The radius of the cars' wheel.
	 */
	double driveWheelRadius = 0.37;
	/**
	 * The circumference of the car's wheel.
	 */
	double driveWheelCircumference = driveWheelRadius * 2 * Math.PI;
	static double[] gearRatio = Cars.getGearRatios();
	static double[] engineTorques = Cars.getEngineTorques();
	static char[] gearNames = Cars.getGearNames();
	/**
	 * double differentialRatio = Chars4Cars.differentialRatio;
	 */
	double differentialRatio = Chars4Cars.differentialRatio;
	/**
	 * The currentGear's ratio.
	 */
	double currentGearRatio;
	/**
	 * If the car is locked.
	 */
	boolean isLocked;
	/**
	 * The fuel of the car.
	 */
	double fuel;
	/**
	 * G acceleration. 1G = 16m/s²
	 */
	double G, lastG;
	double hitBoxX = 0;
	double hitBoxZ = 0;
	/**
	 * Slip factor
	 */
	double slipFactor;
	/**
	 * Actual vector of the car's velocity.
	 */
	Vector rSpeed = new Vector(), lastRSpeed = rSpeed;
	int minGear = Cars.minGear, maxGear = Cars.maxGear;
	/**
	 * Maximum force of the tires before losing traction
	 */
	float tireSlipThreshold = Cars.tireSlipThreshold;
	float lastYaw;
	float sideAccel;

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
		this.x = spawnLocation.getX();
		this.y = spawnLocation.getY();
		this.z = spawnLocation.getZ();
		this.yaw = yaw;
		this.name = name;
		this.isLocked = false;
		this.fuel = fuel;

		car = (Minecart) spawnLocation.getWorld().spawnEntity(new Location(spawnLocation.getWorld(), x, y, z), EntityType.MINECART);

		car.setCustomName("§aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		car.setMaxSpeed(1260);
		car.setDerailedVelocityMod(new Vector(1, 1, 1));
		car.setFlyingVelocityMod(new Vector(1, 1, 1));
		car.setSlowWhenEmpty(false);
		car.setDisplayBlock(new MaterialData(Material.BARRIER));

		car.teleport(new Location(spawnLocation.getWorld(), x, y, z, yaw - 90, 0));

		this.cockpitID = car.getUniqueId();
	}

	/**
	 * Car constructor for existing car entities
	 * 
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

		car.setCustomName("§aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		car.setMaxSpeed(1260);
		car.setDerailedVelocityMod(new Vector(1, 1, 1));
		car.setFlyingVelocityMod(new Vector(1, 1, 1));
		car.setSlowWhenEmpty(false);
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
		this.pitch = car.getLocation().getPitch() + 4;

		speed = Math.sqrt(vx * vx + vz * vz) * 20;
		if (currentGear == -1) {
			speed = -speed;
		}
		rSpeed = new Vector(this.vx * 20, this.vy * 20, this.vz * 20);

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
			engineRunning = true;

			if (!passenger.hasPermission("c4c.usecar")) {
				passenger.sendMessage(Chars4Cars.noPerm);
				passenger.leaveVehicle();
			}

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
		} else {
			brake = 0.1f;
			engineRunning = false;
			if (rSpeed.length() == 0) {
				currentGear = 0;
			}

			throttle = 0;
			passenger = null;
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
			if (passenger != null) {
				if (steerAngle > 0) {
					steerAngle = Math.max(0, steerAngle - 10);
				} else if (steerAngle < 0) {
					steerAngle = Math.min(0, steerAngle + 10);
				}
			}
		}

		if (isOnGround()) {
			// this.yaw = (float) (this.yaw + (-steerAngle *
			// (Math.min(Math.abs(rSpeed.length()) / 20, 1)) / Math.max(1,
			// rSpeed.length() / 5)) * (currentGear == -1 ? -1 : 1) * 0.6);
			this.yaw = (float) (this.yaw - (steerAngle * (currentGear == -1 ? -1 : 1) * 0.13));
		}

		// Set gearRatio to current gear's one.
		currentGearRatio = gearRatio[currentGear + Math.abs(minGear)];

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
				if (throttle * maxEngineRPM > engineRPM) {
					engineRPM = engineRPM + (d * 0.05);
				}
			}
			float throttleF = throttle;
			if (fuel < 0)
				throttleF = 0;

			engineRPM = engineRPM - Math.min(2, engineRPM * 0.006) * (1 - throttleF);

			// Keep engine over 800+ RPM
			if (engineRunning && engineRPM > 250 && !(brake == 1)) {
				engineRPM = Math.max(800 + Math.random() * 10, engineRPM);
			} else {
				// If engine cycle collapses, play shutoff sound
				if (engineRPM <= 250 && engineRPM != 0) {
					engineRPM = 0;
					car.getLocation().getWorld().playSound(car.getLocation(), Compat.BatTakeoff, 0.5f * Chars4Cars.volume, 0.5f);
				}
			}
			if (brake == 1) {
				engineRPM = subUntilZero(engineRPM, 87.56);
			}

			// Prevent engine from overshooting maxEngineRPM (rev limiter)
			engineRPM = Math.min(engineRPM, maxEngineRPM);
			brakeAcc = brake * 0.78;

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
			double combinedPercent = Math.min(1, Math.max(0, clutchPercent - slipFactor * 0.25));

			engineRPM = Math.abs((speed / driveWheelCircumference) * currentGearRatio * 60 * differentialRatio) * combinedPercent + (engineRPM + (d * 0.05)) * (1 - combinedPercent);

			// Get torque of engine
			if (engineRPM < 800) {
				engineRPM = 800 + Math.random() * 10;
			}

			engineAcc = getEngineTorque() * enginePower * throttle * combinedPercent * currentGearRatio * differentialRatio / driveWheelCircumference / (mass + fuel);
			brakeAcc = brake * 0.78;

			// Check if the RPM limiter has to kick in
			if (engineRPM > maxEngineRPM) {
				if (currentGear == -1) {
					speed = Math.min(speed, maxEngineRPM / currentGearRatio * driveWheelCircumference / differentialRatio / 60);
					engineAcc = 0;
				} else {
					speed = Math.min(speed, maxEngineRPM / currentGearRatio * driveWheelCircumference / differentialRatio / 60);
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
		G = Math.abs((new Vector(rSpeed.getX(), rSpeed.getY(), rSpeed.getZ()).subtract(lastRSpeed).length()) / 32 / (0.05 * Chars4Cars.updateDelta));

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

		if (Math.abs(this.vx) > Math.abs(this.vz) && !getLocation().add(0, 0, face.getModZ() * -1).getBlock().getType().isSolid()) {
			if (Math.abs(this.vz) < 0.5)
				hitBoxZ = this.vz;
		} else if (Math.abs(this.vz) > Math.abs(this.vx) && !getLocation().add(face.getModX() * -1, 0, 0).getBlock().getType().isSolid()) {
			if (Math.abs(this.vx) < 0.5)
				hitBoxX = this.vx;
		}

		climbLoc = car.getLocation().add(this.vx * Chars4Cars.climbBlockSearchFactor + hitBoxX, Math.max(0.1f, this.vy * -1), this.vz * Chars4Cars.climbBlockSearchFactor + hitBoxZ);
		// climbLoc.add(0 ,1 ,0) will change climbloc :P
		overLoc = new Location(climbLoc.getWorld(), climbLoc.getX(), climbLoc.getY() + 1, climbLoc.getZ());

		if (!Chars4Cars.climbBlocks) {
			// If car would be at climbLoc, if it would collide there
			if (AABB.hasClimbable(climbLoc) && Math.abs(speed) > 0.1) {
				// If car is NOT on a slab already
				if ((this.y - ((int) this.y)) > Math.min(0.47, Chars4Cars.slabJumpVel)) {
					if (!Cars.isSlab(climbLoc.getBlock().getType()) && Math.abs(speed) > 0.3 && !(AABB.hasSolid(overLoc))) {
						addJumpVel(Chars4Cars.slabJumpVel);
					}
				} else {
					if (AABB.hasSlab(climbLoc) && !AABB.hasSolid(overLoc)) {
						addJumpVel(Chars4Cars.slabJumpVel);
					} else if (!AABB.hasSolid(overLoc)) {
						addJumpVel(Chars4Cars.stairJumpVel);
					}
				}
			}
		} else {
			// If car would be at climbLoc, if it would collide there with
			// blocks in the climbBlockList
			if (AABB.hasClimbList(climbLoc) && Math.abs(speed) > 0.1) {
				// If car is NOT on a slab already
				if ((this.y - ((int) this.y)) > Math.min(0.47, Chars4Cars.slabJumpVel)) {
					if (!Cars.isSlab(climbLoc.getBlock().getType()) && Math.abs(speed) > 0.3 && !(AABB.hasSolid(overLoc))) {
						addJumpVel(Chars4Cars.slabJumpVel);
					}
				} else {
					if (AABB.hasSlab(climbLoc) && !AABB.hasSolid(overLoc)) {
						addJumpVel(Chars4Cars.slabJumpVel);
					} else if (!AABB.hasSolid(overLoc)) {
						addJumpVel(Chars4Cars.stairJumpVel);
					}
				}
			}
		}

		// *=*=*=* Settter Block
		soundLoc = new Location(car.getWorld(), this.x + this.vx, this.y + this.vy, this.z + this.vz);

		if (Chars4Cars.volume > 0) {
			if (Math.abs(lastEngineRPM - engineRPM) > 3 || (Math.floor(Math.random() * 3) == 0 && engineRPM > 1000)) {
				soundLoc.getWorld().playSound(soundLoc, Compat.MinecartRoll, (float) ((engineRPM / maxEngineRPM * 0.5) + 0.3f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
			}
			if (engineRPM > 0) {
				soundLoc.getWorld().playSound(soundLoc, Compat.HorseJump, (float) ((engineRPM / maxEngineRPM * 0.25) + 0.3f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
			}

			if (engineRPM > 3000 && enginePower >= 150) {
				soundLoc.getWorld().playSound(soundLoc, Compat.FireworkLaunch, (float) (((engineRPM / 3000) - 1f) * 0.45f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
			}
			if (engineRPM > 2000 && enginePower >= 200) {
				soundLoc.getWorld().playSound(soundLoc, Compat.Pop, (float) (((engineRPM / 3000) - 1f) * 0.78f) * Chars4Cars.volume, (float) (engineRPM / maxEngineRPM));
			}

			if (engineRPM > maxEngineRPM) {
				soundLoc.getWorld().playSound(soundLoc, Compat.ChestClose, 1.3f * Chars4Cars.volume, 1.8f);
			}

			if ((car.getWorld().getBlockAt(car.getLocation()).getType().equals(Material.WATER) || car.getWorld().getBlockAt(car.getLocation()).getType().equals(Material.STATIONARY_WATER)) && engineRunning) {
				engineRunning = false;
				engineRPM = 0;
				car.eject();
				soundLoc.getWorld().playSound(soundLoc, Compat.FireExtinguish, 1, 1);
			}
		}

		if (Cars.isRail(car.getWorld().getBlockAt(car.getLocation()).getType())) {
			Cars.dropCar(this, getLocation());
			car.eject();
			remove();
			Cars.CarMap.remove(cockpitID);
		}

		if (Chars4Cars.exhaustSmoke && engineRPM > 400) {
			Vector flyVec = new Vector(rotateScalar(0.2, yaw + 180).getX(), 0.06 + Math.random() / 2, rotateScalar(0.2, yaw + 180).getZ());
			Location exhaustLoc = new Location(car.getWorld(), this.x + rotateScalar(0.8, yaw + 180).getX() - this.vx * 7, this.y + 0.1, this.z + rotateScalar(0.8, yaw + 180).getZ() - this.vz * 7);
			ParticleEffect.SMOKE_NORMAL.display(flyVec, 0.4f, exhaustLoc, 60.0);
		}

		// Print information
		try {
			if (passenger != null && Chars4Cars.scoreBoard) {
				try {
					sb.getObjective("stats").unregister();
				} catch (Exception e) {
				}

				Objective stats = sb.registerNewObjective("stats", "dummy");
				stats.setDisplaySlot(DisplaySlot.SIDEBAR);
				stats.setDisplayName(ChatColor.DARK_GRAY + "--== " + ChatColor.GREEN + "Car" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + name + ChatColor.DARK_GRAY + " ==--");

				Score scThrottle = stats.getScore("Throttle: " + (int) Math.floor(throttle * 100) + "%");
				Score scBrake = stats.getScore("Brake: " + (int) Math.floor(brake * 100) + "%");
				Score scG = stats.getScore("G: " + (float) Math.floor((lastG + G) / 2 * 100) / 100 + "*32m/s²");
				Score scEngineRPM = stats.getScore("RPM: " + ((int) engineRPM));
				Score scSpeed = stats.getScore("Speed: " + ((int) (speed * 3.6)) + "Kb/h");
				Score scCurrentGear = stats.getScore("Gear: " + gearNames[currentGear + Math.abs(minGear)]);

				if (Chars4Cars.fuel) {
					Score scFuel = stats.getScore("Fuel: " + Math.floor(fuel * 100) / 100);
					scFuel.setScore(0);
				}

				scThrottle.setScore(5);
				scBrake.setScore(4);
				scG.setScore(3);
				scEngineRPM.setScore(2);
				scSpeed.setScore(1);
				scCurrentGear.setScore(0);

				passenger.setScoreboard(sb);

				if (Chars4Cars.fixRotation) {
					PacketContainer pc = Chars4Cars.protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
					pc.getBytes().write(0, (byte) (yaw * 256f / 360f));
					pc.getBytes().write(1, (byte) (pitch * 256f / 360f));
				}
			}
		} catch (Exception e) {
		}

		if (Math.abs(speed) > Chars4Cars.speedLimit / 3.6) {
			if (speed < 0) {
				speed = Chars4Cars.speedLimit * -1;
			} else {
				speed = Chars4Cars.speedLimit;
			}
		}

		// Calculate car's speed
		if (isOnGround()) {
			if (Math.abs(speed) > 0) {
				slipFactor = 0;
				sideAccel = (float) rotateScalar(speed, yaw).distance(rotateScalar(speed, lastYaw));

				if ((rSpeed.distance(lastRSpeed)) * mass > tireSlipThreshold) {
					slipFactor = 0.8;
				} else {
					slipFactor = brake * 0.09;
				}

				slipFactor += sideAccel * 0.06;

				if (mass * sideAccel > tireSlipThreshold * 2) {
					slipFactor = Math.max(slipFactor, 0.6);
				}

				slipFactor = Math.min(1, Math.max(0, slipFactor));

				Vector rSpeedCopy = new Vector();
				rSpeedCopy.copy(rSpeed);
				rSpeedCopy.multiply(slipFactor / 20);
				Vector preferedSpeed = rotateScalar(speed / 20 * (1 - slipFactor), yaw);
				Vector finalSpeed = rSpeedCopy.add(preferedSpeed);
				this.vx = finalSpeed.getX();
				this.vz = finalSpeed.getZ();
			} else {
				this.vx = 0;
				this.vz = 0;
			}
		}

		// Check if car is approaching rail
		if (Cars.isRail(climbLoc.getBlock().getType())) {
			// Stop the car
			this.vx = 0;
			this.vz = 0;
		}
		
		if (notifyUpdate) {
			CarUpdateEvent cue = new CarUpdateEvent(this);
			Bukkit.getServer().getPluginManager().callEvent(cue);
		}

		// Set car's speed
		car.setVelocity(new Vector(this.vx, this.vy, this.vz));

		// to know engineRPM's rate
		lastEngineRPM = engineRPM;
		// to know acceleration
		lastSpeed = speed;
		lastRSpeed = rSpeed;
		lastG = G;
		// to know side acceleration
		lastYaw = yaw;

		car.setCustomName("§aChars4Cars Car:" + name + ":" + enginePower + ":" + mass + ":" + owner + ":" + fuel);
		// *=*=*=*

		
	}

	/**
	 * Shift up
	 */
	public void shiftUp() {
		if (this.engineRPM > 0) {
			if (currentGear == -1 && rSpeed.length() != 0) {
				return;
			}
			currentGear++;
			if (this.currentGear > maxGear) {
				this.currentGear = maxGear;
			} else {
				clutchPercent = 0;
			}
		}
	}

	/**
	 * Shift down
	 */
	public void shiftDown() {
		if (this.engineRPM > 0) {
			if (currentGear == 0 && rSpeed.length() != 0) {
				return;
			}
			currentGear--;
			if (this.currentGear < minGear) {
				this.currentGear = minGear;
			} else {
				clutchPercent = 0;
			}
		}
	}

	/**
	 * @return UUID of minecart entity (called Cockpit; Minecart car)
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
		car.eject();
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
	 * @return Nearest BlockFace.
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

	/**
	 * @return Car minecart.
	 */
	public Minecart getCarMinecart() {
		return car;
	}

	/**
	 * @param yes
	 *            If the car should notify an update with an CarUpdateEvent.
	 */
	public void setUpdateNotify(boolean yes) {
		notifyUpdate = yes;
	}

	/**
	 * @return The Location of the car.
	 */
	public Location getLocation() {
		return new Location(car.getWorld(), this.x, this.y, this.z);
	}

	/**
	 * @return The velocity of the car.
	 */
	public Vector getVelocity() {
		return new Vector(this.vx, this.vy, this.vz);
	}

	/**
	 * @param vel
	 *            Sets this.vy to vel.
	 */
	public void addJumpVel(double vel) {
		this.vy = vel;
	}
}