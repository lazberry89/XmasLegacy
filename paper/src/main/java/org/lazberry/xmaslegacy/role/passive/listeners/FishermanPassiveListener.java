package org.lazberry.xmaslegacy.role.passive.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.Roles.ServerRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.exp.ExpManager;
import org.lazberry.xmaslegacy.role.general.RoleManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Listeners
@Registry.Include(type = ServerType.WILD)
public class FishermanPassiveListener extends PassiveListeners implements Listener {
    private final ItemStack cookedCod;
    private final ItemStack cookedSalmon;
    private final ItemStack air;
    private final RoleManager rm;
    private final ExpManager em;

    @Inject
    public FishermanPassiveListener(UserManager um, RoleManager rm, ExpManager em) {
        super(ServerRoles.FISHERMAN, um);
        this.cookedCod = new ItemStack(Material.COOKED_COD);
        this.cookedSalmon = new ItemStack(Material.COOKED_SALMON);
        this.air = new ItemStack(Material.AIR);
        this.rm = rm;
        this.em = em;
    }

    private boolean isFish(Material material) {
        return switch (material) {
            case COD, SALMON, TROPICAL_FISH, PUFFERFISH -> true;
            default -> false;
        };
    }

    private boolean isTreasure(ItemStack item) {
        Material type = item.getType();

        if (type == Material.BOW ||
                type == Material.ENCHANTED_BOOK ||
                type == Material.NAME_TAG ||
                type == Material.NAUTILUS_SHELL ||
                type == Material.SADDLE) {
            return true;
        }

        if (type == Material.FISHING_ROD)
            return item.hasItemMeta() && item.getItemMeta().hasEnchants();

        return false;
    }

    private boolean isJunk(ItemStack item) {
        Material type = item.getType();

        if (type == Material.LILY_PAD ||
                type == Material.BOWL ||
                type == Material.LEATHER ||
                type == Material.LEATHER_BOOTS ||
                type == Material.ROTTEN_FLESH ||
                type == Material.STICK ||
                type == Material.STRING ||
                type == Material.GLASS_BOTTLE ||
                type == Material.BONE ||
                type == Material.INK_SAC ||
                type == Material.TRIPWIRE_HOOK) {
            return true;
        }

        if (type == Material.FISHING_ROD)
            return !item.hasItemMeta() || !item.getItemMeta().hasEnchants();

        return false;
    }

    @EventHandler
    public void roastFishWhenFishing(PlayerFishEvent e) {
        var player = e.getPlayer();
        PlayerFishEvent.State state = e.getState();

        canUsePassive(player, u -> {
            switch (state) {
                case BITE -> {
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    player.sendActionBar(ColorUtils.chat("&6찌를 물었어요!"));
                    GlowUtils.glow(e.getHook(), NamedTextColor.RED);
                }
                case CAUGHT_FISH -> {

                    if (e.getCaught() instanceof Item item) {
                        ItemStack caught = item.getItemStack();
                        Material material = caught.getType();

                        calculateValueAndGive(player, u, caught);

                        if (isFish(material)) {
                            if (ThreadLocalRandom.current().nextDouble() < rm.fisherman().getCookChance()) {
                                if (material == Material.COD) item.setItemStack(cookedCod.clone());
                                if (material == Material.SALMON) item.setItemStack(cookedSalmon.clone());
                            }
                        } else if (isJunk(caught)) {
                            item.setItemStack(air);
                            int amount = ThreadLocalRandom.current().nextInt(
                                    rm.fisherman().getJunkDollarMin(),
                                    rm.fisherman().getJunkDollarMax() + 1
                            );
                            u.addDollars(amount);
                            InfoUtils.info(player, "쓰레기 아이템 치환됨. &6({} -> {}원)", material.name(), amount);
                        } else askParseTreasureToDollar(player, item);
                    }
                }
                default -> {}
            }
        });
    }

    public void calculateValueAndGive(Player p, User u, ItemStack item) {
        Material material = item.getType();
        RoleManager.Fisherman fm = rm.fisherman();
        int expToGive = 0;

        if (isTreasure(item)) {
            expToGive = ThreadLocalRandom.current().nextInt(fm.getExpTreasureMin(), fm.getExpTreasureMax() + 1);
        } else if (isJunk(item)) {
            expToGive = ThreadLocalRandom.current().nextInt(fm.getExpJunkMin(), fm.getExpJunkMax() + 1);
        } else if (isFish(material)) {
            switch (material) {
                case COD ->
                        expToGive = ThreadLocalRandom.current().nextInt(fm.getExpCodMin(), fm.getExpCodMax() + 1);
                case SALMON ->
                        expToGive = ThreadLocalRandom.current().nextInt(fm.getExpSalmonMin(), fm.getExpSalmonMax() + 1);
                case PUFFERFISH ->
                        expToGive = ThreadLocalRandom.current().nextInt(fm.getExpPufferfishMin(), fm.getExpPufferfishMax() + 1);
                case TROPICAL_FISH ->
                        expToGive = ThreadLocalRandom.current().nextInt(fm.getExpTropicalFishMin(), fm.getExpTropicalFishMax() + 1);
            }
        }

        em.addRoleExp(p, expToGive);
        sendExpAlert(p, expToGive);
    }

    public void askParseTreasureToDollar(Player p, Item itemEntity) {
        ItemStack item = itemEntity.getItemStack();

        AtomicBoolean processed = new AtomicBoolean(false);
        var callbackOptions = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(1))
                .build();

        Component text = ColorUtils.chat("&e[보물]&f 등급 아이템을 낚았습니다! 즉시 판매하시겠습니까? ");

        Component accept = ColorUtils.chat("&a&l[판매]")
                .hoverEvent(HoverEvent.showText(ColorUtils.chat("&7클릭 시 보물 아이템을 판매합니다.")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player player) {
                        canUsePassive(player, u -> {
                            if (!processed.compareAndSet(false, true)) {
                                InfoUtils.error(player, "이미 처리되었거나 만료된 거래입니다.");
                                return;
                            }

                            if (itemEntity.isValid() && !itemEntity.isDead()) {
                                itemEntity.remove();
                            }
                            else if (player.getInventory().containsAtLeast(item, 1)) {
                                player.getInventory().removeItem(item);
                            }
                            else {
                                InfoUtils.error(player, "판매할 보물 아이템이 존재하지 않습니다.");
                                return;
                            }

                            int price = ThreadLocalRandom.current().nextInt(
                                    rm.fisherman().getTreasureDollarMin(),
                                    rm.fisherman().getTreasureDollarMax() + 1
                            );
                            u.addDollars(price);
                            InfoUtils.info(player, "보물 아이템을 판매했습니다. &6({} -> {}원)", item.getType().name(), price);
                        });
                    }
                }, callbackOptions));

        Component deny = ColorUtils.chat(" &c&l[취소]")
                .hoverEvent(HoverEvent.showText(ColorUtils.chat("&7보물을 아이템 형태로 유지합니다.")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player player) {
                        if (processed.compareAndSet(false, true)) {
                            InfoUtils.info(player, "보물 판매를 취소했습니다.");
                        }
                    }
                }, callbackOptions));

        p.sendMessage(text.append(accept).append(deny));
    }
}