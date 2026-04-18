package org.lazberry.xmasLegacy;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

public class ServerJoinManager implements Listener {
    private UserManager UM;

    public void setUM(UserManager UM) {
        this.UM = UM;
    }

    @EventHandler
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();

        UM.loadUser(p);

		if (!p.hasPlayedBefore()) {
			e.joinMessage(ComponentChanger.comp(Prefix.XmasLegacy + "&6&l " + p.getName() + "&f 님의 첫 접속입니다. 모두 환영해주세요!🎄"));
			p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
			p.spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);
            User user = new User(p, null);
            UM.addUser(p, user);
		} else {
			e.joinMessage(ComponentChanger.comp(Prefix.XmasLegacy + "&6&l " + p.getName() + "&f 님이 접속했어요!"));
			//p.playSound(p, "sound.christmas", 1.0f, 1.0f); //이후에 리소스팩에 전용 사운드 추가

		}
	}
    @EventHandler
    public void LeaveMsg(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        UM.saveAndRemoveUser(p);
    }
}
