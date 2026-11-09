package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Observation;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Monitor;
import org.lazberry.xmaslegacy.framework.monitor.Monitored;

import java.util.UUID;

@Slf4j
@Components
public class ProductProxySignal implements Monitored {
    private static final String channel = ProductHandler.channel;
    private final ProductRecordManager productManager;

    @Inject
    public ProductProxySignal(ProductRecordManager productManager) {
        this.productManager = productManager;
    }

    @Monitor(lvl = Observation.AVERAGE)
    public void transmitProxyGrantSignal(UUID uuid, ProductType productType) {
        WebProduct product = new WebProduct(uuid, productType);
        product.setGranted(false);

        productManager.addProductAsPended(product);

        JedisService.publish(channel, product.toJson());
        log.info("Web product signal sent by Proxy. (receiver: {}, type: {})", uuid, productType.name());
    }
}
