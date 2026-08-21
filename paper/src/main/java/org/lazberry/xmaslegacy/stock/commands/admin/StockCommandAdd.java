package org.lazberry.xmaslegacy.stock.commands.admin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.util.Locale;

public record StockCommandAdd(StockManager sm) implements SubCommand {

    // stock admin add <이름> <시작가> <상한가> <하한가>
    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 4) {
            Stock stock;
            var name = args[2];
            if (sm.exists(name.toLowerCase(Locale.ROOT))) {
                InfoUtils.error(player, "이미 존재하는 명칭입니다.");
                return;
            }
            int initPrice;
            try {
                initPrice = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                InfoUtils.error(player, "숫자를 입력해주세요!");
                return;
            }
            if (initPrice <= 0) {
                InfoUtils.error(player, "시작 가격은 양수여야합니다.");
                return;
            }
            if (args.length >= 6) {
                int max;
                int min;
                try {
                    max = Integer.parseInt(args[4]);
                    min = Integer.parseInt(args[5]);
                } catch (NumberFormatException e) {
                    InfoUtils.error(player, "상한가 혹은 하한가는 숫자여야합니다!");
                    return;
                }
                if (max <= 0 || min <= 0) {
                    InfoUtils.error(player, "상한가 혹은 하한가는 양수여야합니다.");
                    return;
                }
                if (max <= min) {
                    InfoUtils.error(player, "상한가는 하한가보다 항상 커야합니다.");
                    return;
                }
                stock = new Stock(name, initPrice, max, min);
            } else if (args.length == 5) {
                InfoUtils.error(player, "상한가와 하한가는 함께 입력해야 합니다!");
                InfoUtils.warn(player, "사용법: /주식 관리 추가 <이름> <시작가> <상한가> <하한가>");
                return;

            } else {
                stock = new Stock(name, initPrice);
            }
            if (sm.registerStock(stock) != null) {
                InfoUtils.info(player, "주식 {}이 성공적으로 등록되었습니다!", stock.getName());
                player.sendMessage(stock.getInfoMessage());
            }
        } else {
            InfoUtils.error(player, "잘못된 명령어 사용법입니다.");
            InfoUtils.warn(player, "사용법: /주식 관리 추가 <이름> <시작가>");
        }
    }
}
