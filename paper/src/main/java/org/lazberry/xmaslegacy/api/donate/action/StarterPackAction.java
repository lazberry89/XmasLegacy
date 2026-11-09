package org.lazberry.xmaslegacy.api.donate.action;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.lazberry.xmaslegacy.api.PermissionHandler;
import org.lazberry.xmaslegacy.api.ProductType;
import org.lazberry.xmaslegacy.framework.Observation;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Monitor;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.monitor.Monitored;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class StarterPackAction extends ProvideAction implements Monitored {

    @Getter
    private final ProductType productType;

    public StarterPackAction() {
        this.productType = ProductType.STARTER_PACK;
    }

    @Monitor(lvl = Observation.AVERAGE)
    public CompletableFuture<Boolean> grantProduct(UUID uuid) {
        if (PermissionHandler.hasPermission(uuid, productType.name())) {
            return CompletableFuture.completedFuture(true);
        }
        return productType.grantPermission(uuid).whenComplete((success, ex) -> {
            if (ex != null) {
                log.error("Failed to grant product '{}' to player({}).", productType.name(), uuid, ex);
                return;
            }
            if (!success) {
                log.error("Failed of LuckPerm save process. Log will be appeared.");
                return;
            }
            var target = Bukkit.getPlayer(uuid);

            if (target != null && target.isOnline()) {

            }
        });
    }

    public CompletableFuture<Boolean> revokeProduct(UUID uuid) {
        return CompletableFuture.completedFuture(true);
    }
}
