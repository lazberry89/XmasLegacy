package org.lazberry.xmaslegacy.stock.container;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockContainerManager {
    private final Map<UUID, StockContainer> containers = new ConcurrentHashMap<>();

    public StockContainerManager() {}

    public Collection<StockContainer> getStockContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    public StockContainer add(StockContainer container) {
        return containers.put(container.getOwner(), container);
    }

	public <C extends Collection<StockContainer>> void addAll(C values) {
		values.forEach(this::add);
	}

    public StockContainer getOrCreateContainer(UUID uuid) {
        return containers.computeIfAbsent(uuid, k -> new StockContainer(uuid));
    }

    public Optional<StockContainer> getContainer(UUID uuid) {
        return Optional.ofNullable(containers.get(uuid));
    }

    public void unloadContainer(UUID uuid) {
        containers.remove(uuid);
    }
}
