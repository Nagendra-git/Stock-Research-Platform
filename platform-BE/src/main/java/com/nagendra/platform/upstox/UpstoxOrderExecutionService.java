package com.nagendra.platform.upstox;

import com.nagendra.platform.config.TradingProperties;
import com.nagendra.platform.config.UpstoxMarketDataProperties;
import com.nagendra.platform.dto.order.OrderRequest;
import com.nagendra.platform.dto.order.OrderResult;
import com.nagendra.platform.enums.OrderType;
import com.nagendra.platform.service.OrderExecutionService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Places REAL orders against Upstox's V3 order API. This moves real
 * money - only active when trading.mode=LIVE.
 *
 * Verified against the published "Place Order V3" docs as of this
 * writing (https://upstox.com/developer/api-documentation/v3/place-order/):
 *
 *   POST https://api-hft.upstox.com/v3/order/place
 *   {
 *     "quantity": 25, "product": "D", "validity": "DAY", "price": 0,
 *     "tag": "string", "instrument_token": "NSE_EQ|INE669E01016",
 *     "order_type": "MARKET", "transaction_type": "BUY",
 *     "disclosed_quantity": 0, "trigger_price": 0, "is_amo": false,
 *     "slice": false
 *   }
 *
 * Note the host is api-hft.upstox.com (their low-latency order host),
 * NOT api.upstox.com which is used for the market-data-feed authorize
 * call elsewhere in this project. As with the .proto file, re-check the
 * docs page above before relying on this in production - Upstox can and
 * does change field sets between versions.
 */
@Slf4j
public class UpstoxOrderExecutionService implements OrderExecutionService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final TradingProperties tradingProperties;
    private final UpstoxMarketDataProperties upstoxProperties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UpstoxOrderExecutionService(TradingProperties tradingProperties,
                                       UpstoxMarketDataProperties upstoxProperties) {
        this.tradingProperties = tradingProperties;
        this.upstoxProperties = upstoxProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public OrderResult placeOrder(OrderRequest request) {
        String token = upstoxProperties.getAccessToken();
        if (token == null || token.isBlank()) {
            return OrderResult.rejected("Upstox access token is not configured");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("quantity", request.getQuantity());
        body.put("product", request.getProduct());
        body.put("validity", "DAY");
        body.put("price", request.getOrderType() == OrderType.LIMIT
                ? request.getPrice().doubleValue() : 0.0);
        body.put("tag", request.getTag() == null ? "auto-trader" : request.getTag());
        body.put("instrument_token", request.getInstrumentKey());
        body.put("order_type", request.getOrderType().name());
        body.put("transaction_type", request.getTransactionType().name());
        body.put("disclosed_quantity", 0);
        body.put("trigger_price", 0);
        body.put("is_amo", false);
        body.put("slice", false);

        Request httpRequest = new Request.Builder()
                .url(tradingProperties.getOrderPlaceUrl())
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Upstox order placement failed: HTTP {} - {}", response.code(), responseBody);
                return OrderResult.rejected("HTTP " + response.code() + ": " + responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            if (!"success".equals(root.path("status").asText())) {
                log.error("Upstox order placement rejected: {}", responseBody);
                return OrderResult.rejected(responseBody);
            }

            String orderId = root.path("data").path("order_id").asText(null);
            log.info("Upstox order accepted: {} {} x {} ({}) -> order_id={}",
                    request.getTransactionType(), request.getQuantity(), request.getInstrumentKey(),
                    request.getOrderType(), orderId);
            // MARKET order fill price is not known synchronously from this endpoint - the
            // reference price (last LTP) is stored as a best estimate until the real fill
            // is reconciled via the order/trade webhook or a GET /v2/order/details poll.
            BigDecimal estimatedFillPrice = request.getOrderType() == OrderType.LIMIT
                    ? request.getPrice() : request.getReferencePrice();
            return OrderResult.accepted(orderId, estimatedFillPrice);
        } catch (IOException e) {
            log.error("Network error placing Upstox order: {}", e.getMessage());
            return OrderResult.rejected("Network error: " + e.getMessage());
        }
    }
}
