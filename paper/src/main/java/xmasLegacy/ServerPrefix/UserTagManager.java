package xmasLegacy.ServerPrefix;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.HiddenRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Roles.ThirdRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserTagManager {
    private static final Map<UUID, TextDisplay> tagMap = new HashMap<>();
    private static final Map<UUID, Interaction> baseMap = new HashMap<>();
    private static final XmasLegacy plugin = XmasLegacy.getInstance();
    private static final UserManager um = UserManager.INSTANCE;
    private static BukkitTask task;

    public static void runTask() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = um.getUser(player.getUniqueId());

                if (user != null) updateHoverTag(player, user);
            }
        }, 0L, 2L);
    }

    public static void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
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
            ServerPrefix prefix = user.getEquipPrefix();
            Component tierPrefix = prefix == null ? Tier.VISITOR.prefix() : prefix.prefix();

            Component finalStyle = ColorUtils.chat(UserTagManager.getColor(user.getRole()) + roleName + " &7| ")
                    .append(tierPrefix);

            display.text(finalStyle);
            display.setBillboard(Display.Billboard.CENTER);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setSeeThrough(false);

            display.setTransformation(new Transformation(
                    new org.joml.Vector3f(0.0f, 0.9f, 0.0f),
                    new org.joml.Quaternionf(),
                    new org.joml.Vector3f(1.0f, 0.9f, 1.0f),
                    new org.joml.Quaternionf()
            ));
        });
        player.addPassenger(base);
        base.addPassenger(textDisplay);

        baseMap.put(player.getUniqueId(), base);
        tagMap.put(player.getUniqueId(), textDisplay);
    }

    private static String getColor(Role role) {
        if (role instanceof SecondaryRoles) return "&6";
        if (role instanceof ThirdRoles) return "&c";
        if (role instanceof HiddenRoles) return "&d";
        else return "&a";
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
