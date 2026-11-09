package org.lazberry.xmaslegacy.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;

import java.util.UUID;

@Getter
@ConsumableClass
public class WebProduct {
    private static final Gson gson = new GsonBuilder().create();

    private final UUID receiver;
    private final ProductType product;

    @Setter
    private boolean isGranted = false;

    public WebProduct(UUID receiver, ProductType product) {
        this.receiver = receiver;
        this.product = product;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static WebProduct fromJson(String src) {
        if (src == null || src.isEmpty()) return null;
        return gson.fromJson(src, WebProduct.class);
    }
}
