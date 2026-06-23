package xmaslegacy.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class BookUtils {

    @ApiStatus.Internal
    private BookUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static @NotNull ItemStack create(Component author, Component title, Component... pages) {
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
