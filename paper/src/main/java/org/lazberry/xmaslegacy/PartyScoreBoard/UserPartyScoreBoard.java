package org.lazberry.xmaslegacy.PartyScoreBoard;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Utils.BoardUtils;
import org.lazberry.xmaslegacy.Utils.UserHandler;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.List;
import java.util.Objects;

@Slf4j
@Inject
@Task(type = ServerType.GLOBAL)
public enum UserPartyScoreBoard implements Tasks {
	INSTANCE;

	private @Manager @NotNull PartyManager pm;
	private @Manager @NotNull UserManager um;
	private @Nullable BukkitTask task;

	UserPartyScoreBoard() {}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (this.task != null) return;
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
			Bukkit.getOnlinePlayers().stream()
					.filter(Objects::nonNull)
					.filter(Entity::isValid)
				.forEach(p -> {
				var user = um.getUser(p.getUniqueId());
				if (user == null) return;
				if (user.isShowBoard()) this.updateUserBoard(p);
			}), 0L, 10L);
	}

	@Override
	public void stopTask() {
		if (this.task == null) return;
		this.task.cancel();
		this.task = null;
	}

	public void updateUserBoard(@NotNull Player player) {
		User user = um.getUser(player.getUniqueId());
		if (user == null) {
			UserHandler.sendReloadNotice(player);
			log.error("Tried to create user board with null User!");
			return;
		}
		var party = pm.getParty(player.getUniqueId());
		if (party == null) {
			BoardUtils.getOrCreate(player, ColorUtils.chat("&6&l" + player.getName()), b -> {
				b.setLine(1, ColorUtils.chat("&6&l소지금&f " + user.getDollars() + " &6$"));
				b.setLine(2, ColorUtils.chat(""));
				b.setLine(3, ColorUtils.chat("&6&l경험치&f " + user.getExp() + " &6Ex"));
				b.setLine(4, ColorUtils.chat(""));
				var role = user.getRole();
				b.setLine(5, ColorUtils.chat("&6&l직업&f " + role.getKor() + ", &c" + role.getTier() + " TIER"));
				b.setLine(6, ColorUtils.chat(""));
				b.setLine(7, ColorUtils.chat("&6&l직업수치&f " + user.getRoleExp() + " &6Rxp"));
				b.setLine(8, ColorUtils.chat(""));
				b.setLine(9, IceLogo().appendSpace().append(ColorUtils.chat("&b빙결수치")));
				b.setLine(10, IceLogo().appendSpace().append(iceStateBar(user)));
				b.setLine(11, ColorUtils.chat("&7&l------------"));

				for (int i = 12; i < 15; i++) {
					b.removeLine(i);
				}
			});
		} else {
			List<User> partyUsers = party.getMembers();
			BoardUtils.getOrCreate(player, ColorUtils.chat("&6&l[ PARTY ]"), b -> {
				int line = 1;

				for (User member : partyUsers) {
					if (line > 14) break;
					Player memberPlayer = Bukkit.getPlayer(member.getUniqueId());
					String nameStr = (memberPlayer != null) ? memberPlayer.getName() : member.getName();

					String leaderPrefix = "";
					if (pm.isLeader(member.getUniqueId())) leaderPrefix = "&7&l[&6★&7&l]";
					b.setLine(line++, ColorUtils.chat(leaderPrefix + "&e" + nameStr));

					if (memberPlayer != null && memberPlayer.isOnline()) b.setLine(line++, healthBar(memberPlayer));
					else b.setLine(line++, ColorUtils.chat("&7&l[&c오프라인&7&l]"));

					if (line <= 14) b.setLine(line++, ColorUtils.chat(" "));
				}
				for (int i = line; i <= 14; i++) {
					b.removeLine(i);
				}
			});
		}
	}

	private @NotNull Component IceLogo() {
		return ColorUtils.chat("&7&l[&b❄&7&l]");
	}
	private @NotNull Component HealthLogo() {
		return ColorUtils.chat("&7&l[&c♥&7&l]");
	}

	private @NotNull Component healthBar(@NotNull Player player) {
		double health = player.getHealth();
		var maxHealthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
		double maxHealth = (maxHealthAttribute != null) ? maxHealthAttribute.getValue() : 20.0;

		int filledCount = Math.clamp((int) ((health / maxHealth) * 10), 0, 10);
		int emptyCount = 10 - filledCount;

		StringBuilder bar = new StringBuilder("&7&l[&r");

		if (filledCount > 0) {
			bar.append("&c");
			bar.repeat("■", filledCount);
		}

		if (emptyCount > 0) {
			bar.append("&7");
			bar.repeat("■", emptyCount);
		}
		bar.append("&7&l]");
		return ColorUtils.chat(bar.toString());
	}

	private @NotNull Component iceStateBar(@NotNull User user) {
		int icing = user.getIcingState();

		int filledCount = Math.clamp(icing / 10, 0, 10);
		int emptyCount = 10 - filledCount;

		StringBuilder bar = new StringBuilder("&7&l[&r");

		if (filledCount > 0) {
			bar.append("&b");
			bar.repeat("■", filledCount);
		}

		if (emptyCount > 0) {
			bar.append("&7");
			bar.repeat("■", emptyCount);
		}
		bar.append("&7&l]");
		return ColorUtils.chat(bar.toString());
	}
}
