package org.lazberry.xmaslegacy.wep;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class WebApiClient {
	private static final String API_URL = "https://api.xmaslegacy.xyz/api/";
	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();
	@Getter
	private final String internalKey;

	@Getter
	private final int timeoutSeconds;

	@Inject
	public WebApiClient(@ConfigValue(path = "web.api_internal_key") String key,
	                    @ConfigValue(path = "web.timeout_seconds") Integer seconds) {
		this.internalKey = key == null ? "akjefjc8w49vm9wvm2w449v7" : key;
		this.timeoutSeconds = seconds == null ? 5 : seconds;
	}

	public static CompletableFuture<ServerInfo> getServerInfo() {
		return requestServerInfo().handle((s, ex) -> {
			if (s == null) return null;
			try {
				JsonObject root = JsonParser.parseString(s).getAsJsonObject();

				String name = root.has("name") ? root.get("name").getAsString() : "Unknown";
				String version = root.has("version") ? root.get("version").getAsString() : "Unknown";
				int onlineCount = root.has("onlineCount") ? root.get("onlineCount").getAsInt() : 0;
				int preregCount = root.has("preregCount") ? root.get("preregCount").getAsInt() : 0;
				boolean addressRevealed = root.has("addressRevealed") && root.get("addressRevealed").getAsBoolean();

				String address = (addressRevealed && root.has("address")) ? root.get("address").getAsString() : null;

				return new ServerInfo(name, version, onlineCount, preregCount, addressRevealed, address);
			} catch (Exception e) {
				log.error("Failed to parse server info JSON.", e);
				return null;
			}
		});
	}

	private static CompletableFuture<String> fetchRequestAndCatch(HttpRequest request, HttpResponse.BodyHandler<String> responseBodyHandler) {
		return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(response -> {
					int resultCode = response.statusCode();
					if (resultCode == 200) return response.body();
					else {
						log.error("Internal Http Error occurred. (Error code: {})", resultCode);
						return null;
					}
				}).exceptionally(ex -> {
					log.error("Failed to fetch Http request.", ex);
					return null;
				});
	}

	private static CompletableFuture<String> requestServerInfo() {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_URL + "server/info"))
				.header("Accept", "application/json")
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();

		return fetchRequestAndCatch(request, HttpResponse.BodyHandlers.ofString());
	}

	public CompletableFuture<Void> updateOnlinePlayerCount(int current, int max) {
		JsonObject json = new JsonObject();
		json.addProperty("count", current);
		json.addProperty("max", max);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_URL + "internal/online"))
				.header("Content-Type", "application/json")
				.header("X-Internal-Key", internalKey)
				.timeout(Duration.ofSeconds(3))
				.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
				.build();
		return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
				.thenAccept(response -> {
					if (response.statusCode() != 200) {
						log.warn("Failed to update online count. Status code: {}", response.statusCode());
					}
				})
				.exceptionally(ex -> {
					log.error("Exception occurred while updating online count to web api", ex);
					return null;
				});
	}

	/**
	 * Returns list of immutable player information instance. It calls Http request with
	 * async, and also throws exception when Parsing JsonArray of Https Request fails.
	 * This method always return empty list when fails except JsonArray parsing key is not valid. In that case,
	 * current Plugin({@link org.lazberry.xmaslegacy.XmasLegacy}) cannot handle it,
	 * so that's why this method throws {@link IllegalStateException}.
	 * @see HttpRequest
	 * @see WebApiClient#fetchPreregisterList(int)
	 * @param limit size of information list about preregistered players.
	 *              Max size is 100, it'll be clamped with 1 and 100 if value is not valid.
	 * @return completableFuture of PreregisteredPlayer list.
	 * @author lazberry89
	 */
	public static CompletableFuture<List<PreregisteredPlayer>> getPreregisteredPlayers(int limit) {
		return fetchPreregisterList(limit).handle((s, ex) -> {
			List<PreregisteredPlayer> result = new ArrayList<>();
			if (ex != null || s == null) {
				log.error("Failed to fetch preregistered players.", ex);
				return List.of();
			}

			JsonObject root = JsonParser.parseString(s).getAsJsonObject();
			JsonArray jsonArray = root.getAsJsonArray("players");

			//This part: fails to parse array with given key.
			if (jsonArray == null) {
				throw new IllegalStateException("Json Array of 'players' is null." +
						" Listing PreregisteredPlayers failed.");
			}

			for (JsonElement element : jsonArray) {
				try {
					JsonObject player = element.getAsJsonObject();
					UUID uuid = UUID.fromString(player.get("uuid").getAsString());
					String name = player.get("name").getAsString();
					long registeredAt = player.get("registeredAt").getAsLong();

					result.add(new PreregisteredPlayer(uuid, name, registeredAt));
				} catch (Exception e) {
					log.error("Failed to fetch preregistered players. Internal Json value is Invalid.", e);
				}
			}
			return result;
		});
	}

	private static CompletableFuture<String> fetchPreregisterList(int limit) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_URL + "prereg/list?offset=0&limit=" + Math.clamp(limit, 1, 100)))
				.timeout(Duration.ofSeconds(8))
				.header("Accept", "application/json")
				.GET()
				.build();
		return fetchRequestAndCatch(request, HttpResponse.BodyHandlers.ofString());
	}
}

