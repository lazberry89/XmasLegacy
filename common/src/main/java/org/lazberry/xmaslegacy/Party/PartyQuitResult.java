package org.lazberry.xmaslegacy.Party;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.List;
import java.util.UUID;

@ConsumableClass
public record PartyQuitResult(@NotNull UUID leaverUuid, @NotNull List<User> remainingMembers, @NotNull User newLeader) {}
