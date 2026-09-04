package org.lazberry.xmaslegacy.teleporter.logic;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.AbstractDataProcessor;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterConfig extends AbstractDataProcessor {
    private final TeleporterManager tm;

    @Inject
    public TeleporterConfig(XmasLegacy plugin, TeleporterManager tm) {
        super(plugin, "teleporter_config");
        this.tm = tm;
    }

    @Override
    public void abstractInitiate() {
        ConfigBuilder.of(getConfig()).setDefault("cooldown-millis", 3000);
    }

    @Override
    public void saveSync() {
        synchronized (this) {
            var builder = ConfigBuilder.of(getConfig());
            builder.set("portals", null);

            for (TeleporterManager.PortalEntry entry : tm.snapshot()) {
                String path = "portals." + entry.id() + ".";
                builder.set(path + "entrance-teleporter.loc1", entry.entrance().getLoc1());
                builder.set(path + "entrance-teleporter.loc2", entry.entrance().getLoc2());
                builder.set(path + "entrance-teleporter.color", entry.entrance().getColor());
                builder.set(path + "destination", entry.destination());
            }
            builder.save(getFile());
        }
    }

    @Override
    public void loadSync() {
        synchronized (this) {
            long cooldown = getConfig().getLong("cooldown-millis", 3000L);
            tm.setCOOLDOWN_MILLIS(cooldown);

            tm.clear();

            ConfigurationSection section = getConfig().getConfigurationSection("portals");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                Location loc1 = section.getLocation(key + ".entrance-teleporter.loc1");
                Location loc2 = section.getLocation(key + ".entrance-teleporter.loc2");
                Color color = section.getColor(key + ".entrance-teleporter.color", Color.GRAY);

                Location destination = section.getLocation(key + ".destination");

                if (loc1 == null || loc2 == null || destination == null) {
                    log.error("Portal info [{}] is damaged. Skipping...", key);
                    continue;
                }

                Teleporter teleporter = new Teleporter(loc1, loc2, color);
                if (!tm.registerWay(key, teleporter, destination)) {
                    log.error("Failed to register portal instance [{}] to Manager.", key);
                }
            }
        }
    }
}