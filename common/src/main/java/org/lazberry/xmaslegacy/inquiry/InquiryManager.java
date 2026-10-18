package org.lazberry.xmaslegacy.inquiry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

@Registry.Include(type = ServerType.GLOBAL)
public class InquiryManager implements Initiator {
	private static final Logger log = LoggerFactory.getLogger(InquiryManager.class);
	private static final int INQUIRY_COOLDOWN_SECONDS = 30;

	private final SqlInquiryRepository inquiryRepository;

	@Inject
    public InquiryManager(SqlInquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

	@Override
	public void init() {
		warmUpCache();
	}

	public void setInquiryCooldown(UUID uuid) {
		JedisService.setWithExpire("uuid:" + uuid + ":inquiry-cooldown", "1", INQUIRY_COOLDOWN_SECONDS);
	}

	public long getInquiryCooldown(UUID uuid) {
		long ttl = JedisService.ttl("uuid:" + uuid + ":inquiry-cooldown");
		return ttl > 0 ? ttl : -1;
	}

	public boolean canInquire(UUID uuid) {
		return JedisService.getFromJedis("uuid:" + uuid + ":inquiry-cooldown") == null;
	}

	public void sendInquiry(String name, UUID uuid, String message) {
		inquiryRepository.saveInquiryAndGet(uuid, name, message).whenComplete((id, e) -> {
			if (e != null) {
				log.error("Failed to save inquiry for player {} (uuid: {})", name, uuid, e);
				return;
			}

			Inquiry inquiry = new Inquiry(id, uuid, name, message, System.currentTimeMillis());
			JedisService.setToJedis("id:" + id + ":pending-inquiry", inquiry.toJson());

			JedisService.sadd("inquiry:pending", String.valueOf(id));
			JedisService.publish("inquiry-channel", "NEW_INQUIRY:" + name + ":" + id);

			log.warn("player {} (uuid: {}) sent inquiry with contents '{}'", name, uuid, message);
			setInquiryCooldown(uuid);
		});
	}

	public void answerInquiry(long id, String answer) {
		Inquiry inquiry = getInquiry(id);
		if (inquiry == null || inquiry.isResolved()) return;

		inquiry.setAnswer(answer);
		inquiry.setStatus(InquiryStatus.RESOLVED);
		persist(inquiry);

		JedisService.publish("inquiry-channel", "ANSWERED:" + inquiry.getUniqueId() + ":" + id);
		JedisService.sadd("uuid:" + inquiry.getUniqueId() + ":unread-inquiries", String.valueOf(id));
	}

	public Set<String> getUnreadInquiryIds(UUID uuid) {
		String unreadKey = "uuid:" + uuid + ":unread-inquiries";
		return JedisService.smembers(unreadKey);
	}

	public Set<Inquiry> getUnreadInquiries(UUID uuid) {
		Set<String> unreadIds = getUnreadInquiryIds(uuid);
		Set<Inquiry> result = new HashSet<>();

		for (String idStr : unreadIds) {
			try {
				long id = Long.parseLong(idStr);
				Inquiry inquiry = getInquiry(id);
				if (inquiry != null) {
					result.add(inquiry);
				}
			} catch (NumberFormatException ignored) {}
		}
		return result;
	}

	public void setInquiryRead(UUID uuid, long id) {
		String unreadKey = "uuid:" + uuid + ":unread-inquiries";
		JedisService.srem(unreadKey, String.valueOf(id));
	}

	public void clearAllUnreadInquiries(UUID uuid) {
		String unreadKey = "uuid:" + uuid + ":unread-inquiries";
		JedisService.removeKey(unreadKey);
	}

	private void persist(Inquiry inquiry) {
		long id = inquiry.getId();
		String longId = String.valueOf(id);
		String json = inquiry.toJson();

		if (inquiry.getStatus() == InquiryStatus.PENDING) {
			JedisService.removeKey("id:" + id + ":resolved-inquiry");
			JedisService.srem("inquiry:resolved", longId);
			JedisService.setToJedis("id:" + id + ":pending-inquiry", json);
			JedisService.sadd("inquiry:pending", longId);
		} else {
			JedisService.removeKey("id:" + id + ":pending-inquiry");
			JedisService.srem("inquiry:pending", longId);
			JedisService.setToJedis("id:" + id + ":resolved-inquiry", json);
			JedisService.sadd("inquiry:resolved", longId);
		}

		inquiryRepository.updateInquiryResolution(id, inquiry.getAnswer(), inquiry.getStatus())
				.exceptionally(e -> {
					log.error("Failed to update inquiry(id: {}) with database.", id, e);
					return null;
				});
	}

	public @Nullable Inquiry getInquiry(long id) {
		var value = Inquiry.fromJson(JedisService.getFromJedis("id:" + id + ":pending-inquiry"));
		if (value != null) return value;
		else return Inquiry.fromJson(JedisService.getFromJedis("id:" + id + ":resolved-inquiry"));
	}

	public Component inquiryStateText(Inquiry inquiry) {
		boolean isResolved = inquiry.isResolved();
		String answer = inquiry.getAnswer();

		Component statusTag = isResolved
				? ColorUtils.chat("&a&l[답변됨]")
				: ColorUtils.chat("&c&l[보류중]");

		Component name = ColorUtils.chat("&r&7" + inquiry.getName() + " |")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&6uuid: &f" + inquiry.getUniqueId())));

		Component contents = ColorUtils.chat("&r&7\"" + inquiry.getInquiry() + "\"");

		Component answerHoverText;
		if (isResolved && answer != null && !answer.isBlank()) {
			answerHoverText = ColorUtils.chat("&b&l[ 관리자 답변 ]")
					.appendNewline()
					.append(ColorUtils.chat("&f" + answer));
		} else {
			answerHoverText = ColorUtils.chat("&7답변되지 않은 문의입니다.");
		}

		Component answerStatus = ColorUtils.chat("&7(답변 확인)")
				.hoverEvent(HoverEvent.showText(answerHoverText));

		// 출력 조합: [상태] [#ID] 플레이어 | "문의내용" (답변 확인)
		return Component.empty()
				.append(statusTag)
				.appendSpace()
				.append(ColorUtils.chat("&e#" + inquiry.getId()))
				.appendSpace()
				.append(name)
				.appendSpace()
				.append(contents)
				.appendSpace()
				.append(answerStatus);
	}

	public Set<Inquiry> getInquiries() {
		return getInquiriesByPredicate(i -> true);
	}

	public Set<Inquiry> getInquiries(UUID uuid) {
		return getInquiriesByPredicate(i -> Objects.equals(i.getUniqueId(), uuid));
	}

	private Set<Inquiry> getInquiriesByPredicate(Predicate<Inquiry> predicate) {
		Set<String> idStrings = new HashSet<>();
		idStrings.addAll(JedisService.smembers("inquiry:pending"));
		idStrings.addAll(JedisService.smembers("inquiry:resolved"));

		Set<Inquiry> inquiries = new TreeSet<>(Comparator.comparingLong(Inquiry::getId));

		for (String idStr : idStrings) {
			try {
				long id = Long.parseLong(idStr);
				Inquiry inquiry = getInquiry(id);
				if (inquiry != null && predicate.test(inquiry)) {
					inquiries.add(inquiry);
				}
			} catch (NumberFormatException ignored) {}
		}
		return inquiries;
	}

	public Component getInquiryHistoryText(UUID uuid) {
		Set<Inquiry> inquiries = getInquiries(uuid);
		if (inquiries.isEmpty()) {
			return ColorUtils.chat("&7등록된 문의 내역이 없습니다.");
		}

		Component result = Component.empty()
				.append(ColorUtils.chat("&e&l[ 문의 내역 목록 ]"))
				.appendNewline();

		for (Inquiry inquiry : inquiries) {
			long id = inquiry.getId();
			String message = inquiry.getInquiry();
			String answer = inquiry.getAnswer();
			boolean isResolved = inquiry.isResolved();

			Component header = isResolved
					? ColorUtils.chat("&a&l[답변됨] &r&e#" + id + " &f\"" + message + "\"")
					: ColorUtils.chat("&c&l[보류중] &r&e#" + id + " &f\"" + message + "\"");
			header = header.hoverEvent(HoverEvent.showText(ColorUtils.chat("&7" + inquiry.getName() + "의 문의")));

			result = result.append(header);

			if (isResolved && answer != null && !answer.isBlank()) {
				Component answerComponent = ColorUtils.chat("\n  &7└ &b&l답변: &f" + answer);
				result = result.append(answerComponent);
			}

			result = result.appendNewline();
		}

		return result;
	}

	private void warmUpCache() {
		inquiryRepository.getPendingInquiries().thenAccept(pendingList -> {
			for (Inquiry inquiry : pendingList) {
				String longId = String.valueOf(inquiry.getId());
				JedisService.setToJedis("id:" + longId + ":pending-inquiry", inquiry.toJson());
				JedisService.sadd("inquiry:pending", longId);
			}
			log.info("Successfully warmed up {} pending inquiries into Redis cache.", pendingList.size());
		});
	}
}

