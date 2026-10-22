package org.lazberry.xmaslegacy.enchant.transcendence.ability.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.SonicHunter;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class SonicHunterListener implements Listener {
    private final SonicHunter sonicHunter;

    @Inject
    public SonicHunterListener(SonicHunter sonicHunter) {
        this.sonicHunter = sonicHunter;
    }

    @EventHandler
    public void onDamageWhenFeared(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        var weapon = attacker.getInventory().getItemInMainHand();
        if (sonicHunter.isAbilityWeapon(weapon) && sonicHunter.isFeared(victim)) {
            e.setDamage(e.getDamage() * 1.5);
        }
    }
}
