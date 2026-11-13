package org.lazberry.xmaslegacy.api;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Console command the web store runs after a payment:
 * <pre>donation action:GRANT target:&lt;uuid&gt; product:&lt;product&gt;</pre>
 * Arguments are {@code key:value} pairs in any order. {@code product} accepts the product key
 * ({@code starter_pack}) or the enum name ({@code STARTER_PACK}).
 */
@Slf4j
@Commands(command = "donation")
public class DonationCommand implements SimpleCommand {
    private static final String USAGE = "Usage: donation action:GRANT target:<uuid> product:<product>";

    private final ProductProxySignal signal;

    @Inject
    public DonationCommand(ProductProxySignal signal) {
        this.signal = signal;
    }

    /** Hands out paid products, so only the console (where the web store runs it) may use it. */
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source() instanceof ConsoleCommandSource;
    }

    @Override
    public void execute(Invocation invocation) {
        Map<String, String> args = new HashMap<>();
        for (String arg : invocation.arguments()) {
            int colon = arg.indexOf(':');
            if (colon > 0) args.put(arg.substring(0, colon).toLowerCase(), arg.substring(colon + 1));
        }

        String action = args.get("action");
        String target = args.get("target");
        String productArg = args.get("product");
        if (action == null || target == null || productArg == null) {
            fail(invocation, USAGE);
            return;
        }

        if (!action.equalsIgnoreCase("GRANT")) {
            fail(invocation, "Unknown action: " + action + " (supported: GRANT)");
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(target);
        } catch (IllegalArgumentException e) {
            fail(invocation, "Invalid uuid: " + target);
            return;
        }

        ProductType product = findProduct(productArg);
        if (product == null) {
            fail(invocation, "Unknown product: " + productArg);
            return;
        }

        signal.transmitProxyGrantSignal(uuid, product);
        invocation.source().sendMessage(Component.text(
                "Donation accepted. (receiver: " + uuid + ", product: " + product.name() + ")", NamedTextColor.GREEN));
    }

    private static ProductType findProduct(String value) {
        for (ProductType type : ProductType.values()) {
            if (type.getKey().equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) return type;
        }
        return null;
    }

    private static void fail(Invocation invocation, String message) {
        // Logged too, so a misconfigured store command is visible even if nobody watches the console.
        log.warn("Rejected donation command '{}': {}", String.join(" ", invocation.arguments()), message);
        invocation.source().sendMessage(Component.text(message, NamedTextColor.RED));
    }
}
