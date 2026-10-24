package org.lazberry.xmaslegacy.enchant.transcendence.ability.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.logic.SonicHunter;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class SonicHunterListener implements Listener {
    private final SonicHunter sonicHunter;

    @Inject
    public SonicHunterListener(AbilityManager abilityManager) {
	    this.sonicHunter = abilityManager.getAbility(AbilityType.SONIC_HUNTER);
    }

    @EventHandler
    public void onDamageWhenFeared(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        var weapon = attacker.getInventory().getItemInMainHand();
        if (Ability.isAbilityWeapon(weapon, AbilityType.SONIC_HUNTER)
		        && sonicHunter.isFeared(victim)) {
            e.setDamage(e.getDamage() * 1.5);
        }
    }
}
