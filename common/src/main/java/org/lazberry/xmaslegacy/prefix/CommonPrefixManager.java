package org.lazberry.xmaslegacy.prefix;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Components.Exclude(type = ServerType.LOBBY)
public class CommonPrefixManager {
	private final SqlPrefixRepository prefixRepository;
	private final UserManager userManager;

	@Inject
	public CommonPrefixManager(SqlPrefixRepository prefixRepository, UserManager userManager) {
		this.prefixRepository = prefixRepository;
		this.userManager = userManager;
	}

	public CompletableFuture<Boolean> grantUniquePrefix(UUID uuid, ServerPrefix prefix, Runnable action) {
		User user = userManager.getUser(uuid);
		if (user == null) return CompletableFuture.completedFuture(false);

		return prefixRepository.grantPrefix(uuid, prefix).handle((value, ex) -> {
			if (value) {
				user.addPrefix(prefix);
				action.run();
				return true;
			}
			return false;
		});
	}
}
