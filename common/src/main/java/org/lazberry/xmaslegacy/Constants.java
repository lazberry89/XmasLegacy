package org.lazberry.xmaslegacy;

import net.kyori.adventure.text.Component;

import java.util.List;

public class Constants {
	public static long USER_SAVE_TASK_DURATION = 20 * 60 * 5;

	//Consumable
	public static int COOKIE_COUNT = 16;
	public static long COOKIE_TIMER_MINUTE = 60L;

    //Economy
    public static int TAX_RATE = 3;
    public static int CURRENCY_MINIMUM = 100;
    public static int MAX_CURRENCY_STACK = 99;

    //Region
    public static int MINER_MINY = -100;
    public static int USER_MINY = 15;
    public static int INNER_RANGE = 5;
    public static int OUTER_RANGE = 10;
	public static int ID_LENGTH = 8;
	public static int MAX_HEIGHT = 320;

	//Bag
	public static int BAG_SIZE = 27;
	public static int FREE_COOKIE_COUNT = 10;

	//User
	public static int BASIC_MONEY_MOBILE = 5000;
	public static int BASIC_MONEY_NORMAL = 1000;

	//Inquiry
	public static int INQUIRY_COOLDOWN = 30;

    //Shop
    //-Priest
    public static int DRAGON_BREATH_PRICE = 200;
    public static int HEALER_POTION_PRICE = 900;
    public static int PROTECTION_POTION_PRICE = 1700;
    public static int SPEAR_POTION_PRICE = 2500;
    public static int DEATH_SAVER_PRICE = 7200;

	public static int DRAGON_POTION_DURATION = 5;
	public static int DRAGON_HEAL_AMPLIFIER = 0;
	public static int DRAGON_PROTECTION_AMPLIFIER = 0;
	public static int DRAGON_SATURATION_AMPLIFIER = 0;

	public static int HEALER_POTION_DURATION = 5;
	public static int HEALER_POTION_AMPLIFIER = 2;
	public static int PROTECTION_POTION_DURATION = 7;
	public static int DEATH_SAVER_DURATION = 2;

	//Merchant
	public static final Component PRICE_TITLE = ColorUtils.chat("&6&l상품 등록하기");
	public static final Component SHOP_TITLE = ColorUtils.chat("&6&l상인 상점");
	public static final Component PURCHASE_TITLE = ColorUtils.chat("&6&l구매 확인");

	//MagicBook
	public static final String SELECT_BOOK = "magicbookopened_v2";

	//Emblem
	public static final String TARGET_EMBLEM = "target_emblem";
	public static final String RANGE_EMBLEM = "range_emblem";

	//Enchant
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
