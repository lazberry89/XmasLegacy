package xmasLegacy.FirstRoleManager;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Warrior extends AbstractFirstRole {
    private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
    public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.BLOOD_FRENZY);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Warrior(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
        if (!consumeEnergy(p, 3)) return;
		p.damage(8);
		p.getWorld().strikeLightningEffect(p.getLocation());
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

		for (Entity entity : p.getNearbyEntities(5, 5, 5)) {
			if (entity instanceof LivingEntity le && !le.equals(p)) {
				le.damage(4, p);
				le.teleport(le.getLocation().add(0, 0.1, 0));
				le.setVelocity(le.getVelocity().add(new Vector(0, 0.7, 0)));
			}
		}

		p.setCooldown(tool, getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat( Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, 3)) return;
		Location startLoc = p.getEyeLocation();
		final Vector direction = startLoc.getDirection().clone().normalize().multiply(1.0);
		final float playerYaw = p.getLocation().getYaw();

		ArmorStand axeStand = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setArms(true);
			stand.setBasePlate(false);
			stand.setMarker(true);
			GlowUtils.setGlowColor(stand, NamedTextColor.RED);

			Location loc = stand.getLocation();
			loc.setYaw(playerYaw);
			stand.teleport(loc);

			stand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
		});

		p.setCooldown(tool, this.getCooldown2() * 20);

		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = 40;

			@Override
			public void run() {
				// 종료 조건: 시간 초과 또는 아머스탠드 소멸
				if (ticks >= maxTicks || !axeStand.isValid()) {
					if (axeStand.isValid()) axeStand.remove();
					this.cancel();
					return;
				}

				Location currentLoc = axeStand.getLocation().add(direction);
				axeStand.teleport(currentLoc);
				axeStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 3, 0.05, 0.05, 0.05, 0.01);

				double rotation = ticks * 0.6;
				axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));

				for (Entity entity : axeStand.getNearbyEntities(1.2, 1.2, 1.2)) {
					if (entity instanceof LivingEntity target && !entity.equals(p)) {

						Location targetLoc = target.getLocation();
						Vector targetDir = targetLoc.getDirection().normalize();
						Location backLoc = targetLoc.clone().subtract(targetDir.multiply(1.5));

						if (backLoc.getBlock().getType().isSolid()) {
							backLoc.add(0, 1.0, 0);
						}

						p.teleport(backLoc);
						p.playSound(backLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
						target.damage(6.0, p);

						axeStand.remove();
						this.cancel();
						return;
					}
				}

				// 벽 충돌 시 제거
				if (currentLoc.getBlock().getType().isSolid()) {
					axeStand.remove();
					this.cancel();
					return;
				}

				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.WARRIOR;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_AXE)
				.setName(ColorUtils.chat("&8&l무거운 도끼"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "warrior")
				.build().clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
		        .setName(ColorUtils.chat("&8&l전사의 갑옷"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setUnbreakable()
		        .hideAllFlags()
		        .setArmorState(9, EquipmentSlotGroup.CHEST)
                .addAttribute(Attribute.SCALE, 0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
                .addAttribute(Attribute.MAX_HEALTH, 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
		        .setTag("role_id", "WarriorArmor")
		        .build().clone();
    }

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 야성적인 파괴력을 강조한 직업 설명
		String page1 = """
          &8&l[ WARRIOR ]
          
          &7전사는 압도적인 파괴력과
          강인한 생명력으로 전장의 최전선을
          유린하는 거침없는 투사입니다.
          
          &8&m-----------------
          &8&l[ &d&lADVANCE &8&l]&r
          &8- &72차: 버서커, 파이터
          &8- &73차: &8&o추후 공개 예정...
          """;

		// [페이지 2] 단순하고 강력한 스킬 설명
		String page2 = String.format("""
          &8&l[ &a&lSKILLS ]
          
          &8&l▶ &e&l토마호크 [%d초]
          &7도끼를 투척해 적중 시 대상의
          등 뒤로 소리 없이 도약합니다.
          
          &8&l▶ 프렌지 [%d초]
          &7자신의 혈기를 방출하여 주변의
          &7적들을 모두 공중으로 분쇄합니다.
          
          &8&m-----------------
          """, getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("전사", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
