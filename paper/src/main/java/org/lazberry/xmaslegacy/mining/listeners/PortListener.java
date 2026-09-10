package org.lazberry.xmaslegacy.mining.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.exp.ExpManager;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class PortListener implements Listener {
    private final SpawnRepository sr;
    private final MineManager mm;
    private final UserManager um;
    private final ExpManager em;
    private final XmasLegacy plugin;

    @Inject
    public PortListener(SpawnRepository sr, MineManager mm, UserManager um, ExpManager em, XmasLegacy plugin) {
        this.sr = sr;
        this.mm = mm;
        this.um = um;
        this.em = em;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void cancelBlockPlacing(BlockPlaceEvent e) {
        var player = e.getPlayer();
        if (player.getWorld().equals(mm.getWorld()) && !player.isOp()) {
            e.setCancelled(true);
            InfoUtils.error(player, "여기엔 블록을 설치할 수 없어요!");
        }
    }

    @EventHandler
    public void cancelBlockBreaking(BlockBreakEvent e) {
        var player = e.getPlayer();
        Block broken = e.getBlock();
        Location brokenLocation = broken.getLocation();
        if (mm.getWorld() == null || !player.getWorld().equals(mm.getWorld())) {
            return;
        }
        if (!mm.isInsideInternalField(brokenLocation) && !player.isOp()) {
            InfoUtils.error(player, "여기선 블록을 캘 수 없어요!");
            e.setCancelled(true);
            return;
        }
        if (mm.isBreakable(broken.getType()) && mm.isInsideInternalField(brokenLocation)) {
            Location loc = broken.getLocation();
            Bukkit.getScheduler().runTask(plugin, () ->
                loc.getBlock().setType(mm.randomOreByChance()));

            OptionalUtils.ifNotNullOrElse(um.getUser(player.getUniqueId()),
                    u -> em.addExp(player, 1), () -> {
                InfoUtils.error(player, "유저 정보가 로드되지 않아 경험치가 지급되지 않았습니다.");
                InfoUtils.warn(player, "강제로 로드를 시도합니다.");
                UserHandler.loadUser(player, false);
            });
        } else if (!player.isOp()) {
		    e.setCancelled(true);
	    }
    }

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		World w = e.getWorld();
		if (w.getName().equalsIgnoreCase("port")) {
			mm.setWorld(w);
			Bukkit.broadcast(ColorUtils.chat("World settings to port!"));
		}
	}

    @EventHandler
    public void whenDeadAndTeleport(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        Location loc = sr.get(DestinationType.PORT).getSpawn();
        if (loc == null) {
            InfoUtils.error(player, "스폰이 설정되지 않았습니다. 자동으로 문의됩니다.");
            player.performCommand("inquiry 항구 스폰설정이 되지 않았습니다.");
            return;
        }
        player.teleport(loc);
    }
}
