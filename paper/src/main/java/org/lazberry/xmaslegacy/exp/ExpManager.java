package org.lazberry.xmaslegacy.exp;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.Tier;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class ExpManager {
    private final UserManager um;

    @Inject
    public ExpManager(UserManager um) {
        this.um = um;
    }

    public int getRequiredExpForNextLevel(int currentLevel) {
        return 100 + (currentLevel * 50);
    }

    public Tier getTierForLevel(int level) {
        if (level >= 80) return Tier.ETERNAL;
        if (level >= 70) return Tier.OVERLORD;
        if (level >= 60) return Tier.LEGENDARY;
        if (level >= 50) return Tier.MYTHIC;
        if (level >= 40) return Tier.HERO;
        if (level >= 30) return Tier.CELEBRITY;
        if (level >= 20) return Tier.NEIGHBOR;
        if (level >= 10) return Tier.USER;
        return Tier.VISITOR;
    }

    public boolean addExp(@Nullable Player player, int amount) {
        if (player == null) return false;

        User user = um.getUser(player.getUniqueId());
        boolean[] result = {false};

        OptionalUtils.ifNotNullOrElse(
                user,
                u -> {
                    u.addExp(amount);
                    checkLevelUp(player, u);
                    result[0] = true;
                },
                () -> logError(player.getName())
        );

        return result[0];
    }

    private void checkLevelUp(Player player, User user) {
        while (user.getExp() >= getRequiredExpForNextLevel(user.getLevel())) {
            int required = getRequiredExpForNextLevel(user.getLevel());
            user.addExp(-required);
            user.addLevel(1);

            Tier newTier = getTierForLevel(user.getLevel());
            if (user.getTier() != newTier) {
                user.setTier(newTier);
                user.addPrefix(newTier);

                InfoUtils.info(player, ColorUtils.chat("티어가 ").append(newTier.prefix()).append(ColorUtils.chat("로 상승했습니다!")));
                InfoUtils.info(player, "칭호도 함께 지급되었습니다. &6'/prefix inv'&f 로 확인해보세요!");
                player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                log.info("{} 님이 {} 티어로 승급했습니다!", player.getName(), newTier.name());
            }
        }
    }

    public RoleMastery getMasteryForRoleExp(double roleExp) {
        if (roleExp >= 200000) return RoleMastery.INFINITE;
        if (roleExp >= 100000) return RoleMastery.IMMORTAL;
        if (roleExp >= 60000) return RoleMastery.INSANE;
        if (roleExp >= 30000) return RoleMastery.EMERALD;
        if (roleExp >= 15000) return RoleMastery.CRYSTAL;
        if (roleExp >= 7000) return RoleMastery.GOLD;
        if (roleExp >= 3000) return RoleMastery.SILVER;
        if (roleExp >= 1000) return RoleMastery.BRONZE;
        return RoleMastery.BEGINNER;
    }

    public boolean addRoleExp(@Nullable Player player, int amount) {
        if (player == null) return false;

        User user = um.getUser(player.getUniqueId());
        boolean[] result = {false};

        OptionalUtils.ifNotNullOrElse(
                user,
                u -> {
                    if (!u.hasRole()) return;
                    u.addRoleExp(amount);
                    RoleMastery newMastery = getMasteryForRoleExp(u.getRoleExp());

                    if (u.getMastery() != newMastery) {
                        u.setMastery(newMastery);
                        u.addPrefix(newMastery);
                        InfoUtils.info(player, ColorUtils.chat("직업 마스터리가 ").append(newMastery.prefix()).append(ColorUtils.chat("로 상승했습니다!")));
                        InfoUtils.info(player, "칭호도 함께 지급되었습니다. &6'/prefix inv'&f 로 확인해보세요!");
                        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        log.info("{} 님의 직업 마스터리가 {} (으)로 상승했습니다!", player.getName(), newMastery.name());
                    }

                    addExp(player, 1);

                    result[0] = true;
                },
                () -> logError(player.getName())
        );

        return result[0];
    }

    @Contract(pure = true)
    public double calculateBuff(double origin, double percent) {
        return origin + (origin * percent);
    }

    private void logError(String name) {
        log.warn("User data not found for player: {}", name);
    }
}