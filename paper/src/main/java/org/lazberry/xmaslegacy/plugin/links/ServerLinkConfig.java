package org.lazberry.xmaslegacy.plugin.links;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.ServerLinks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.AbstractDataSerializer;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("UnstableApiUsage")
@Registry.Include(type = ServerType.GLOBAL)
public class ServerLinkConfig extends AbstractDataSerializer<Map<ServerLinks.Type, URI>> {

    @Inject
    public ServerLinkConfig(XmasLegacy plugin) {
        super(plugin, "server_links");
    }

    @Override
    public void saveSync(Map<ServerLinks.Type, URI> value) {
        synchronized (this) {
            ConfigBuilder builder = ConfigBuilder.create();
            if (value.isEmpty()) {
                builder.set(ServerLinks.Type.WEBSITE.name(), "https://xmaslegacy.xyz");
                setConfig(builder.save(getFile()).build());
                return;
            }

            for (var entry : value.entrySet()) {
                ServerLinks.Type type = entry.getKey();
                URI uri = entry.getValue();

                builder.set(type.name(), uri.toString());
            }
            setConfig(builder.save(getFile()).build());
        }
    }

    @Override
    public Map<ServerLinks.Type, URI> loadSync() {
        synchronized (this) {
            ConfigBuilder builder = ConfigBuilder.of(getConfig());
            final var result = new HashMap<ServerLinks.Type, URI>();
            for (String key : getConfig().getKeys(false)) {
                try {
                    ServerLinks.Type type = ParseEnum.of(ServerLinks.Type.class).parse(key);
                    String value = builder.getValue(key, String.class);
                    if (value == null) {
                        log.error("Failed to parse URI value in config.");
                        continue;
                    }

                    URI uri = URI.create(value);
                    result.put(type, uri);
                } catch (Exception e) {
                    log.error("Exception occurred while parsing config values to ServerLinks", e);
                }
            }
            return result;
        }
    }
}
