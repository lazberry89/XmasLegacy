package org.lazberry.xmaslegacy.admin;

import java.util.List;

/** One bag slot as shown on the admin page. {@code slot} is -1 for data that could not be read. */
public record ItemView(int slot, String id, int count, String name, String nameColor,
                       List<String> lore, List<String> enchants, String model) {}
