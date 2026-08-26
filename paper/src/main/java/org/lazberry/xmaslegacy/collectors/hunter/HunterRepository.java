package org.lazberry.xmaslegacy.collectors.hunter;

import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Registry.Include(type = ServerType.MAIN)
public class HunterRepository {
	private final Map<Difficulty, Hunter> hunters = new EnumMap<>(Difficulty.class);

	@Inject
	public HunterRepository(HunterData data) {
		hunters.put(Difficulty.PEACEFUL, new HunterBug(data));
		hunters.put(Difficulty.EXCITING, new PhaseHunter(data));
		hunters.put(Difficulty.HORROR, new Silence(data));
	}

	public <T extends Hunter> T getHunter(Difficulty difficulty, Class<T> clazz) {
		return clazz.cast(Objects.requireNonNull(hunters.get(difficulty)));
	}
}
