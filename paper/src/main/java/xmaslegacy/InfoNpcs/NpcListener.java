package xmaslegacy.InfoNpcs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.XmasLegacy;

@Listeners
public class NpcListener implements Listener {
    private final @NotNull NpcManager ncm;
    private final @NotNull XmasLegacy plugin;

    public NpcListener() {
        this.ncm = NpcManager.INSTANCE;
        this.plugin = XmasLegacy.getInstance();
    }

    @EventHandler
    public void onNpcClick(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof LivingEntity le)) return;
        var container = le.getPersistentDataContainer();
        if (!container.has(AbstractNpc.key())) return;

        NpcType value;
        String containerValue = "main";
        try {
            containerValue = container.get(AbstractNpc.key(), PersistentDataType.STRING);
            value = NpcType.valueOf(containerValue);
        } catch (IllegalArgumentException ex) {
            plugin.getSLF4JLogger().error("Cannot find value {}. Should register instance to NpcManager.java", containerValue);
            return;
        }

        ncm.getNpcInstance(value).sendCaption(p);
    }
}
