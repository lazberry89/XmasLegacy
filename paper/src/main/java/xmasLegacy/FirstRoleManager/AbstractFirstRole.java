package xmasLegacy.FirstRoleManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.UsingEnergy;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("DuplicatedCode, unused")
public abstract class AbstractFirstRole implements UsingEnergy {
	private final Roles role;
    private final XmasLegacy plugin;

	public AbstractFirstRole(Roles role) {
		this.plugin = XmasLegacy.getInstance();
		this.role = role;
	}

    public XmasLegacy getPlugin() {
        return this.plugin;
    }

	public abstract void useFirstSkill(Player p);
	public abstract void useSecondSkill(Player p);
	public abstract @NotNull Roles getRole();
	public abstract @NotNull ItemStack roleWeapon();
    public abstract @NotNull ItemStack roleArmor();
	public abstract @NotNull ItemStack roleBook();

	public int getCooldown1() {
		return cooldown1;
	}
	public int getCooldown2() {
		return cooldown2;
	}
	public void loadCooldown(String path) {}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean consumeEnergy(Player player, int hungerCost) {
        int currentFood = player.getFoodLevel();

        if (currentFood < hungerCost) {
            player.sendMessage(ColorUtils.chat(Alert.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        player.setFoodLevel(Math.max(0, currentFood - hungerCost));

        return true;
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
