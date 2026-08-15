package org.lazberry.xmaslegacy.Utils;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
public class InventorySerializer {

    public static @NotNull String serializeContents(ItemStack[] items) {
        if (items == null) return "";
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", items);

        String yamlString = config.saveToString();
        return Base64.getEncoder().encodeToString(yamlString.getBytes(StandardCharsets.UTF_8));
    }

    public static @NotNull String serializeContents(ItemStack item) {
        if (item == null) return "";
        return serializeContents(new ItemStack[]{ item });
    }

    public static @NotNull String serializeContents(Inventory inventory) {
        return serializeContents(inventory.getContents());
    }

    @SuppressWarnings("unchecked")
    public static ItemStack[] deserializeContents(String base64Data) throws Exception {
        if (base64Data == null || base64Data.isBlank()) {
            return new ItemStack[0];
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            String yamlString = new String(decodedBytes, StandardCharsets.UTF_8);

            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlString);

            List<ItemStack> list = (List<ItemStack>) config.getList("items");
            if (list == null) return new ItemStack[0];

            return list.toArray(new ItemStack[0]);
        } catch (Exception e) {
            log.error("Failed to parse Base64 data to ItemStack instance.", e);
            throw e;
        }
    }
}
