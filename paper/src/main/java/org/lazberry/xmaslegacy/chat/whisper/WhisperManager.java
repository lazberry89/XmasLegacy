package org.lazberry.xmaslegacy.chat.whisper;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class WhisperManager {
    public static final String WHISPER_CHANNEL = "whisper_global";

    public void sendWhisper(String name, String server, UUID target, String message) {

    }
}
