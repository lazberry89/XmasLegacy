package xmaslegacy.RoleManagers.SecondaryRoleManager;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Roles.Unpromotable;
import xmaslegacy.Annotation.Roles;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.SkillEffectManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ItemBuilder;

@Roles(grade = 2)
public class Fighter extends AbstractSecondRole implements Unpromotable {
    private final @NotNull PartyManager pm;
    private final @NotNull SkillEffectManager sem;

    public Fighter() {
        super(SecondaryRoles.FIGHTER);
        this.pm = PartyManager.INSTANCE;
        this.sem = SkillEffectManager.INSTANCE;
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!(p.getTargetEntity(2, false) instanceof LivingEntity target)) {
            InfoUtils.error(p, "유효한 타겟이 없습니다.");
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        p.setCollidable(false);
        sem.hideEntity(p);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {p.setCollidable(true); sem.showEntity(p);}, 5L);

        p.getWorld().spawnParticle(Particle.ASH, p.getLocation(), 10, 0.5, 0.5, 0.5, 0.01);
        p.getWorld().playSound(p, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);

        sem.StunEntity(target.getUniqueId(), 30L);

        Vector vector = p.getLocation().getDirection();
        p.setVelocity(vector.multiply(3.0).setY(Math.min(vector.getY(), 1.5)));
        Location startLoc = p.getLocation().add(0, 1, 0);
        Vector dir = vector.clone().normalize();

        Vector vector1;
        if (Math.abs(dir.getY()) > 0.9) {
            vector1 = new Vector(1, 0, 0);
        } else {
            vector1 = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
        }

        double radius = 0.5;
        double spiralTightness = 3.5;
        vector1.multiply(radius);
        Particle.DustOptions option = new Particle.DustOptions(Color.BLUE, 1.1f);
        Particle.DustTransition trs = new Particle.DustTransition(Color.BLUE, Color.AQUA, 1.1f);

        for (double d = 0; d < 6.0; d += 0.15) {
            Location centerPoint = startLoc.clone().add(dir.clone().multiply(d));
            p.getWorld().spawnParticle(Particle.DUST, centerPoint, 1, 0, 0, 0, 0, option);
            centerPoint.getNearbyEntitiesByType(LivingEntity.class, 0.5, 0.5)
                    .stream()
                    .filter(s -> !pm.isParty(p.getUniqueId(), s.getUniqueId()))
                    .forEach(s -> s.damage(2.0, p));

            double radians = d * spiralTightness;
            Vector rotatedOffset = vector1.clone().rotateAroundAxis(dir, radians);

            Location spiralPoint = centerPoint.clone().add(rotatedOffset);

            p.getWorld().spawnParticle(Particle.DUST, spiralPoint, 1, 0, 0, 0, 0, trs);
        }

        p.setCooldown(tool, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!(p.getTargetEntity(2, false) instanceof LivingEntity target)) {
            InfoUtils.error(p, "유효한 타겟이 없습니다.");
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        Location centerLoc = p.getLocation().clone();
        p.getWorld().spawnParticle(Particle.EXPLOSION, centerLoc, 1, 0, 0, 0, 0);

        Vector meUp = new Vector(0.0f, 0.6f, 0.0f);
        Vector targetUp = new Vector(0.0f, 2.5f, 0.0f);
        p.setVelocity(meUp);
        p.swingMainHand();

        spawnExpandingShockwave(p, centerLoc);

        p.getWorld().playSound(centerLoc, Sound.ENTITY_BREEZE_JUMP, 1.5f, 0.5f);

        double damage = 12;

        UpperCutEffect(target.getLocation().clone().add(0, 1, 0));
        target.setVelocity(targetUp);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> target.damage(damage, p), 5L);
        p.setCooldown(tool, 30);
    }

    @Override
    public void useAdditional() {

    }

    private void spawnExpandingShockwave(Player p, Location center) {
        new org.bukkit.scheduler.BukkitRunnable() {
            double radius = 0.5;
            final double maxRadius = 4.5;
            final double expansionSpeed = 0.8;
            final Particle.DustTransition dustOptions = new Particle.DustTransition(Color.RED, Color.BLACK, 1.2f);

            @Override
            public void run() {
                if (radius > maxRadius) {
                    this.cancel();
                    return;
                }

                int particleCount = (int) (radius * 12);

                for (int i = 0; i < particleCount; i++) {
                    double angle = 2 * Math.PI * i / particleCount;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, 0.1, z);

                    p.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);

                    if (i % 3 == 0) {
                        p.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0.1, 0, 0.05);
                    }
                }
                radius += expansionSpeed;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    private void UpperCutEffect(Location loc) {
        var oraxen = OraxenItems.getItemById("haki_wave");
        if (oraxen == null) {
            getPlugin().getSLF4JLogger().error("Could not find Model \"haki_wave\"");
            return;
        }
        ItemStack wave = oraxen.build();
        ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, w -> {
            w.setItemStack(wave);
            w.setInterpolationDuration(4);
            w.setBrightness(new Display.Brightness(15, 15));
            Transformation trans = w.getTransformation();
            trans.getScale().set(1.0f, 1.0f, 1.0f);
            w.setTransformation(trans);
        });
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            if (!display.isValid()) return;

            Transformation targetTrans = display.getTransformation();
            targetTrans.getScale().set(7.0f, 1.0f, 7.0f);
            display.setInterpolationDelay(0);
            display.setTransformation(targetTrans);
        }, 2L);

        Bukkit.getScheduler().runTaskLater(getPlugin(), display::remove, 7L);
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HOE)
                .setName(ColorUtils.chat("&c&l복서의 글러브"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HELMET)
                .setName(ColorUtils.chat("&7&l복서의 투구"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .addAttribute(Attribute.ATTACK_SPEED, 0.01, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .build().clone();
    }

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return emblem.getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return emblem.getRangeEmblem();
	}

}
