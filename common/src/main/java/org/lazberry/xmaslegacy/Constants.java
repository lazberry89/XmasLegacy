package org.lazberry.xmaslegacy;

import java.util.List;

public class Constants {
	public static final long USER_SAVE_TASK_DURATION = 20 * 60 * 5;

	public static final int COOKIE_COUNT = 16;
	public static final long COOKIE_TIMER_MINUTE = 60L;

    public static final int TAX_RATE = 3;
    public static final int MAX_CURRENCY_STACK = 99;

	public static final int ID_LENGTH = 8;

	public static final int BAG_SIZE = 27;
	public static final int FREE_COOKIE_COUNT = 10;

	public static final int BASIC_MONEY_MOBILE = 5000;
	public static final int BASIC_MONEY_NORMAL = 1000;

	public static final int INQUIRY_COOLDOWN = 30;

	public static final String SELECT_BOOK = "magicbookopened_v2";

	public static final String TARGET_EMBLEM = "target_emblem";
	public static final String RANGE_EMBLEM = "range_emblem";

	public static final Double LEVEL1_MULTIPLIER = 1.10;
	public static final Double LEVEL2_MULTIPLIER = 1.20;
	public static final Double LEVEL3_MULTIPLIER = 1.35;
	public static final Double LEVEL4_MULTIPLIER = 1.50;
	public static final Double LEVEL5_MULTIPLIER = 1.65;
	public static final Double LEVEL6_MULTIPLIER = 1.95;
	public static final Double LEVEL7_MULTIPLIER = 2.30;
	public static final Double LEVEL8_MULTIPLIER = 2.70;
	public static final Double LEVEL9_MULTIPLIER = 3.10;
	public static final Double LEVEL10_MULTIPLIER = 3.50;
	public static final List<Double> ENCHANT_MULTIPLIERS = List.of(
			LEVEL1_MULTIPLIER, LEVEL2_MULTIPLIER, LEVEL3_MULTIPLIER, LEVEL4_MULTIPLIER, LEVEL5_MULTIPLIER,
			LEVEL6_MULTIPLIER, LEVEL7_MULTIPLIER, LEVEL8_MULTIPLIER, LEVEL9_MULTIPLIER, LEVEL10_MULTIPLIER
	);
	
	public static final int LEVEL1_NEEDED = 1;
	public static final int LEVEL2_NEEDED = 1;
	public static final int LEVEL3_NEEDED = 1;
	public static final int LEVEL4_NEEDED = 3;
	public static final int LEVEL5_NEEDED = 3;
	public static final int LEVEL6_NEEDED = 7;
	public static final int LEVEL7_NEEDED = 7;
	public static final int LEVEL8_NEEDED = 15;
	public static final int LEVEL9_NEEDED = 30;
	public static final List<Integer> ENCHANT_NEEDED = List.of(
			LEVEL1_NEEDED, LEVEL2_NEEDED, LEVEL3_NEEDED, LEVEL4_NEEDED, LEVEL5_NEEDED,
			LEVEL6_NEEDED, LEVEL7_NEEDED, LEVEL8_NEEDED, LEVEL9_NEEDED
	);
	public static final String regionKey = "region";
}
