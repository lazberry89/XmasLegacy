package org.lazberry.xmaslegacy.admin;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/** Admin keys from a server-only yml file, re-read whenever the file's modification time changes. */
@Slf4j
public class AdminKeyStore {
	private final Path file;
	private FileTime loadedAt;
	private List<byte[]> keys = List.of();

	public AdminKeyStore(Path file) {
		this.file = file;
	}

	public synchronized boolean hasKeys() {
		refresh();
		return !keys.isEmpty();
	}

	public synchronized boolean matches(String key) {
		if (key == null || key.isEmpty()) return false;
		refresh();
		byte[] given = key.getBytes(StandardCharsets.UTF_8);
		boolean found = false;
		// Check every key so the time taken does not reveal which one matched.
		for (byte[] k : keys) found |= MessageDigest.isEqual(k, given);
		return found;
	}

	private void refresh() {
		try {
			if (!Files.exists(file)) {
				keys = List.of();
				loadedAt = null;
				return;
			}
			FileTime modified = Files.getLastModifiedTime(file);
			if (modified.equals(loadedAt)) return;

			Object root = new Yaml().load(Files.readString(file));
			Object list = root instanceof Map<?, ?> map ? map.get("keys") : null;
			keys = list instanceof List<?> l
					? l.stream().filter(o -> o != null && !o.toString().isBlank())
					.map(o -> o.toString().getBytes(StandardCharsets.UTF_8)).toList()
					: List.of();
			loadedAt = modified;
		} catch (Exception e) {
			log.error("Failed to read admin keys from {}. All admin logins are refused.", file, e);
			keys = List.of();
			loadedAt = null;
		}
	}
}
