package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.List;

@Registry.Exclude(type = ServerType.LOBBY)
public class AdvancementBuilder {
	private final List<BaseAdvancement> list = new ArrayList<>();

	public BaseAdvancement register(String id, Material icon, String title, AdvancementFrameType frame,
	                                int x, int y, Advancement parent, String... description) {
		return register(id, new ItemStack(icon), title, frame, x, y, parent, description);
	}

	public BaseAdvancement register(String id, ItemStack icon, String title, AdvancementFrameType frame,
	                                int x, int y, Advancement parent, String... description) {
		AdvancementDisplay display = new AdvancementDisplay(
				icon, title, frame, true, false, x, y, description
		);
		BaseAdvancement base = new BaseAdvancement(id, display, parent);
		list.add(base);
		return base;
	}

	public void buildAndRegister(AdvancementTab tab, RootAdvancement root) {
		BaseAdvancement[] array = list.toArray(BaseAdvancement[]::new);
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		tab.registerAdvancements(root, array);
	}
}
