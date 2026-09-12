package org.lazberry.xmaslegacy.casino.logics;

import org.lazberry.xmaslegacy.casino.games.GameMachine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoManager {
    private final Map<String, GameMachine> machines = new ConcurrentHashMap<>();
}
