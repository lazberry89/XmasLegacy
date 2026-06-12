package xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Commands(command = "파티")
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull UserManager um;
    private final @NotNull PartyManager pm;

    public PartyCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.getInstance();
        this.pm = PartyManager.getInstance();
    }

    //파티 초대 <이름>
    //파티 참가 <이름>
    //파티 멤버 <이름>
    //파티 추방 <이름>
    //파티 생성
    //파티 나가기
    //파티 멤버
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        var user = um.getUser(p.getUniqueId());
        if (user == null) {
            ServerTransfer.loadUser(p, false);
            return true;
        }
        if (args.length == 1) {
            switch (args[0]) {
                case "생성" -> {
                    if (pm.createParty(user)) plugin.infoMsg(InfoLevel.INFO, p, "파티가 생성되었습니다.");
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티를 생성하지 못했습니다. 이미 파티에 소속되어있습니다.");
                }
                case "멤버" -> {
                    var party = pm.getParty(p.getUniqueId());
                    if (party == null) {
                        plugin.infoMsg(InfoLevel.WARN, p, "소속되어있는 파티가 없습니다.");
                        return true;
                    }
                    List<User> members = party.getMembers();
                    List<String> notLeader = new ArrayList<>(members.stream()
                            .filter(u -> !u.equals(party.getLeader()))
                            .map(User::getName).toList());
                    if (members.isEmpty()) {
                        plugin.infoMsg(InfoLevel.ERROR, p, "파티원이 없네요..? 뭔가 이상해보입니다.");
                        return true;
                    }
                    Component msgLeader = ColorUtils.chat(Alert.YELLOW + " 파티 리더 : &6" + party.getLeader().getName());
                    Component msgMembers = ColorUtils.chat(Alert.YELLOW + " 파티 멤버 : &6" + (notLeader.isEmpty() ? "&c없음" : String.join(", ", notLeader)));
                    p.sendMessage(msgLeader.appendNewline().append(msgMembers));
                }
                case "나가기" -> {
					if (pm.leaveParty(user)) plugin.infoMsg(InfoLevel.INFO, p, "파티에서 나갔습니다.");
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티에서 나가지 못했습니다. 파티에 소속되어있는지 확인해주세요.");
                }
                default -> plugin.infoMsg(InfoLevel.ERROR, p, "유효한 명령어가 아닙니다.");
            }
        }
        if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				plugin.infoMsg(InfoLevel.ERROR, p, "플레이어를 찾을 수 없습니다.");
				return true;
			}
			var party = pm.getParty(target.getUniqueId());
			var current = pm.getParty(p.getUniqueId());
			switch (args[0]) {
				case "초대" -> {
					if (party == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "소속되어있는 파티가 없습니다.");
						return true;
					}
					if (party.isFull()) {
						plugin.infoMsg(InfoLevel.ERROR, p, "파티가 가득 찼습니다.");
						return true;
					}
					if (user.isMobile()) FloodgateApi.getInstance().getPlayer(target.getUniqueId()).sendForm(inviteComp(party, target));
					else target.sendMessage(inviteComp(party));
					plugin.infoMsg(InfoLevel.INFO, p, "파티 초대 요청을 보냈습니다.");
				}
				case "참가" -> {
					if (current != null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "이미 파티에 소속되어 있습니다. 파티에 참가하려면 먼저 현재 파티에서 나가야 합니다.");
						return true;
					}
					if (party == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
						return true;
					}
					if (party.isFull()) {
						plugin.infoMsg(InfoLevel.ERROR, p, "파티가 가득 찼습니다.");
						return true;
					}
					if (pm.joinParty(user, party.getLeader())) plugin.infoMsg(InfoLevel.INFO, p, "파티에 참가했습니다.");
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
				}
				case "추방" -> {
					if (party == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
						return true;
					}
				}
			}
        }
        return true;
    }

	private @NotNull SimpleForm inviteComp(@NotNull Party party, @NotNull Player player) {
		UUID uuid = player.getUniqueId();
		return SimpleForm.builder()
				.title("§6§l서버 참가 제안")
				.content("파티초대가 왔습니다.\n" + "초대자 : §6" + party.getLeader().getName() + "\n" + "§r파티원 수 : §6" + party.getMembers().size() + " / 4")
				.button("§a§l[ 수락 ]")
				.button("§c§l[ 거절 ]")

				.validResultHandler(response -> {
					int buttonId = response.clickedButtonId();

					if (buttonId == 0) {
						var user = um.getUser(uuid);
						if (user == null) {
							ServerTransfer.loadUser(player, false);
							return;
						}
						if (pm.joinParty(user, party.getLeader())) plugin.infoMsg(InfoLevel.INFO, player, "파티에 참가했습니다.");
						else plugin.infoMsg(InfoLevel.ERROR, player, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
					} else plugin.infoMsg(InfoLevel.WARN, player, "서버 이동 제안이 거절되었습니다.");
				})
				.closedResultHandler(() -> plugin.infoMsg(InfoLevel.WARN, player, "서버 이동 제안이 취소되었습니다."))
				.build();
	}

	private @NotNull Component inviteComp(@NotNull Party party) {
		var options = ClickCallback.Options.builder()
				.uses(1)
				.lifetime(java.time.Duration.ofMinutes(3))
				.build();
		Component accept = ColorUtils.chat("&a&l[수락]").hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 파티에 참가하세요. -> &6" + party.getLeader().getName() + "님의 파티")))
				.clickEvent(ClickEvent.callback(audience -> {
					if (audience instanceof Player p && p.isOnline()) {
						var user = um.getUser(p.getUniqueId());
						if (user == null) {
							ServerTransfer.loadUser(p, false);
							return;
						}
						if (pm.joinParty(user, party.getLeader())) plugin.infoMsg(InfoLevel.INFO, p, "파티에 참가했습니다.");
						else plugin.infoMsg(InfoLevel.ERROR, p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
					}
				}, options));
		return ColorUtils.chat(Alert.XmasLegacy + " 파티참가 요청이 왔습니다. (초대자 : &6" + party.getLeader().getName() + "&f) ").append(accept);
	}

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
        return List.of();
    }
}
