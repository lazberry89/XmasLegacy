package org.lazberry.xmaslegacy.stock.container;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StockContainer implements InventoryHolder {
    @EqualsAndHashCode.Include
    private final UUID owner;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int SLOTS_PER_PAGE = 45;
    private final List<ItemStack> allItems = new ArrayList<>();

    public StockContainer(UUID owner) {
        this.owner = owner;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtils.chat("&6&l개인 주식 보관소"));
        renderPage();
    }

    public StockContainer(UUID owner, ItemStack[] savedItems) {
        this(owner);
        if (savedItems != null && savedItems.length > 0)
            allItems.addAll(Arrays.asList(savedItems));
        renderPage();
    }

    public void renderPage() {
        for (int i = 0; i < SLOTS_PER_PAGE; i++) inventory.setItem(i, null);

        int startIndex = currentPage * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, allItems.size());

        if (startIndex < allItems.size()) {
            List<ItemStack> pageItems = allItems.subList(startIndex, endIndex);
            for (int i = 0; i < pageItems.size(); i++) {
                inventory.setItem(i, pageItems.get(i));
            }
        }
        setupControlBar();
    }

    private void setupControlBar() {
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        bg.editMeta(m -> m.customName(ColorUtils.chat("")));
        for (int i = 45; i <= 53; i++) inventory.setItem(i, bg);

        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.customName(ColorUtils.chat("&e&l◀ 이전 페이지"));
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }

        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.customName(ColorUtils.chat("&e&l다음 페이지 ▶"));
        nextButton.setItemMeta(nextMeta);
        inventory.setItem(53, nextButton);
    }

    public void syncCurrentPage() {
        int startIndex = currentPage * SLOTS_PER_PAGE;

        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            int targetIndex = startIndex + i;
            ItemStack item = inventory.getItem(i);

            while (allItems.size() <= targetIndex) {
                allItems.add(null);
            }
            allItems.set(targetIndex, item);
        }
    }

    public void nextPage() {
        syncCurrentPage();
        currentPage++;
        renderPage();
    }

    public void prevPage() {
        if (currentPage > 0) {
            syncCurrentPage();
            currentPage--;
            renderPage();
        }
    }

    public ItemStack[] getContentsForSave() {
        syncCurrentPage();
        return allItems.stream()
                .filter(item -> item != null && !item.getType().isAir())
                .toArray(ItemStack[]::new);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
