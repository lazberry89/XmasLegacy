package xmaslegacy.ServerPrefix;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ServerTransfer;

public class ChatPrefixListener implements Listener {
	private final @NotNull PrefixManager pfm;
	private final @NotNull RuleManager rm;
	private final @NotNull UserManager um;

	public ChatPrefixListener() {
		this.pfm = PrefixManager.INSTANCE;
		this.rm = RuleManager.INSTANCE;
		this.um = UserManager.INSTANCE;
	}

	@EventHandler
	@NonBlocking
	public void onChatPrefix(AsyncChatEvent e) {
		var p = e.getPlayer();
		String rawMsg = PlainTextComponentSerializer.plainText().serialize(e.message());
		String msg;
		if (rm.checkBadWords(rawMsg)) {
			msg = rm.hideBadWords(rawMsg);
			InfoUtils.infoMsg(InfoLevel.ERROR, p, "욕설이 포함된 메시지는 제재를 받을 수 있습니다.");
		} else {
			msg = rawMsg;
		}
		e.renderer((source, sourceDisplayName, message, viewer) -> {
			User user = um.getUser(source.getUniqueId());
			if (user == null) return Component.text()
					.append(sourceDisplayName)
					.append(Component.text(" : "))
					.append(Component.text(msg))
					.build();
			ServerPrefix prefix = user.getEquipPrefix();
			Component equipP = prefix == null ? ColorUtils.chat("") : prefix.prefix();
			return Component.text()
					.append(equipP)
					.append(Component.text(" "))
					.append(sourceDisplayName)
					.append(Component.text(" : "))
					.append(Component.text(msg))
					.build();
		});
	}

	@EventHandler
	public void PrefixGui(InventoryClickEvent e) {
		if (!(e.getInventory().getHolder() instanceof PrefixInterface pif)) return;
		var p = (Player) e.getWhoClicked();
		User user = um.getUser(p.getUniqueId());

		if (user == null) {
			p.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
			ServerTransfer.sendReloadNotice(p);
			return;
		}

		e.setCancelled(true);
		int slot = e.getRawSlot();

		switch (slot) {
			case 47 -> pif.prevPage(p);
			case 49 -> p.closeInventory();
			case 51 -> pif.nextPage(p);
			default -> {
				ServerPrefix prefix = pif.getPrefix(slot);
				if (prefix == null) return;
				if (!prefix.equals(user.getEquipPrefix())) {
					pfm.unequipPrefix(p);
					pfm.equipPrefix(p, prefix);
				} else {
					pfm.unequipPrefix(p);
				}
				pif.update(p);
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			}
		}
	}
}
