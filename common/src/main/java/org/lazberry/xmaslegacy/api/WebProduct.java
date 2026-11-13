package org.lazberry.xmaslegacy.api;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;

import java.util.UUID;

@Getter
@ConsumableClass
public class WebProduct {
    private static final Gson GSON = new Gson();

    /** Purchase number. Issued by the database (AUTO_INCREMENT), unique per purchase. */
    private final long id;
    private final UUID receiver;
    private final ProductType product;

    @Setter
    private boolean isGranted = false;

    public WebProduct(long id, UUID receiver, ProductType product) {
        this.id = id;
        this.receiver = receiver;
        this.product = product;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static WebProduct fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, WebProduct.class);
    }
}
