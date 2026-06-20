package xmaslegacy.InfoNpcs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	private final @NotNull Sound conversationSound;
	protected final @NotNull Map<UUID, Long> lastTalkTime = new HashMap<>();
	private static final long DIALOGUE_TIMEOUT = 20000L;

    public AbstractNpc(@NotNull List<String> cap, @NotNull Component name, @NotNull Sound conversationSound) {
        this.plugin = XmasLegacy.getInstance();
        this.key = plugin.getNamespacedKey("npc");
        this.caption = cap;
		this.name = name;
		this.conversationSound = conversationSound;
    }

	protected @NotNull String next(@NotNull Player player) {
		var uuid = player.getUniqueId();
		long currentTime = System.currentTimeMillis();

		if (this.lastTalkTime.containsKey(uuid)) {
			long lastTime = this.lastTalkTime.get(uuid);
			if (currentTime - lastTime > DIALOGUE_TIMEOUT) {
				this.playerCaption.put(uuid, 0);
			}
		}

		this.lastTalkTime.put(uuid, currentTime);

		int num = this.playerCaption.getOrDefault(uuid, 0);
		String currentCaption = this.caption.get(num);

		num++;

		if (num >= this.caption.size()) {
			num = 0;
			this.lastTalkTime.remove(uuid);
		}

		this.playerCaption.put(uuid, num);
		return currentCaption;
	}

    public void sendCaption(@NotNull Player player) {
	    @Nullable var user = UserManager.INSTANCE.getUser(player.getUniqueId());
	    if (user == null) {
		    ServerTransfer.sendReloadNotice(player);
		    return;
	    }

	    player.playSound(player, this.conversationSound, 1.0f, 1.0f);
	    Component txt = this.name.append(ColorUtils.chat(" &f" + next(player)));
	    player.sendActionBar(txt);
	    if (user.isMobile()) player.sendMessage(txt);
    }
}
