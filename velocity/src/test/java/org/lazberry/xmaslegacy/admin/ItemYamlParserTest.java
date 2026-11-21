package org.lazberry.xmaslegacy.admin;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemYamlParserTest {
	private static String b64(String yaml) {
		return Base64.getEncoder().encodeToString(yaml.getBytes(StandardCharsets.UTF_8));
	}

	private static final String BREAD = """
			- ==: org.bukkit.inventory.ItemStack
			  DataVersion: 4671
			  id: minecraft:bread
			  count: 1
			  components:
			    minecraft:item_model: '"oraxen:sunflower_bread"'
			    minecraft:lore: '[{color:"gray",text:"농부만이 만들 수 있는 이 세상을 살아갈 식량입니다."}]'
			    minecraft:custom_name: '{bold:1b,color:"yellow",text:"태양초 빵"}'
			    minecraft:item_name: '{color:"gold",text:"sunflower_bread"}'
			  schema_version: 1
			""";

	@Test
	void parsesCustomNameLoreAndSlot() {
		List<ItemView> items = ItemYamlParser.parse(b64("items:\n" + BREAD + "- null\n- null\n"));
		assertEquals(1, items.size());
		ItemView it = items.get(0);
		assertEquals(0, it.slot());
		assertEquals("minecraft:bread", it.id());
		assertEquals(1, it.count());
		assertEquals("태양초 빵", it.name());
		assertEquals("yellow", it.nameColor());
		assertEquals(List.of("농부만이 만들 수 있는 이 세상을 살아갈 식량입니다."), it.lore());
		assertEquals("oraxen:sunflower_bread", it.model());
	}

	@Test
	void fallsBackToItemNameThenId() {
		String withItemName = """
				items:
				- id: minecraft:bread
				  count: 3
				  components:
				    minecraft:item_name: '{color:"gold",text:"sunflower_bread"}'
				""";
		assertEquals("sunflower_bread", ItemYamlParser.parse(b64(withItemName)).get(0).name());

		String bare = "items:\n- id: minecraft:bread\n  count: 3\n";
		ItemView it = ItemYamlParser.parse(b64(bare)).get(0);
		assertEquals("bread", it.name());
		assertEquals(3, it.count());
		assertTrue(it.lore().isEmpty());
	}

	@Test
	void readsEnchantments() {
		String yaml = """
				items:
				- id: minecraft:diamond_sword
				  count: 1
				  components:
				    minecraft:enchantments: '{"minecraft:sharpness":3}'
				""";
		assertEquals(List.of("sharpness 3"), ItemYamlParser.parse(b64(yaml)).get(0).enchants());
	}

	@Test
	void skipsNullSlotsAndKeepsIndex() {
		String yaml = "items:\n- null\n- id: minecraft:stone\n  count: 64\n";
		List<ItemView> items = ItemYamlParser.parse(b64(yaml));
		assertEquals(1, items.size());
		assertEquals(1, items.get(0).slot());
	}

	@Test
	void brokenBase64GivesSingleUnparsedItem() {
		List<ItemView> items = ItemYamlParser.parse("!!notbase64");
		assertEquals(1, items.size());
		assertEquals("해석 불가", items.get(0).name());
	}

	@Test
	void nullOrEmptyInputGivesNoItems() {
		assertTrue(ItemYamlParser.parse(null).isEmpty());
		assertTrue(ItemYamlParser.parse("").isEmpty());
	}
}
