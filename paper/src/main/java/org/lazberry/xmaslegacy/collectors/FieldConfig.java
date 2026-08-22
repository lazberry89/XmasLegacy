package org.lazberry.xmaslegacy.collectors;

import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Include(type = ServerType.MAIN)
public class FieldConfig implements Initiator {

	@Override
	public void init() {

	}

	@Override
	public void close() {
		Initiator.super.close();
	}
}
