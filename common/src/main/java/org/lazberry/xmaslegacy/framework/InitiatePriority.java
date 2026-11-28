package org.lazberry.xmaslegacy.framework;

/**
 * Defines the initialization and shutdown priority of an {@link Initiator} bean.
 *
 * <p><b>Init order:</b> {@code FAST → AVERAGE → SLOW → DELAYED}<br>
 * <b>Close order:</b> {@code FAST → AVERAGE → SLOW → DELAYED} (by the priority on close())
 *
 * <p>Infrastructure (e.g. Redis) uses {@link #FAST} on init() and {@link #DELAYED} on close(),
 * so it is available while the other components start and shut down.
 */
public enum InitiatePriority {

	/**
	 * First to init; first to close if set on close().
	 * Use for low-level infrastructure: Redis, DB connection pools, etc.
	 */
	FAST,

	/**
	 * Default priority for most managers and services.
	 */
	AVERAGE,

	/**
	 * Runs after AVERAGE.
	 */
	SLOW,

	/**
	 * Last to init; last to close if set on close(), as Redis does.
	 * Put it on close() of infrastructure others still need while closing.
	 */
	DELAYED;

	public static final InitiatePriority DEFAULT = AVERAGE;
}
