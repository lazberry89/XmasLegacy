package xmasLegacy.ServerPrefix;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserTagManager {
    private static final Map<UUID, TextDisplay> tagMap = new HashMap<>();
    private static final Map<UUID, Interaction> baseMap = new HashMap<>();
    private static BukkitTask task;

    public static void runTask() {

    }


    public static void createHoverTag(Player player, @Nullable User user) {
        if (user == null) return;
        removeHoverTag(player);

        Location loc = player.getLocation();

        Interaction base = loc.getWorld().spawn(loc, Interaction.class, interaction -> {
            interaction.setInteractionWidth(0.0f);
            interaction.setInteractionHeight(0.0f);
            interaction.setResponsive(false);
        });

        TextDisplay textDisplay = loc.getWorld().spawn(loc, TextDisplay.class, display -> {
            String roleName = user.getRole().getKor();
            Component tierPrefix = user.getTier().prefix();

            Component finalStyle = ColorUtils.chat("&a" + roleName + " &7| ")
                    .append(tierPrefix);

            display.text(finalStyle);
            display.setBillboard(Display.Billboard.CENTER);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setSeeThrough(false);

            display.setTransformation(new Transformation(
                    new org.joml.Vector3f(0.0f, 1.3f, 0.0f),
                    new org.joml.Quaternionf(),
                    new org.joml.Vector3f(1.0f, 1.0f, 1.0f),
                    new org.joml.Quaternionf()
            ));
        });
        player.addPassenger(base);
        base.addPassenger(textDisplay);

        baseMap.put(player.getUniqueId(), base);
        tagMap.put(player.getUniqueId(), textDisplay);
    }

    public static void updateHoverTag(Player player, @Nullable User user) {
        if (user == null) return;
        TextDisplay textDisplay = tagMap.get(player.getUniqueId());
        if (textDisplay != null && textDisplay.isValid()) {
            String roleName = user.getRole().getKor();
            Component finalStyle = ColorUtils.chat("&a" + roleName + " &7| ").append(user.getTier().prefix());

            textDisplay.text(finalStyle);
        } else {
            createHoverTag(player, user);
        }
    }

    public static void removeHoverTag(Player player) {
        UUID uuid = player.getUniqueId();

        if (tagMap.containsKey(uuid)) {
            TextDisplay display = tagMap.remove(uuid);
            if (display != null) display.remove();
        }

        if (baseMap.containsKey(uuid)) {
            Interaction base = baseMap.remove(uuid);
            if (base != null) base.remove();
        }
    }
}
