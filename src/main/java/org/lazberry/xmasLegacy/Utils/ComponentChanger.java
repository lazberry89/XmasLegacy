package org.lazberry.xmasLegacy.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentChanger {
	public static Component comp(String legacyText) {
		// &를 색상 코드로 인식해서 Component로 바꿔줌
		return LegacyComponentSerializer.legacyAmpersand().deserialize(legacyText);
	}
}
