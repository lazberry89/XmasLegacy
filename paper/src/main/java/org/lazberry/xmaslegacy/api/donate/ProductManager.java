package org.lazberry.xmaslegacy.api.donate;

import org.lazberry.xmaslegacy.api.donate.action.ProvideAction;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.util.EnumMap;
import java.util.Map;

@Registry.Exclude(type = ServerType.LOBBY)
public class ProductManager implements Initiator {
	private final Map<ProductType, ProvideAction> typeActions = new EnumMap<>(ProductType.class);

	@Override
	public void init() {

	}

	@Override
	public void close() {
		Initiator.super.close();
	}
}
