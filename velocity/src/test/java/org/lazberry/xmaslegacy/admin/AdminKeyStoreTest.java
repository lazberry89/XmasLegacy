package org.lazberry.xmaslegacy.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminKeyStoreTest {
	@TempDir Path dir;

	@Test
	void matchesListedKey() throws Exception {
		Path file = dir.resolve("admin-keys.yml");
		Files.writeString(file, "keys:\n  - \"alpha-key\"\n  - \"beta-key\"\n");
		AdminKeyStore store = new AdminKeyStore(file);

		assertTrue(store.hasKeys());
		assertTrue(store.matches("beta-key"));
		assertFalse(store.matches("gamma"));
		assertFalse(store.matches(""));
		assertFalse(store.matches(null));
	}

	@Test
	void reloadsWhenFileChanges() throws Exception {
		Path file = dir.resolve("admin-keys.yml");
		Files.writeString(file, "keys: [\"alpha-key\"]\n");
		AdminKeyStore store = new AdminKeyStore(file);
		assertTrue(store.matches("alpha-key"));

		FileTime before = Files.getLastModifiedTime(file);
		Files.writeString(file, "keys: [\"new-key\"]\n");
		Files.setLastModifiedTime(file, FileTime.fromMillis(before.toMillis() + 2000));

		assertFalse(store.matches("alpha-key"));
		assertTrue(store.matches("new-key"));
	}

	@Test
	void missingFileHasNoKeys() {
		AdminKeyStore store = new AdminKeyStore(dir.resolve("nope.yml"));
		assertFalse(store.hasKeys());
		assertFalse(store.matches("x"));
	}

	@Test
	void deletedFileRejectsEverything() throws Exception {
		Path file = dir.resolve("admin-keys.yml");
		Files.writeString(file, "keys: [\"alpha-key\"]\n");
		AdminKeyStore store = new AdminKeyStore(file);
		assertTrue(store.matches("alpha-key"));

		Files.delete(file);
		assertFalse(store.matches("alpha-key"));
		assertFalse(store.hasKeys());
	}
}
