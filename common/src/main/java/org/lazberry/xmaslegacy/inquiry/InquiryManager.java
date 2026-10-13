package org.lazberry.xmaslegacy.inquiry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Registry.Include(type = ServerType.GLOBAL)
public class InquiryManager {
	private static final Logger log = LoggerFactory.getLogger(InquiryManager.class);
	private static final int INQUIRY_COOLDOWN_SECONDS = 30;

	private final SqlInquiryRepository inquiryRepository;

	@Inject
    public InquiryManager(SqlInquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

	public long getInquiryCooldown(UUID uuid) {
		String stringValue = JedisService.getFromJedis("uuid:" + uuid + ":inquiry-cooldown");
		if (stringValue == null) return -1;
		long resultValue;
		try {
			resultValue = Long.parseLong(stringValue);
		} catch (NumberFormatException e) {
			return -1;
		}
		return resultValue;
	}

	public void setInquiryCooldown(UUID uuid) {
		JedisService.setWithExpire("uuid:" + uuid + ":inquiry-cooldown", "1", INQUIRY_COOLDOWN_SECONDS);
	}

	public boolean canInquire(UUID uuid) {
		return JedisService.getFromJedis("uuid:" + uuid + ":inquiry-cooldown") == null;
	}

	public void sendInquiry(String name, UUID uuid, String message) {
		long id = inquiryRepository.saveInquiryAndGet(uuid, name, message);

		Inquiry inquiry = new Inquiry(id, uuid, name, message, System.currentTimeMillis());
		JedisService.setToJedis("id:" + id + ":pending-inquiry", inquiry.toJson());
		JedisService.sadd("inquiry:pending", String.valueOf(id));

		log.warn("player {} (uuid: {}) sent inquiry with contents '{}'", name, uuid, message);
	}

	public void answerInquiry(long id, String answer) {
		Inquiry inquiry = getInquiry(id);
		if (inquiry == null) return;

		inquiry.setAnswer(answer);
		setInquiryState(id, InquiryStatus.RESOLVED);
	}

	public void setInquiryState(long id, InquiryStatus status) {
		Inquiry inquiry = getInquiry(id);
		if (inquiry == null) return;

		inquiry.setStatus(status);
		String longId = String.valueOf(id);
		String json = inquiry.toJson();

		if (status == InquiryStatus.PENDING) {
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
	}

	public @Nullable Inquiry getInquiry(long id) {
		var value = Inquiry.fromJson(JedisService.getFromJedis("id:" + id + ":pending-inquiry"));
		if (value != null) return value;
		else return Inquiry.fromJson(JedisService.getFromJedis("id:" + id + ":resolved-inquiry"));
	}

	public Component inquiryStateText(Inquiry inquiry) {
		Component name = ColorUtils.chat(inquiry.getName())
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&6uuid: " + inquiry.getUniqueId())));
		Component contents = ColorUtils.chat(ColorUtils.GRAY + "\"" + inquiry.getInquiry() + "\"")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&7문의내용입니다.")));
		Component pending = ColorUtils.chat(" &c&l[보류중]")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&7아직 해결되지 않은 문의입니다.")
						.appendNewline().append(ColorUtils.chat("&7명령어를 통해 상태를 변경하거나 답변하세요."))));
		Component resolved = ColorUtils.chat(" &a&l[해결됨]")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&7답변이 완료되었거나 해결된 문의입니다.")
						.appendNewline().append(ColorUtils.chat("&724시간 뒤 혹은 서버 종료 시 만료됩니다."))));
		Component status = inquiry.getStatus() == InquiryStatus.PENDING ? pending :
				resolved;
		return Inquiry.icon.appendSpace().append(name).append(ColorUtils.chat(" &e&l| "))
				.append(contents).append(status);
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

		Set<Inquiry> inquiries = new HashSet<>();

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
}

