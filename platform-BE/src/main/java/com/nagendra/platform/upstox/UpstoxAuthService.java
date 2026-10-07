package com.nagendra.platform.upstox;


import com.nagendra.platform.config.UpstoxMarketDataProperties;
import com.nagendra.platform.exception.MarketDataException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Calls GET /v3/feed/market-data-feed/authorize to obtain a fresh,
 * one-time authorized_redirect_uri. This MUST be called again on every
 * reconnect - the URI cannot be reused.
 */
@Service
@RequiredArgsConstructor
public class UpstoxAuthService {

    private static final Logger log = LoggerFactory.getLogger(UpstoxAuthService.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpstoxMarketDataProperties properties;


    public String fetchAuthorizedWebSocketUrl() {
        String token = properties.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new MarketDataException("Upstox access token is not configured");
        }

        Request request = new Request.Builder()
                .url(properties.getMarketData().getAuthorizeUrl())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Never log the token itself
                throw new MarketDataException("Upstox authorize call failed with status " + response.code());
            }
            String body = response.body() != null ? response.body().string() : null;
            if (body == null) {
                throw new MarketDataException("Upstox authorize call returned an empty body");
            }
            JsonNode root = objectMapper.readTree(body);
            if (!"success".equals(root.path("status").asText())) {
                throw new MarketDataException("Upstox authorize call did not return status=success");
            }
            String wsUrl = root.path("data").path("authorized_redirect_uri").asText(null);
            if (wsUrl == null || wsUrl.isBlank()) {
                throw new MarketDataException("Upstox authorize response missing authorized_redirect_uri");
            }
            log.info("Obtained fresh Upstox authorized WebSocket URL");
            return wsUrl;
        } catch (IOException e) {
            throw new MarketDataException("Network error while calling Upstox authorize endpoint", e);
        }
    }
}
