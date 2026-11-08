package org.lazberry.xmaslegacy.api.donate.action;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.lazberry.xmaslegacy.api.PermissionHandler;
import org.lazberry.xmaslegacy.api.donate.ProductType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
public class StarterPackAction extends ProvideAction {
    private final ProductType productType;

    public StarterPackAction() {
        this.productType = ProductType.STARTER_PACK;
    }

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

    }
}
