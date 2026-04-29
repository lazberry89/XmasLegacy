package xmasLegacy.FirstRoleManager.Gatherer;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class Gatherer extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.ETERNAL_POSE);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Gatherer(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	private ItemStack CompassBuilder(Block target, Player p) {
		ItemStack compass = ItemBuilder.of(getPlugin(), Material.COMPASS)
				.setName(ColorUtils.chat(String.format("&6&l%s의 이터널포스", p.getName())))
				.setLore(ColorUtils.chat("&7제멋대로인 포스의 위치를 알려줍니다."))
				.setTag("pose", p.getName())
				.build();
		CompassMeta meta = (CompassMeta) compass.getItemMeta();
		meta.setLodestone(target.getLocation());
		meta.setLodestoneTracked(true);
		compass.setItemMeta(meta);
		return compass;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		Block pose = p.getTargetBlockExact(4);
		if (pose == null || pose.getType() != Material.SEA_LANTERN) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		p.getInventory().addItem(CompassBuilder(pose, p));

		Particle.DustTransition dust = new Particle.DustTransition(Color.AQUA, Color.WHITE, 1.5f);
		p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, p.getLocation(), 15, 0.5, 0.5, 0.5, 0.01, dust);
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
		p.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		Location loc = p.getLocation();
		if (tool == null || tool.getType() == Material.AIR) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 8)) return;

		Block block;
		p.getNearbyEntities(12, 12, 12).forEach(e -> {
			if (e instanceof LivingEntity le) {
				GlowUtils.setGlowColor(le, NamedTextColor.GOLD);
				Bukkit.getScheduler().runTaskLater(getPlugin(),t -> {
					if (le.isValid()) {
						GlowUtils.clearGlow(le);
					}
				}, 40L);
			}
		});
		for (int i = -6; i <= 6; i++) {
			for (int j = -6; j <= 6; j++) {
				for (int k = -6; k <= 6; k++) {
					block = loc.clone().add(i, j, k).getBlock();
					if (block.getState() instanceof Container) {
						BlockGlow(block);
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
					}
				}
			}
		}
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	private void BlockGlow(Block block) {
		Location loc = block.getLocation();
		loc.getWorld().spawn(loc, Shulker.class, s -> {
			s.setAI(false);
			s.setSilent(true);
			s.setInvulnerable(true);
			s.setInvisible(true);
			s.setCollidable(false);
			s.setPeek(0);
			GlowUtils.setGlowColor(s, NamedTextColor.GOLD);
			Bukkit.getScheduler().runTaskLater(getPlugin(), s::remove, 40L);
		});
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.GATHERER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.COMPASS)
				.setName(ColorUtils.chat("&6&l최후의 길잡이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setTag("role_id", "gatherer")
				.hideAllFlags()
				.addAttribute(Attribute.MOVEMENT_SPEED, 0.01, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_BOOTS)
				.setName(ColorUtils.chat("&6&l길잡이의 유물"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setTag("role_id", "gatherer")
				.hideAllFlags()
				.setUnbreakable()
				.addAttribute(Attribute.MOVEMENT_SPEED, 0.01, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET)
				.build().clone();
	}
}
