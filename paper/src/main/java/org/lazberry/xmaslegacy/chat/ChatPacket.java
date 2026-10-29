package org.lazberry.xmaslegacy.chat;


import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.ColorUtils;

@ConsumableClass
public record ChatPacket(String sender, String server, Component message) {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Component.class, (JsonSerializer<Component>) (msg, type, context) ->
                new JsonPrimitive(GsonComponentSerializer.gson().serialize(msg)))
            .registerTypeAdapter(Component.class, (JsonDeserializer<Component>) (element, type, context) ->
                    GsonComponentSerializer.gson().deserialize(element.getAsString()))
            .create();

    @Override
    public Component message() {
        return message.hoverEvent(HoverEvent.showText(
                ColorUtils.chat("&7발송위치: " + server)));
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static ChatPacket fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, ChatPacket.class);
    }
}
