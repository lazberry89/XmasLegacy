package xmasLegacy.FirstRoleManager.Priest;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Priest extends AbstractFirstRole {
    private final PartyManager PM;
    private final SkillEffectManager SEM;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.COMPACT_HEAL);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Priest() {
		super(Roles.PRIEST);
        this.PM = PartyManager.getInstance();
        this.SEM = SkillEffectManager.getInstance();
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, 3)) return;
		int duration = 5 * 20;
		int amplifier = 1;
		Entity entity = p.getTargetEntity(15, false);
        if (entity != null) {
            if (!(entity instanceof Player target) || !PM.isParty(p.getUniqueId(), target.getUniqueId())) {
                p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 타겟이 아닙니다!"));
                GlowUtils.setGlowColor(entity, NamedTextColor.RED);
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    if (entity.isValid()) {
                        GlowUtils.clearGlow(entity);
                    }
                }, 10L);
                return;
            }
            target.removePotionEffect(PotionEffectType.REGENERATION);
            GlowUtils.setGlowColor(target, NamedTextColor.GREEN);
            target.addPotionEffect(new PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION,
                    duration,
                    amplifier
            ));
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
            target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
            target.getWorld().playSound(p.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.0f);
            p.setCooldown(tool, this.getCooldown1() * 20);
        }
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
        int duration = 5 * 20;
        int amplifier = 1;

        Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.0f);
        SEM.drawCircularLine(p.getLocation().add(0, 0.2, 0),
                Particle.DUST, 7, false, 100, dust);
        for (Entity ally : p.getNearbyEntities(5, 5, 5)) {
            if (!(ally instanceof Player target)) continue;
            if (PM.isParty(p.getUniqueId(), target.getUniqueId())) {
                target.removePotionEffect(PotionEffectType.STRENGTH);
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH,
                        duration,
                        amplifier
                ));
                GlowUtils.setGlowColor(target, NamedTextColor.YELLOW);
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
            }
        }
        p.setCooldown(tool, this.getCooldown2() * 20);

	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.PRIEST;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_SPEAR)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "priest")
				.addAttribute(Attribute.ATTACK_DAMAGE, 5.0, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_CHESTPLATE)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(5.0, EquipmentSlotGroup.CHEST)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, 5.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setTag("role_id", "priest")
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
      		&0&l[ &e&l성직자 가이드 &0&l ]&r
      
      		&0성직자는 아군을 치유하고
      		&0신성한 축복을 내려 승리를
      		&0이끄는 &d&l헌신적인 조력자&r&0입니다.
      
      		&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &0주교, 수도사
      		&0- &83차 전직: &0SAINT
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&e&l▶ &0&l컴팩트 힐 &8[%d초]
      		&0성스러운 빛으로 아군의
      		&0&l생명력&r&0을 즉시 회복시킵니다.
      
      		&e&l▶ &0&l불꽃의 가호 &8[%d초]
      		&0주변 동료들에게 &c&l힘의 근원&r&0을
     		&0부여하여 전투력을 높여줍니다.      
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());
		
		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("성직자", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
