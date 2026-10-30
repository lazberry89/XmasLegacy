package org.lazberry.xmaslegacy.sin;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class NightmareManager implements Initiator {
    private final XmasLegacy plugin;

	@Getter @Setter
	private String hiddenName = "&7&k###";

	@Inject
    public NightmareManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {}

	public void blackPlayer(Player player) {
		player.setInvisible(true);
		GlowUtils.glow(player, NamedTextColor.DARK_RED);
		hidePlayerName(player);
	}

	public void resetPlayer(Player player) {
		player.setInvisible(false);
		GlowUtils.clearGlow(player);
		showPlayerName(player);
	}

	private void hidePlayerName(Player player) {
		player.displayName(ColorUtils.chat(hiddenName));
		player.playerListName(ColorUtils.chat(hiddenName));
	}

	private void showPlayerName(Player player) {
		player.displayName(null);
		player.playerListName(null);
	}

    @Override
    public void close() {
    }
}
