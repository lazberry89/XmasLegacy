package org.lazberry.xmaslegacy.admin;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads bag contents written by paper's {@code InventorySerializer} (Base64 of Bukkit item YAML)
 * without Bukkit. Text components inside are SNBT strings, so only the parts the page shows are pulled out.
 */
public final class ItemYamlParser {
	private static final Pattern TEXT = Pattern.compile("text:\"((?:[^\"\\\\]|\\\\.)*)\"");
	private static final Pattern COLOR = Pattern.compile("color:\"([^\"]*)\"");
	private static final Pattern ENCHANT = Pattern.compile("\"([a-z0-9_.:-]+)\":(\\d+)");

	private ItemYamlParser() {}

	public static List<ItemView> parse(String base64) {
		if (base64 == null || base64.isBlank()) return List.of();
		try {
			String yaml = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
			Object root = new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
			Object items = root instanceof Map<?, ?> m ? m.get("items") : null;
			if (!(items instanceof List<?> list)) return unparsed(base64);

			List<ItemView> out = new ArrayList<>();
			for (int slot = 0; slot < list.size(); slot++) {
				if (list.get(slot) instanceof Map<?, ?> item) out.add(view(slot, item));
			}
			return out;
		} catch (Exception e) {
			return unparsed(base64);
		}
	}

	private static ItemView view(int slot, Map<?, ?> item) {
		String id = String.valueOf(item.get("id"));
		int count = item.get("count") instanceof Number n ? n.intValue() : 1;
		Map<?, ?> c = item.get("components") instanceof Map<?, ?> m ? m : Map.of();

		String nameSnbt = str(c.get("minecraft:custom_name"));
		if (nameSnbt == null) nameSnbt = str(c.get("minecraft:item_name"));
		String name = nameSnbt == null ? null : joinTexts(nameSnbt);
		if (name == null || name.isEmpty()) name = id.substring(id.indexOf(':') + 1);
		String color = nameSnbt == null ? null : first(COLOR, nameSnbt);

		List<String> lore = new ArrayList<>();
		String loreSnbt = str(c.get("minecraft:lore"));
		if (loreSnbt != null) for (String line : topLevelElements(loreSnbt)) lore.add(joinTexts(line));

		List<String> enchants = new ArrayList<>();
		String ench = str(c.get("minecraft:enchantments"));
		if (ench != null) {
			Matcher m = ENCHANT.matcher(ench);
			while (m.find()) enchants.add(m.group(1).substring(m.group(1).indexOf(':') + 1) + " " + m.group(2));
		}

		String model = str(c.get("minecraft:item_model"));
		if (model != null) model = model.replace("\"", "");
		return new ItemView(slot, id, count, name, color, lore, enchants, model);
	}

	private static List<ItemView> unparsed(String raw) {
		String head = raw.length() > 120 ? raw.substring(0, 120) : raw;
		return List.of(new ItemView(-1, "unknown", 0, "해석 불가", "red", List.of(head), List.of(), null));
	}

	private static String str(Object o) {
		return o == null ? null : o.toString();
	}

	private static String first(Pattern p, String s) {
		Matcher m = p.matcher(s);
		return m.find() ? m.group(1) : null;
	}

	private static String joinTexts(String snbt) {
		StringBuilder sb = new StringBuilder();
		Matcher m = TEXT.matcher(snbt);
		while (m.find()) sb.append(m.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
		return sb.toString();
	}

	/**
	 * Splits an SNBT list {@code [{..},{..}]} into its compound elements, ignoring braces inside strings.
	 * shortcut: plain-string lore lines (no braces) are skipped; paper writes compounds today.
	 */
	private static List<String> topLevelElements(String list) {
		List<String> out = new ArrayList<>();
		int depth = 0, start = -1;
		boolean inString = false;
		for (int i = 0; i < list.length(); i++) {
			char ch = list.charAt(i);
			if (inString) {
				if (ch == '\\') i++;
				else if (ch == '"') inString = false;
				continue;
			}
			if (ch == '"') inString = true;
			else if (ch == '{' || ch == '[') {
				if (depth == 1 && start < 0) start = i;
				depth++;
			} else if (ch == '}' || ch == ']') {
				depth--;
				if (depth == 1 && start >= 0) {
					out.add(list.substring(start, i + 1));
					start = -1;
				}
			}
		}
		return out;
	}
}
