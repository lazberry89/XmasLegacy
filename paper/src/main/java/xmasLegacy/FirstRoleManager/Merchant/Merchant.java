package xmasLegacy.FirstRoleManager.Merchant;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class Merchant extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.OPEN_STOCKS);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	private final PriceInterface PIF;

	public Merchant(int c1, int c2, PriceInterface PIF, XmasLegacy plugin) {
		super(c1, c2, plugin);
		this.PIF = PIF;
	}

	@Override
	public void useFirstSkill(Player p) {

	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		p.openInventory(PIF.MerchantShop());
		PIF.setOwner(p.getUniqueId());
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MERCHANT;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.ENDER_CHEST)
				.setName(ColorUtils.chat("&d&l상인의 보자기"))
				.setLore(ColorUtils.chat("&7상점을 열거나 매입품을 확인할 수 있어요."))
				.hideAllFlags()
				.setTag("role_id", "merchant")
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.IRON_HELMET)
				.setName(ColorUtils.chat("&d&l상인대가리 보호막"))
				.setLore(ColorUtils.chat("&7상인한테 과연 이런게 필요할까?"), ColorUtils.chat("&7아 근데 스킬쓸려면 필요함 ㅇㅇ"))
				.hideAllFlags()
				.setTag("role_id", "merchant")
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		return null;
	}
}
