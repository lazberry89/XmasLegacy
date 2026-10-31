package org.lazberry.xmaslegacy.framework;

/**
 * Defines the initialization and shutdown priority of an {@link Initiator} bean.
 *
 * <p><b>Init order:</b> {@code FAST → AVERAGE → SLOW → DELAYED}<br>
 * <b>Close order:</b> {@code DELAYED → SLOW → AVERAGE → FAST}</b>
 *
 * <p>This ensures that infrastructure components (e.g., Redis, databases) with
 * {@link #FAST} priority are available when higher-level service components shut down.
 */
public enum InitiatePriority {

	/**
	 * Earliest to init, last to close.
	 * Use for low-level infrastructure: Redis, DB connection pools, etc.
	 */
	FAST,

	/**
	 * Default priority for most managers and services.
	 */
	AVERAGE,

	/**
	 * Initialized after AVERAGE; closed before AVERAGE.
	 */
	SLOW,

	/**
	 * Latest to init, first to close.
	 * Use for user-facing services that depend on infrastructure being alive.
	 */
	DELAYED;

	public static final InitiatePriority DEFAULT = AVERAGE;
}
