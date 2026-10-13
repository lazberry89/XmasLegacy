package org.lazberry.xmaslegacy.inquiry;

import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = ServerType.GLOBAL)
public class InquiryCooldownResetListener implements Listener {

}
