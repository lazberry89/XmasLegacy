package org.lazberry.xmaslegacy.collectors.game;

import org.lazberry.xmaslegacy.collectors.field.Field;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Include(type = ServerType.MAIN)
public class CollectorsManager {
	private final Map<Difficulty, Field> fields = new ConcurrentHashMap<>(5);
}
