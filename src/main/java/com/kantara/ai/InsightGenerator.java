package com.kantara.ai;

import com.kantara.config.Config;
import java.util.Map;

public interface InsightGenerator {
    String generateInsights(Map<String, Object> payload, Config config);
    AiResponse parseAiResponse(String rawResponse);
}
