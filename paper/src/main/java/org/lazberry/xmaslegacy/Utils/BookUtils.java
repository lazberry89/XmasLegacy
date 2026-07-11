package org.lazberry.xmaslegacy.Utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class BookUtils {

    /**
     * This method make books easier.
     * @param author set Author of book.
     * @param title name of book.
     * @param pages the components in each page.
     * @return ItemStack of created book.
     */
    public @NotNull ItemStack create(@NotNull Component author, @NotNull Component title, @NotNull Component... pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.author(author);
            meta.title(title);
            meta.addPages(pages);
            book.setItemMeta(meta);
        }
        return book;
    }
}
