package org.lazberry.xmaslegacy.stock.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockManager;

public record StockCommandInfo(StockManager sm) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            var item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                InfoUtils.error(player, "주권 확인증을 손에 들고있어주세요!");
                return;
            }
            if (!sm.isStockCertificate(item)) {
                InfoUtils.error(player, "주권 확인증을 손에 들고 사용해주세요.");
                return;
            }

            var optionalStock = sm.parseStockFromCertificate(item);
            if (optionalStock.isEmpty()) {
                InfoUtils.error(player, "확인증이 훼손되어 정보를 인식할 수 없습니다!");
                InfoUtils.warn(player, "\"&6/문의\"&f를 사용하여 관리자에게 문의해보세요.");
                return;
            }
            var stock = optionalStock.get();
            player.sendMessage(stock.getInfoMessage());
        } else if (args.length >= 2) {
            var value = args[1].toLowerCase();

            if (value.isBlank()) {
                InfoUtils.error(player, "주식 이름을 입력해주세요!");
                return;
            }
            var optionalStock = sm.getStock(value);
            if (optionalStock.isEmpty()) {
                InfoUtils.error(player, "존재하지 않는 주식 이름입니다: &7{}", value);
                return;
            }
            var stock = optionalStock.get();
            player.sendMessage(stock.getInfoMessage());
        } else {
            InfoUtils.error(player, "잘못된 명령어 사용법입니다!");
            InfoUtils.warn(player, "사용법: /주식 정보, /주식 정보 <주식 이름>");
        }
    }
}
