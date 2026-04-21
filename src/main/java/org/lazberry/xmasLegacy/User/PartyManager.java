package org.lazberry.xmasLegacy.User;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.*;

public class PartyManager {
    private final UserManager UM;
    private final XmasLegacy plugin;
    private final Map<UUID, List<User>> userPartyMap = new HashMap<>();

    public PartyManager(UserManager UM, XmasLegacy plugin) {
        this.UM = UM;
        this.plugin = plugin;
    }

    public List<User> createParty(Player p) {
        UUID uuid = p.getUniqueId();
        if (userPartyMap.containsKey(uuid)) return userPartyMap.get(uuid);

        List<User> newParty = new ArrayList<>();
        newParty.add(UM.getUser(p));
        userPartyMap.put(uuid, newParty);
        p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 파티를 생성하였습니다."));
        p.sendMessage(ColorUtils.chat("&6파티원&f을 모아보세요!"));
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        return newParty;
    }

    public boolean joinParty(Player joiner, Player target) {
        List<User> targetParty = userPartyMap.get(target.getUniqueId());
        if (targetParty == null) return false;

        if (targetParty.size() >= 4) return false;
        if (userPartyMap.containsKey(joiner.getUniqueId())) return false;

        User u = UM.getUser(joiner);
        targetParty.add(u);
        userPartyMap.put(joiner.getUniqueId(), targetParty);
        joiner.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 파티에 참가했습니다. &6" + targetParty.size() + "/4"));
        targetParty.forEach(p -> {
            p.getPlayer().sendMessage(ColorUtils.chat(Prefix.YELLOW + "파티에 " + joiner.getName() + "님이 참가했습니다. &6" + targetParty.size() + "/4"));
            p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        });
        return true;
    }

    public void quitParty(Player p) {
        UUID uuid = p.getUniqueId();
        List<User> currentParty = userPartyMap.get(uuid);
        if (currentParty == null) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 참가 중인 파티가 없습니다!"));
            return;
        }

        User u = UM.getUser(p);

        userPartyMap.remove(uuid);
        p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 파티에서 퇴장했습니다."));

        currentParty.remove(u);

        if (!currentParty.isEmpty()) {
            String msg = ColorUtils.chat(Prefix.YELLOW + p.getName() + "님이 파티에서 퇴장했습니다.");
            currentParty.forEach(member -> member.getPlayer().sendMessage(msg));

            currentParty.getFirst().getPlayer().sendMessage(ColorUtils.chat(Prefix.YELLOW + " 당신이 새로운 파티장이 되었습니다."));
        }
    }

    public boolean removeParty(Player p) {
        UUID uuid = p.getUniqueId();
        List<User> currentParty = userPartyMap.get(uuid);
        if (currentParty == null) return false;

        User leader = currentParty.getFirst();
        if (!leader.getUuid().equals(uuid)) return false;

        for (User member : currentParty) {
            userPartyMap.remove(member.getUuid());
            member.getPlayer().sendMessage(Prefix.YELLOW + " 파티가 해체되었습니다.");
        }

        currentParty.clear();
        return true;
    }

    public List<User> getParty(Player p) {
        return userPartyMap.get(p.getUniqueId());
    }

    public boolean isInParty(Player p) {
        return userPartyMap.containsKey(p.getUniqueId());
    }

    public boolean isLeader(Player p) {
        List<User> party = getParty(p);
        if (party == null || party.isEmpty()) return false;

        return party.getFirst().getUuid().equals(p.getUniqueId());
    }

    public boolean isParty(Player p1, Player p2) {
        List<User> party = getParty(p1);
        if (party == null) return false;

        return party.stream()
                .anyMatch(user -> user.getUuid().equals(p2.getUniqueId()));
    }
}