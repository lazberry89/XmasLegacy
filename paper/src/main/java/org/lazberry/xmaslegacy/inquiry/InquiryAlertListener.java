package org.lazberry.xmaslegacy.inquiry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.GLOBAL)
public class InquiryAlertListener implements Listener {
	private final InquiryManager inquiryManager;

	@Inject
	public InquiryAlertListener(InquiryManager inquiryManager) {
		this.inquiryManager = inquiryManager;
	}

	@EventHandler
	public void alertPlayerWhenHasInquiryAnswered(PlayerJoinEvent e) {
		var player = e.getPlayer();
		var uuid = player.getUniqueId();

		var inquiries = inquiryManager.getUnreadInquiries(uuid);
		int count = inquiries.size();

		if (count == 0) return;
		InfoUtils.info(player, "등록하신 문의&6({}개)&f가 답변되었습니다. &6'/문의함'&f을 사용하여 확인하세요.", count);
	}
}
