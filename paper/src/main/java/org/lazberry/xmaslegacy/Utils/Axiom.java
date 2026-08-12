package org.lazberry.xmaslegacy.Utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.ThreadLocalRandom;

public class Axiom {
	private static final String[] SUFFIXES = {"", "k", "M", "B", "T", "P", "E"};

	@Contract("-> fail")
	@ApiStatus.Internal
	private Axiom() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static float snapDegrees(float angle) {
		angle = (angle % 360 + 360) % 360;

		if (angle >= 315 || angle < 45) return 180f;
		else if (angle >= 45 && angle < 135) return -90f;
		else if (angle >= 135 && angle < 225) return 0f;
		else return 90f;
	}

	public static Location snapDegrees(Location location) {
		var loc = location.clone();
		float yaw = snapDegrees(loc.getYaw());
		float pitch = snapDegrees(loc.getPitch());
		loc.setYaw(yaw);
		loc.setPitch(pitch);

		return loc;
	}

	public static float normalizeYaw(float yaw) {
		yaw %= 360;
		if (yaw > 180) yaw -= 360;
		if (yaw < -180) yaw += 360;
		return yaw;
	}

	public static float getFacingYaw(float playerYaw) {
		return normalizeYaw(playerYaw + 180f);
	}

	public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
		return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
	}

	public static <C extends Comparable<C>> C clamp(C value, C min, C max) {
		if (value.compareTo(min) < 0) return min;
		if (value.compareTo(max) > 0) return max;
		return value;
	}

	public static double lerp(double start, double end, double alpha) {
		return start + (end - start) * clamp(alpha, 0.0, 1.0);
	}

	public static boolean isBetween(double value, double min, double max) {
		return value >= min && value <= max;
	}

	public static boolean chance(double probability) {
		return ThreadLocalRandom.current().nextDouble() < probability;
	}

	public static <V extends Number> int randomRange(V min, V max) {
		int minVal = min.intValue();
		int maxVal = max.intValue();

		if (minVal >= maxVal) return minVal;
		return ThreadLocalRandom.current().nextInt(minVal, maxVal + 1);
	}

	public static double randomRange(double min, double max) {
		if (min >= max) return min;
		return ThreadLocalRandom.current().nextDouble(min, max);
	}

	public static Location centerBlock(Location loc) {
		Location cloned = loc.clone();
		cloned.setX(loc.getBlockX() + 0.5);
		cloned.setZ(loc.getBlockZ() + 0.5);
		return cloned;
	}

	public static Location getOffsetLocation(Location base, double forward, double right, double up) {
		Location loc = base.clone();
		Vector dir = loc.getDirection().setY(0);

		if (dir.lengthSquared() == 0) {
			float rad = (float) Math.toRadians(loc.getYaw());
			dir = new Vector(-Math.sin(rad), 0, Math.cos(rad));
		} else {
			dir.normalize();
		}

		Vector rightDir = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

		loc.add(dir.multiply(forward));
		loc.add(rightDir.multiply(right));
		loc.add(0, up, 0);
		return loc;
	}

	public static double distanceHorizontal(Location loc1, Location loc2) {
		if (loc1.getWorld() != loc2.getWorld()) return Double.MAX_VALUE;
		double dx = loc1.getX() - loc2.getX();
		double dz = loc1.getZ() - loc2.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	public static boolean isWithinDistance(Location loc1, Location loc2, double maxDistance) {
		if (loc1.getWorld() != loc2.getWorld()) return false;
		return loc1.distanceSquared(loc2) <= (maxDistance * maxDistance);
	}

	public static Vector rotateAroundY(Vector vector, double angleDegrees) {
		double angle = Math.toRadians(angleDegrees);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		double x = vector.getX() * cos - vector.getZ() * sin;
		double z = vector.getX() * sin + vector.getZ() * cos;

		return vector.clone().setX(x).setZ(z);
	}

	public static float[] vectorToYawPitch(Vector dir) {
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();

		double xz = Math.sqrt(x * x + z * z);
		float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
		float pitch = (float) Math.toDegrees(-Math.atan2(y, xz));

		return new float[]{yaw, pitch};
	}

	public static int blockToChunk(int blockCoord) {
		return blockCoord >> 4;
	}

	public static double round(double value, int decimals) {
		if (decimals < 0) return value;
		double scale = Math.pow(10, decimals);
		return Math.round(value * scale) / scale;
	}

	public static String formatCompact(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) return "0";
		boolean negative = value < 0;
		double absValue = Math.abs(value);

		if (absValue < 1000) return String.format("%.0f", value);

		int index = (int) (Math.log10(absValue) / 3);
		index = Math.min(index, SUFFIXES.length - 1);

		double scaled = absValue / Math.pow(10, index * 3);
		return String.format("%s%.1f%s", negative ? "-" : "", scaled, SUFFIXES[index]);
	}

	public static int parseIntOrDefault(String input, int defaultValue) {
		if (input == null || input.isBlank()) return defaultValue;
		try {
			return Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static double parseDoubleOrDefault(String input, double defaultValue) {
		if (input == null || input.isBlank()) return defaultValue;
		try {
			return Double.parseDouble(input.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static boolean isInBoundingBox(Location target, Location loc1, Location loc2) {
		if (target.getWorld() != loc1.getWorld() || target.getWorld() != loc2.getWorld()) return false;

		double minX = Math.min(loc1.getX(), loc2.getX());
		double maxX = Math.max(loc1.getX(), loc2.getX());
		double minY = Math.min(loc1.getY(), loc2.getY());
		double maxY = Math.max(loc1.getY(), loc2.getY());
		double minZ = Math.min(loc1.getZ(), loc2.getZ());
		double maxZ = Math.max(loc1.getZ(), loc2.getZ());

		return target.getX() >= minX && target.getX() <= maxX &&
				target.getY() >= minY && target.getY() <= maxY &&
				target.getZ() >= minZ && target.getZ() <= maxZ;
	}

	public static long secondsToTicks(double seconds) {
		return (long) (seconds * 20.0);
	}

	public static long millisToTicks(long millis) {
		return millis / 50L;
	}

	public static String formatTicksToMMSS(long ticks) {
		long totalSeconds = ticks / 20;
		long minutes = totalSeconds / 60;
		long seconds = totalSeconds % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	public static double yawToRadian(float yaw) {
		return Math.toRadians(-yaw - 90);
	}
}