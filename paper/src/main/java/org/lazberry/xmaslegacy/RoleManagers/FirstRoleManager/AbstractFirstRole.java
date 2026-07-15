package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.Emblem;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.io.File;
import java.io.IOException;

@Slf4j
public abstract class AbstractFirstRole implements RoleClass {
    private final @Getter XmasLegacy plugin;
	private final @NotNull BasicRoles role;
	protected final @NotNull @Getter Emblem emblem;
    protected @Getter @Setter int cooldown1;
    protected @Getter @Setter int cooldown2;

	public AbstractFirstRole(@NotNull BasicRoles role) {
		this.plugin = XmasLegacy.getInstance();
		this.role = role;
		this.emblem = new Emblem(role);
	}

	public @NotNull BasicRoles getRole() {
		return this.role;
	}
	public abstract void useFirstSkill(@NonNull Player p);
	public abstract void useSecondSkill(@NonNull Player p);
	public abstract @NotNull ItemStack roleWeapon();
    public abstract @NotNull ItemStack roleArmor();
	public abstract @NotNull ItemStack roleBook();
	public @NotNull ItemStack TargetEmblem() {
		return this.emblem.getTargetEmblem();
	}
	public @NotNull ItemStack RangeEmblem() {
		return this.emblem.getRangeEmblem();
	}

    protected abstract void loadCustomStats(@NotNull FileConfiguration config);

	public void loadRoleData(@NotNull String path) {
		File roleFolder = new File(plugin.getDataFolder(), "roles");
		if (!roleFolder.exists()) {
			boolean mkdir = roleFolder.mkdirs();
			if (!mkdir) {
				log.error("Making Role folder Failed. Disabling plugin.");
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
			log.warn("Successfully created Role folder.");
		}

		File roleFile = new File(roleFolder, path + ".yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(roleFile);

		config.addDefault("cooldown.skill1", 4);
		config.addDefault("cooldown.skill2", 4);

		this.loadCustomStats(config);

		config.options().copyDefaults(true);
		try {
			config.save(roleFile);
		} catch (IOException e) {
			log.error("\uD83D\uDEA8 {}.yml 파일을 저장하는 중 오류가 발생했습니다.", path);
		}

		this.cooldown1 = config.getInt("cooldown.skill1");
		this.cooldown2 = config.getInt("cooldown.skill2");
	}

	@Contract(value = "null -> !null; !null -> !null", pure = true)
	protected @NotNull Component LinkComponentMaker(@Nullable String path) {
		if (path == null || path.trim().isEmpty()) {
			return ColorUtils.chat("&8&o-> 쇼케이스가 준비 중입니다 <-")
					.hoverEvent(HoverEvent.showText(ColorUtils.chat("&c관리자에게 문의해주세요.")));
		}

		return ColorUtils.chat("&8&n-> 쇼케이스 확인하기 <-")
				.clickEvent(ClickEvent.openUrl(path))
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&e클릭하여 영상을 시청하세요!")));
	}

	/**
	 * 여러 페이지를 가진 가이드북을 생성합니다.
	 * @param roleName 직업 이름
	 * @param showcaseUrl 쇼케이스 링크
	 * @param pages 각 페이지에 들어갈 내용들 (가변 인자)
	 */
	protected @NotNull ItemStack createGuideBook(String roleName, @Nullable String showcaseUrl, String... pages) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();

		if (bookMeta != null) {
			bookMeta.author(Component.text("System").color(net.kyori.adventure.text.format.NamedTextColor.RED));
			var mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
			bookMeta.title(mm.deserialize("<gray><bold>[ " + roleName + " 가이드 ]"));

			for (int i = 0; i < pages.length; i++) {
				Component pageComponent = ColorUtils.chat(pages[i]);
				if (i == pages.length - 1) {
					pageComponent = pageComponent.append(Component.newline())
							.append(LinkComponentMaker(showcaseUrl));
				}

				bookMeta.addPages(pageComponent);
			}

			book.setItemMeta(bookMeta);
		}
		return book;
	}
}
