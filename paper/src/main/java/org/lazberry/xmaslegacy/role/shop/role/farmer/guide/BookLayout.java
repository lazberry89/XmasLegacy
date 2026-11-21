package org.lazberry.xmaslegacy.role.shop.role.farmer.guide;

import java.util.ArrayList;
import java.util.List;

/**
 * Lays text out for a written book so nothing is cut off: lines are wrapped by the client's pixel widths
 * and pages are split at 14 lines. Lines carry '&' colour codes, which take no width.
 */
public final class BookLayout {
	/** Usable text width of a book page is 114px; keep a little margin for width estimates. */
	static final int MAX_WIDTH = 110;
	static final int LINES_PER_PAGE = 14;

	public record Section(String title, List<String> paragraphs) {}

	private BookLayout() {}

	/** Pixel width of one character in the default font, including the 1px spacing. */
	static int charWidth(char c) {
		if (c > 0x7F) return 9; // Hangul and other wide glyphs (8px + spacing)
		return switch (c) {
			case ' ' -> 4;
			case 'i', '!', '.', ',', ':', ';', '|' -> 2;
			case 'l', '\'', '`' -> 3;
			case 't', 'I', '[', ']', '"' -> 4;
			case 'f', 'k', '<', '>', '(', ')', '{', '}', '*' -> 5;
			case '~', '@' -> 7;
			default -> 6;
		};
	}

	static int width(String s) {
		int w = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '&' && i + 1 < s.length() && "0123456789abcdefklmnor".indexOf(Character.toLowerCase(s.charAt(i + 1))) >= 0) {
				i++;
				continue;
			}
			w += charWidth(c);
		}
		return w;
	}

	/** Word-wraps one paragraph. The paragraph's leading colour code is repeated on every wrapped line. */
	static List<String> wrap(String paragraph) {
		String color = paragraph.matches("^&[0-9a-fk-or].*") ? paragraph.substring(0, 2) : "";
		String indent = paragraph.startsWith(color + "- ") ? "  " : "";
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : paragraph.substring(color.length()).split(" ")) {
			String candidate = line.isEmpty() ? word : line + " " + word;
			if (width(candidate) <= MAX_WIDTH) {
				line.setLength(0);
				line.append(candidate);
				continue;
			}
			if (!line.isEmpty()) {
				lines.add(color + line);
				line.setLength(0);
				word = indent + word;
			}
			// A single word wider than the page is split by characters.
			for (char c : word.toCharArray()) {
				if (width(line.toString() + c) > MAX_WIDTH) {
					lines.add(color + line);
					line.setLength(0);
				}
				line.append(c);
			}
		}
		if (!line.isEmpty()) lines.add(color + line);
		return lines;
	}

	/** Every section starts on a new page; long sections continue on following pages. */
	public static List<String> paginate(List<Section> sections) {
		List<String> pages = new ArrayList<>();
		for (Section s : sections) {
			List<String> lines = new ArrayList<>();
			if (s.title() != null) {
				lines.addAll(wrap(s.title()));
				lines.add("");
			}
			for (String p : s.paragraphs()) {
				if (p.isEmpty()) lines.add("");
				else lines.addAll(wrap(p));
			}
			for (int i = 0; i < lines.size(); i += LINES_PER_PAGE) {
				List<String> page = new ArrayList<>(lines.subList(i, Math.min(lines.size(), i + LINES_PER_PAGE)));
				while (!page.isEmpty() && page.get(0).isEmpty()) page.remove(0);
				if (!page.isEmpty()) pages.add(String.join("\n", page));
			}
		}
		return pages;
	}
}
