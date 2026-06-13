package xmasLegacy;

import com.google.j2objc.annotations.UsedByReflection;
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

import java.util.*;
import java.util.stream.Collectors;

@Commands(command = "파티")
@SuppressWarnings("unused")
@UsedByReflection
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull UserManager um;
    private final @NotNull PartyManager pm;

    public PartyCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.INSTANCE;
        this.pm = PartyManager.INSTANCE;
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
			ServerTransfer.sendReloadNotice(p);
            return true;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
				case "도움", "help" -> sendHelpMessage(p);
                case "생성", "create" -> {
                    if (pm.createParty(user)) plugin.infoMsg(InfoLevel.INFO, p, "파티가 생성되었습니다.");
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티를 생성하지 못했습니다. 이미 파티에 소속되어있습니다.");
                }
                case "멤버", "member" -> {
                    var party = pm.getParty(p.getUniqueId());
                    if (party == null) {
                        plugin.infoMsg(InfoLevel.WARN, p, "소속되어있는 파티가 없습니다.");
                        return true;
                    }
                    p.sendMessage(memberShowcase(party));
                }
                case "나가기", "leave" -> {
	                var party = pm.getParty(p.getUniqueId());
					if (pm.leaveParty(user)) {
						party.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUUID()))
								.filter(Objects::nonNull)
								.filter(Player::isOnline)
								.filter(Player::isValid)
								.forEach(t -> plugin.infoMsg(InfoLevel.INFO, t, "&6" + p.getName() + "&f님이 파티를 나갔습니다."));
						plugin.infoMsg(InfoLevel.INFO, p, "파티에서 나갔습니다.");
					}
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티에서 나가지 못했습니다. 파티에 소속되어있는지 확인해주세요.");
                }
                default -> plugin.infoMsg(InfoLevel.ERROR, p, "유효한 명령어가 아닙니다.");
            }
        }
        if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				plugin.infoMsg(InfoLevel.ERROR, p, "플레이어를 찾을 수 없습니다.");
				return true;
			}
			User targetUser = um.getUser(target.getUniqueId());
			if (targetUser == null) {
				plugin.infoMsg(InfoLevel.ERROR, p, "해당 유저의 정보가 로드되지 않았습니다.");
				return true;
			}
			var party = pm.getParty(target.getUniqueId());
			var current = pm.getParty(p.getUniqueId());
			switch (args[0].toLowerCase()) {
				case "초대", "invite" -> {
					if (current == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "소속되어있는 파티가 없습니다. 먼저 파티를 생성하세요.");
						return true;
					}
					if (party != null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어는 이미 다른 파티에 소속되어 있습니다.");
						return true;
					}
					if (current.isFull()) {
						plugin.infoMsg(InfoLevel.ERROR, p, "파티가 가득 찼습니다.");
						return true;
					}
					if (targetUser.isMobile())
						Bukkit.getScheduler().runTaskLater(plugin, () -> {
							var floodgatePlayer = FloodgateApi.getInstance().getPlayer(target.getUniqueId());
							if (floodgatePlayer != null) {
								floodgatePlayer.sendForm(inviteComp(current, target));
							}
						}, 5L);
					else Bukkit.getScheduler().runTaskLater(plugin, () -> target.sendMessage(inviteComp(current)), 2L);
					plugin.infoMsg(InfoLevel.INFO, p, "파티 초대 요청을 보냈습니다.");
				}
				case "참가", "join" -> {
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
					if (pm.joinParty(party.getLeader(), user)) plugin.infoMsg(InfoLevel.INFO, p, "파티에 참가했습니다.");
					else plugin.infoMsg(InfoLevel.ERROR, p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
				}
				case "추방", "expel" -> {
					if (current == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "소속되어 있는 파티가 없습니다.");
						return true;
					}
					if (party == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
						return true;
					}
					if (!current.equals(party)) {
						plugin.infoMsg(InfoLevel.ERROR, p, "서로 같은 파티가 아닙니다.");
						return true;
					}
					if (!user.equals(current.getLeader())) {
						plugin.infoMsg(InfoLevel.ERROR, p, "파티장만 유저를 추방시킬 수 있습니다.");
						return true;
					}
					if (pm.leaveParty(targetUser)) {
						plugin.infoMsg(InfoLevel.WARN, target, "파티에서 추방당했습니다.");
						plugin.infoMsg(InfoLevel.INFO, p, "파티에서 추방했습니다.");
					} else plugin.infoMsg(InfoLevel.ERROR, p, "파티에서 추방하지 못했습니다. 파티에 소속되어있는지 확인해주세요.");
				}
				case "멤버", "members" -> {
					if (party == null) {
						plugin.infoMsg(InfoLevel.WARN, p, "파티에 소속되어있지 않은 유저입니다.");
						return true;
					}
					p.sendMessage(memberShowcase(party));
				}
			}
        }
        return true;
    }

	private void sendHelpMessage(@NotNull Player p) {
		p.sendMessage(ColorUtils.chat("&7&m-------------------------------------"));
		p.sendMessage(ColorUtils.chat("&6&l[ XmasLegacy 파티 시스템 사용법 ]"));
		p.sendMessage(ColorUtils.chat("&e/파티 생성 &7- 새로운 파티를 생성합니다."));
		p.sendMessage(ColorUtils.chat("&e/파티 초대 <이름> &7- 플레이어를 파티에 초대합니다."));
		p.sendMessage(ColorUtils.chat("&e/파티 참가 <이름> &7- 해당 플레이어의 파티에 가입합니다."));
		p.sendMessage(ColorUtils.chat("&e/파티 멤버 &7- 현재 소속된 파티원을 확인합니다."));
		p.sendMessage(ColorUtils.chat("&e/파티 추방 <이름> &7- 파티원을 파티에서 쫓아냅니다."));
		p.sendMessage(ColorUtils.chat("&e/파티 나가기 &7- 현재 파티에서 탈퇴합니다."));
		p.sendMessage(ColorUtils.chat("&7&m-------------------------------------"));
	}

	private @NotNull Component memberShowcase(@NotNull Party party) {
		List<User> members = party.getMembers();
		List<String> notLeader = new ArrayList<>(members.stream()
				.filter(u -> !u.equals(party.getLeader()))
				.map(User::getName).toList());
		if (members.isEmpty()) return ColorUtils.chat(Alert.XmasLegacy + " 파티원이 없네요..? 뭔가 이상해보입니다.");

		Component msgLeader = ColorUtils.chat(Alert.YELLOW + " 파티 리더 : &6" + party.getLeader().getName());
		Component msgMembers = ColorUtils.chat(Alert.YELLOW + " 파티 멤버 : &6" + (notLeader.isEmpty() ? "&c없음" : String.join(", ", notLeader)));
		return msgLeader.appendNewline().append(msgMembers);
	}

	private @NotNull SimpleForm inviteComp(@NotNull Party party, @NotNull Player player) {
		UUID uuid = player.getUniqueId();
		return SimpleForm.builder()
				.title("§6§l파티 참가 제안")
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
						if (pm.joinParty(party.getLeader(), user)) plugin.infoMsg(InfoLevel.INFO, player, "파티에 참가했습니다.");
						else plugin.infoMsg(InfoLevel.ERROR, player, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
					} else plugin.infoMsg(InfoLevel.WARN, player, "파티 참가 제안이 거절되었습니다.");
				})
				.closedResultHandler(() -> plugin.infoMsg(InfoLevel.WARN, player, "파티 참가 제안이 취소되었습니다."))
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
						if (pm.joinParty(party.getLeader(), user)) {
							plugin.infoMsg(InfoLevel.INFO, p, "파티에 참가했습니다.");
							party.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUUID()))
									.filter(Objects::nonNull)
									.filter(Player::isOnline)
									.filter(Player::isValid)
									.forEach(t -> plugin.infoMsg(InfoLevel.INFO, t, "&6" + p.getName() + "&f님이 파티에 참가했습니다."));
						}
						else plugin.infoMsg(InfoLevel.ERROR, p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
					}
				}, options));
		return ColorUtils.chat(Alert.XmasLegacy + " 파티참가 요청이 왔습니다. (초대자 : &6" + party.getLeader().getName() + "&f) ").append(accept);
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
		if (args.length == 1) {
			List<String> completions = List.of("도움", "생성", "멤버", "나가기", "초대", "참가", "추방");
			return completions.stream()
					.filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
					.collect(Collectors.toList());
		}

		if (args.length == 2) {
			String subCommand = args[0].toLowerCase();
			if (subCommand.equals("초대") || subCommand.equals("invite") ||
					subCommand.equals("참가") || subCommand.equals("join") ||
					subCommand.equals("추방") || subCommand.equals("expel") ||
					subCommand.equals("멤버") || subCommand.equals("member"))

				return Bukkit.getOnlinePlayers().stream()
						.map(Player::getName)
						.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
						.collect(Collectors.toList());

		}
		return Collections.emptyList();
	}
}
