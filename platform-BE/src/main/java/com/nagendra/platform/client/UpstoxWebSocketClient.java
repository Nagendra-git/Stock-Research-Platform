package com.nagendra.platform.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.upstox.marketdatafeeder.rpc.proto.FeedResponse;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around a single OkHttp WebSocket connection to a
 * one-time Upstox authorized_redirect_uri. Stateless w.r.t. reconnects -
 * the owning UpstoxMarketDataManager creates a new instance (with a
 * freshly authorized URL) for every connection attempt.
 */
@Component
public class UpstoxWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(UpstoxWebSocketClient.class);

    private final OkHttpClient httpClient;
    private volatile WebSocket webSocket;

    public UpstoxWebSocketClient() {
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // streaming connection, no read timeout
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
    }

    public void connect(String url,
                        Runnable onOpen,
                        Consumer<FeedResponse> onFeed,
                        Runnable onClosed,
                        Consumer<Throwable> onFailure) {
        Request request = new Request.Builder().url(url).build();

        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                log.info("Upstox WebSocket connected");
                onOpen.run();
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {
                try {
                    FeedResponse response = FeedResponse.parseFrom(bytes.toByteArray());
                    onFeed.accept(response);
                } catch (InvalidProtocolBufferException e) {
                    log.error("Failed to parse protobuf FeedResponse: {}", e.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                // Upstox V3 sends binary protobuf only; unexpected text frame.
                log.warn("Received unexpected text frame from Upstox WebSocket (ignored)");
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                log.info("Upstox WebSocket closing: code={}, reason={}", code, reason);
                ws.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                log.info("Upstox WebSocket closed: code={}, reason={}", code, reason);
                onClosed.run();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                log.error("Upstox WebSocket failure: {}", t.getMessage());
                onFailure.accept(t);
            }
        });
    }

    public boolean sendBinary(byte[] payload) {
        WebSocket ws = this.webSocket;
        if (ws == null) {
            return false;
        }
        return ws.send(ByteString.of(payload));
    }

    public void close() {
        WebSocket ws = this.webSocket;
        if (ws != null) {
            ws.close(1000, "client shutdown");
            webSocket = null;
        }
    }
}