package xmasLegacy.SecondaryRoleManager;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import xmasLegacy.XmasLegacy;

public class BerserkerSpeedManager {
    private static final NamespacedKey SPEED_KEY = XmasLegacy.getInstance().getNamespacedKey("berserker_speed");

    public static void applyFlatSpeed(Player p) {
        AttributeInstance attribute = p.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attribute == null) return;

        removeFlatSpeed(p);

        AttributeModifier modifier = new AttributeModifier(SPEED_KEY, 0.07, AttributeModifier.Operation.ADD_NUMBER);
        attribute.addModifier(modifier);

        p.setWalkSpeed(0.34f);
    }

    /**
     * ❌ 속도 원상복구 (기본값으로 세팅)
     */
    public static void removeFlatSpeed(Player p) {
        AttributeInstance attribute = p.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_KEY);
        }
        p.setWalkSpeed(0.2f);
    }
}
