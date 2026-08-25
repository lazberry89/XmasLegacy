package org.lazberry.xmaslegacy.collectors.game;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Registry.Include(type = ServerType.MAIN)
public class TicketRepository {
    private final Map<Difficulty, ItemStack> ticketByDifficulty = new EnumMap<>(Difficulty.class);
    private final NamespacedKey key;

    @Inject
    public TicketRepository(XmasLegacy plugin) {
        this.key = KeyUtils.get("collectors_ticket");
        ticketByDifficulty.put(Difficulty.PEACEFUL, createPeacefulTicket(plugin));
        ticketByDifficulty.put(Difficulty.EXCITING, createExcitingTicket(plugin));
        ticketByDifficulty.put(Difficulty.HORROR, createHorrorTicket(plugin));
    }

    public @NotNull ItemStack getTicket(Difficulty difficulty) {
        return Objects.requireNonNull(ticketByDifficulty.get(difficulty));
    }

    public boolean isTicket(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        return ticketByDifficulty.containsValue(item);
    }

    public @Nullable Difficulty getDifficultyByTicket(ItemStack item) {
        if (!isTicket(item)) return null;
        return ParseEnum.of(Difficulty.class).parse(KeyUtils.get(item, key, PersistentDataType.STRING));
    }

    private ItemStack createPeacefulTicket(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
                .setName(ColorUtils.chat("&a&l지하 폐하수도 입장권"))
                .setLore(ColorUtils.chat("&7입장권을 들고 문지기를 찾아 티켓을 건내주세요."),
                        ColorUtils.chat("&7난이도 : &a쉬움"))
                .setGlint(true)
                .setTag(key, PersistentDataType.STRING, Difficulty.PEACEFUL.name())
                .setMaxStackSize(1)
                .build();
    }

    private ItemStack createExcitingTicket(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
                .setName(ColorUtils.chat("&6&l무너진 성당 입장권"))
                .setLore(ColorUtils.chat("&7입장권을 들고 문지기를 찾아 티켓을 건내주세요."),
                        ColorUtils.chat("&7난이도 : &e보통, 어려움"))
                .setGlint(true)
                .setTag(key, PersistentDataType.STRING, Difficulty.EXCITING.name())
                .setMaxStackSize(1)
                .build();
    }

    private ItemStack createHorrorTicket(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .setName(ColorUtils.chat("&c&l사제의 무덤 입장권"))
                .setLore(ColorUtils.chat("&7입장권을 들고 문지기를 찾아 티켓을 건내주세요."),
                        ColorUtils.chat("&7난이도 : &4어려움, 호러"))
                .setGlint(true)
                .setTag(key, PersistentDataType.STRING, Difficulty.HORROR.name())
                .setMaxStackSize(1)
                .build();
    }
}
