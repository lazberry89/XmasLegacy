package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record GoodsCommandCreate(ShowCaseManager showCaseManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 3) { //goods create <name> <price>
			InfoUtils.error(player, "올바르지 않는 명령어입니다.");
			return;
		}
		String name = args[1];
		int price;
		try {
			price = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			InfoUtils.error(player, "가격은 숫자 형태로 입력해주세요!");
			return;
		}
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType().isAir()) {
			InfoUtils.error(player, "판매할 아이템을 손에 들고있어주세요!");
			return;
		}
		showCaseManager.registerGoods(name, item, price);
		InfoUtils.info(player, "등록했습니다. &6({})", name);
	}
}
