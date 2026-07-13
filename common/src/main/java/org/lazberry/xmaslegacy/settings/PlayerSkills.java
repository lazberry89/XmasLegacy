package org.lazberry.xmaslegacy.settings;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum PlayerSkills {
    SHOCK_DART(BasicSkills.SHOCK_DART),
    BACK_DASH(BasicSkills.BACK_DASH),
    SHARP_SWEEPING(BasicSkills.SHARP_SWEEPING),
    TAUNT(BasicSkills.TAUNT),
    DAGGER_RUSH(BasicSkills.DAGGER_RUSH),
    SMOKE(BasicSkills.SMOKE),
    BLOOD_FRENZY(BasicSkills.BLOOD_FRENZY),
    TOMAHAWK(BasicSkills.TOMAHAWK),
    COMPACT_POINT(BasicSkills.COMPACT_POINT),
    GRAVITY(BasicSkills.GRAVITY),
    COMPACT_HEAL(BasicSkills.COMPACT_HEAL),
    STEROID(BasicSkills.STEROID),
    RADIUS_HARVEST(BasicSkills.RADIUS_HARVEST),
    SPEED_GROWER(BasicSkills.SPEED_GROWER),
    CHAIN_MINING(BasicSkills.CHAIN_MINING),
    ORE_EYE(BasicSkills.ORE_EYE),
    ETERNAL_POSE(BasicSkills.ETERNAL_POSE),
    TRUTH_EYE(BasicSkills.TRUTH_EYE),
    OPEN_STOCKS(BasicSkills.OPEN_STOCKS),
    SELL_ITEMS(BasicSkills.SELL_ITEMS),
    FIX(BasicSkills.FIX),
    TEMP_BUFF(BasicSkills.TEMP_BUFF),

    SOUL_STEAL(SecondarySkillSet.SOUL_STEAL),
    KARMA(SecondarySkillSet.KARMA),
    TARGET_GUARD(SecondarySkillSet.TARGET_GUARD),
    OVERCHARGE_PRISM(SecondarySkillSet.OVERCHARGE_PRISM),
    ULTRA_MADNESS(SecondarySkillSet.ULTRA_MADNESS),
    TRIPLE_TOMAHAWK(SecondarySkillSet.TRIPLE_TOMAHAWK),
    COUNTER(SecondarySkillSet.COUNTER),
    FINISHER(SecondarySkillSet.FINISHER),
    SNIPE(SecondarySkillSet.SNIPE),
    MAGIC_BULLET(SecondarySkillSet.MAGIC_BULLET),
    FIRE_BULLET(SecondarySkillSet.FIRE_BULLET),
    PRISM_LASER(SecondarySkillSet.PRISM_LASER),
    CHAINING(SecondarySkillSet.CHAINING),
    SHOTGUN(SecondarySkillSet.SHOTGUN),
    CHAIN_GRAB(SecondarySkillSet.CHAIN_GRAB),
    MANA_ORB(SecondarySkillSet.MANA_ORB),
    GLACIAL_PRISON(SecondarySkillSet.GLACIAL_PRISON);

    private final @NotNull SkillSet origin;

    PlayerSkills(@NotNull SkillSet origin) {
        this.origin = origin;
    }

    /**
     * PlayerSkills enum only defines what skill actually exists. Not divided by Tree structure.
     * @return origin value of skill enum which is divided by the rule of Tree Structure.
     */
    @Contract(value = "-> !null", pure = true)
    public @NotNull SkillSet origin() {
        return this.origin;
    }
}
