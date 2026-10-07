package com.nagendra.platform.upstox;


import com.nagendra.platform.exception.MarketDataException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class UpstoxSubscriptionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] buildSubscribeMessage(List<String> instrumentKeys, String mode) {
        return buildMessage("sub", instrumentKeys, mode);
    }

    public byte[] buildUnsubscribeMessage(List<String> instrumentKeys) {
        return buildMessage("unsub", instrumentKeys, null);
    }

    private byte[] buildMessage(String method, List<String> instrumentKeys, String mode) {
        try {
            Map<String, Object> data = mode != null
                    ? Map.of("mode", mode, "instrumentKeys", instrumentKeys)
                    : Map.of("instrumentKeys", instrumentKeys);

            Map<String, Object> payload = Map.of(
                    "guid", UUID.randomUUID().toString(),
                    "method", method,
                    "data", data
            );
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception e) {
      throw new MarketDataException("Failed to build Upstox subscription payload", e);
        }
    }
}
