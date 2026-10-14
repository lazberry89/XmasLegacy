package org.lazberry.xmaslegacy.inquiry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.UUID;

@Data
public class Inquiry {
    public static final Component icon = ColorUtils.chat("&#007AE1&l[&#219AE4&l문&#41B9E6&l의&#62D9E9&l]");
    public static Component alertMessage(String message) {
        return icon.appendSpace().append(ColorUtils.chat(message));
    }

    private static final Gson GSON = new GsonBuilder().create();

    private final long id;
    private final UUID uniqueId;
    private final String name;
    private final String inquiry;
    private final long createdAt;

    private InquiryStatus status = InquiryStatus.PENDING;
    private String answer = null;

    public Inquiry(long id, UUID uniqueId, String name, String inquiry, long createdAt) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.name = name;
        this.inquiry = inquiry;
        this.createdAt = createdAt;
    }

    public boolean isResolved() {
        return this.status == InquiryStatus.RESOLVED || (this.answer != null && !this.answer.isBlank());
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static Inquiry fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, Inquiry.class);
    }
}
