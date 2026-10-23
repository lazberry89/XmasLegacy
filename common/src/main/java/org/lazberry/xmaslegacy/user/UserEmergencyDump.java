package org.lazberry.xmaslegacy.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.lazberry.xmaslegacy.user.User.GSON;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserEmergencyDump {
    private static final @NotNull Logger log = LoggerFactory.getLogger(UserEmergencyDump.class);
    private final @NotNull File rootDataFolder;

	@Inject
    public UserEmergencyDump(@NotNull File rootDataFolder) {
		this.rootDataFolder = rootDataFolder;
    }

    void threadDump(@NotNull User user) {
        File dumpDir = new File(rootDataFolder, "emergency_dumps");
        if (!dumpDir.exists())
            if (!dumpDir.mkdirs()) {
                log.error("Failed to create emergency dump directory. User {}'s info may got lost.", user.getUniqueId());
                return;
            }
        File dumpFile = new File(dumpDir, user.getUniqueId() + ".properties");

        Properties props = new Properties();
        props.setProperty("uuid", user.getUniqueId().toString());
        props.setProperty("name", user.getName());
        props.setProperty("role", user.getRole().name());
        props.setProperty("dollars", String.valueOf(user.getDollars()));
        props.setProperty("inquireCount", String.valueOf(user.getInquireCount()));
        props.setProperty("playTime", String.valueOf(user.getPlayTime()));
        props.setProperty("Exp", String.valueOf(user.getExp()));
        props.setProperty("roleExp", String.valueOf(user.getRoleExp()));
        props.setProperty("sinPoints", String.valueOf(user.getSinPoints()));
        props.setProperty("level", String.valueOf(user.getLevel()));
        props.setProperty("isNewUser", String.valueOf(user.isNewUser()));
        props.setProperty("wantsCookie", String.valueOf(user.ifWantsCookie()));
        props.setProperty("tier", user.getTier().name());
        props.setProperty("mastery", user.getMastery().name());
        props.setProperty("isImmuneToIcing", String.valueOf(user.isImmuneToIcing()));
        props.setProperty("icingState", String.valueOf(user.getIcingState()));
        props.setProperty("showBoard", String.valueOf(user.isShowBoard()));
        props.setProperty("isMobile", String.valueOf(user.isMobile()));
        props.setProperty("availablePrefix", new com.google.gson.Gson().toJson(user.getAvailablePrefix()));
        props.setProperty("equipPrefix", user.getEquipPrefix() != null ? user.getEquipPrefix().name() : "");

        try (FileOutputStream out = new FileOutputStream(dumpFile)) {
            props.store(out, "Emergency Backup for " + user.getName());
            log.warn("User {}'s info got backUp. (Thread Dump via Properties)", user.getUniqueId());
        } catch (Exception e) {
            log.error("Local backUp failed. Info may got lost.", e);
        }
    }

    @Nullable User checkLocalEmergencyFile(@NotNull UUID uuid) {
        File dumpFile = new File(new File(rootDataFolder, "emergency_dumps"), uuid + ".properties");
        if (!dumpFile.exists()) return null;

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(dumpFile)) {
            props.load(in);
            String name = props.getProperty("name", "Unknown");

	        Role role = Role.parseRole(props.getProperty("role"), ServerRoles.USER);
            User recoveredUser = new User(uuid, role, name);

            recoveredUser.setDollars(Long.parseLong(props.getProperty("dollars", "0")));
            recoveredUser.setInquireCount(Integer.parseInt(props.getProperty("inquireCount", "0")));
            recoveredUser.setPlayTime(Long.parseLong(props.getProperty("playTime", "0")));
            recoveredUser.setExp(Integer.parseInt(props.getProperty("Exp", "0")));
            recoveredUser.setRoleExp(Integer.parseInt(props.getProperty("roleExp", "0")));
            recoveredUser.setSinPoints(Integer.parseInt(props.getProperty("sinPoints", "0")));
            recoveredUser.setLevel(Integer.parseInt(props.getProperty("level", "0")));
            recoveredUser.setNewUser(Boolean.parseBoolean(props.getProperty("isNewUser", "false")));
            recoveredUser.wantsCookie(Boolean.parseBoolean(props.getProperty("wantsCookie", "false")));

            recoveredUser.setTier(Tier.valueOf(props.getProperty("tier", "BRONZE")));
            recoveredUser.setMastery(RoleMastery.valueOf(props.getProperty("mastery", "BEGINNER")));
            recoveredUser.setImmuneToIcing(Boolean.parseBoolean(props.getProperty("isImmuneToIcing", "false")));
            recoveredUser.setIcingState(Integer.parseInt(props.getProperty("icingState", "100")));
            recoveredUser.setShowBoard(Boolean.parseBoolean(props.getProperty("showBoard", "true")));

            recoveredUser.setMobile(Boolean.parseBoolean(props.getProperty("isMobile", "false")));

            String prefixJson = props.getProperty("availablePrefix");
            if (prefixJson != null && !prefixJson.isEmpty()) {
                List<ServerPrefix> prefixes = GSON.fromJson(
                        prefixJson,
                        new com.google.gson.reflect.TypeToken<List<ServerPrefix>>(){}.getType()
                );
                recoveredUser.getAvailablePrefix().clear();
                recoveredUser.getAvailablePrefix().addAll(prefixes);
            }

            String equipName = props.getProperty("equipPrefix");
            if (equipName != null && !equipName.isEmpty()) {
                ServerPrefix equip = ServerPrefix.parse(equipName, Tier.VISITOR);
                recoveredUser.setEquipPrefix(equip);
            } else {
                recoveredUser.setEquipPrefix(null);
            }

            return recoveredUser;
        } catch (Exception e) {
            log.error("Failed to load emergency dump for {}", uuid, e);
            return null;
        }
    }

    void deleteLocalEmergencyFile(@NotNull UUID uuid) {
        File dumpFile = new File(new File(rootDataFolder, "emergency_dumps"), uuid + ".properties");
        if (dumpFile.exists() && dumpFile.delete()) {
            log.info("Cleaned up emergency dump for User {}", uuid);
        }
    }
}
