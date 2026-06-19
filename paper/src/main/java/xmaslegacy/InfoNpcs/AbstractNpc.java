package xmaslegacy.InfoNpcs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Utils.ServerTransfer;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractNpc {
    protected final @NotNull Map<UUID, Integer> playerCaption = new HashMap<>();
    protected final @NotNull List<String> caption;
    private final @NotNull @Getter XmasLegacy plugin;
    private final @NotNull @Getter NamespacedKey key;
	private final @NotNull @Getter Component name;

    protected AbstractNpc(@NotNull List<String> cap, @NotNull Component name) {
        this.plugin = XmasLegacy.getInstance();
        this.key = plugin.getNamespacedKey("npc");
        this.caption = cap;
		this.name = name;
    }

    protected abstract @NotNull String next(@NotNull Player player);
    public void sendCaption(@NotNull Player player) {
	    var user = UserManager.INSTANCE.getUser(player.getUniqueId());
	    if (user == null) {
		    ServerTransfer.sendReloadNotice(player);
		    return;
	    }

	    player.playSound(player, Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
	    Component txt = this.name.append(ColorUtils.chat(" &f" + next(player)));
	    player.sendActionBar(txt);
	    if (user.isMobile()) player.sendMessage(txt);
    }
}
