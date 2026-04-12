package com.kantara.ai;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AiService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI apiUri;
    private final boolean simulateCall;

    public AiService() {
        this(HttpClient.newHttpClient(), URI.create("https://api.example.com/v1/insights"), true);
    }

    public AiService(String apiUrl) {
        this(HttpClient.newHttpClient(), URI.create(apiUrl), false);
    }

    public AiService(HttpClient httpClient, URI apiUri, boolean simulateCall) {
        this.httpClient = httpClient;
        this.apiUri = apiUri;
        this.simulateCall = simulateCall;
        this.objectMapper = new ObjectMapper();
    }

    public String generateInsights(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("payload must not be null or empty");
        }

        try {
            String payloadJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            String prompt = buildPrompt(payloadJson);

            if (simulateCall) {
                return buildSimulatedResponse(payload);
            }

            String requestBody = objectMapper.writeValueAsString(Map.of("prompt", prompt));

            HttpRequest request = HttpRequest.newBuilder(apiUri)
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            throw new IllegalStateException(
                    "AI API call failed. Status: " + response.statusCode() + ", Body: " + response.body()
            );
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to convert payload or request to JSON", e);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while calling AI API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI API call was interrupted", e);
        }
    }

    private String buildPrompt(String payloadJson) {
        return """
                You are a senior business analyst.

                Analyze the following structured data:

                %s

                STRICT INSTRUCTIONS:
                - Return ONLY valid JSON.
                - Do NOT include any text before or after the JSON.
                - Do NOT include explanations.

                REQUIREMENTS:
                1. key_insights:
                - Provide 3-5 insights.
                - Each insight MUST reference actual data (numbers, categories, trends).
                - Each insight MUST be specific (no generic statements).

                2. presentation:
                - Generate EXACTLY 5 slides.
                - Each slide must include "title" and "bullets" (3-5 bullet points).

                3. Slides structure:
                - Slide 1: Executive Summary
                - Slide 2: Key Metrics
                - Slide 3: Trends / Analysis
                - Slide 4: Risks / Issues
                - Slide 5: Recommendations

                OUTPUT FORMAT:
                {
                  "key_insights": [
                    "...",
                    "...",
                    "..."
                  ],
                  "presentation": [
                    {
                      "title": "...",
                      "bullets": ["...", "..."]
                    }
                  ]
                }
                """.formatted(payloadJson);
    }

    private String buildSimulatedResponse(Map<String, Object> payload) throws JacksonException {
        Map<String, Object> kpis = asMap(payload.get("kpis"));

        String source = String.valueOf(payload.getOrDefault("source", "uploaded dataset"));
        String period = String.valueOf(payload.getOrDefault("period", "N/A"));

        double revenue = asDouble(kpis.get("revenue"), 0.0);
        double cost = asDouble(kpis.get("cost"), 0.0);
        double churnRatio = asDouble(kpis.get("customer_churn"), 0.0);
        double margin = revenue - cost;
        double marginPct = revenue > 0 ? (margin / revenue) * 100.0 : 0.0;
        int highlightsCount = payload.get("highlights") instanceof List<?> h ? h.size() : 0;

        List<String> insights = new ArrayList<>();
        insights.add(String.format(
                Locale.ROOT,
                "In %s, revenue is %.2f and cost is %.2f, resulting in a gross margin of %.2f (%.1f%%).",
                period, revenue, cost, margin, marginPct
        ));
        insights.add(String.format(
                Locale.ROOT,
                "Customer churn is %.1f%%, implying an estimated retention level of %.1f%% for the same period.",
                churnRatio * 100.0, (1.0 - churnRatio) * 100.0
        ));
        insights.add(String.format(
                Locale.ROOT,
                "The payload includes %d KPI entries and %d highlight items, indicating a compact but decision-ready dataset.",
                kpis.size(), highlightsCount
        ));
        if (highlightsCount > 0 && payload.get("highlights") instanceof List<?> highlights) {
            insights.add("Highlighted business signal: " + String.valueOf(highlights.get(0)) + ".");
        }

        List<Map<String, Object>> slides = List.of(
                Map.of(
                        "title", "Executive Summary",
                        "bullets", List.of(
                                String.format(Locale.ROOT, "Source: %s", source),
                                String.format(Locale.ROOT, "Period analyzed: %s", period),
                                String.format(Locale.ROOT, "Revenue %.2f vs cost %.2f", revenue, cost)
                        )
                ),
                Map.of(
                        "title", "Key Metrics",
                        "bullets", List.of(
                                String.format(Locale.ROOT, "Revenue: %.2f", revenue),
                                String.format(Locale.ROOT, "Cost: %.2f", cost),
                                String.format(Locale.ROOT, "Gross margin: %.2f", margin),
                                String.format(Locale.ROOT, "Customer churn: %.1f%%", churnRatio * 100.0)
                        )
                ),
                Map.of(
                        "title", "Trends / Analysis",
                        "bullets", List.of(
                                String.format(Locale.ROOT, "Margin rate is %.1f%%, showing current profitability profile.", marginPct),
                                String.format(Locale.ROOT, "Retention estimate is %.1f%% based on churn data.", (1.0 - churnRatio) * 100.0),
                                String.format(Locale.ROOT, "Dataset trend signals captured in %d highlight statements.", highlightsCount)
                        )
                ),
                Map.of(
                        "title", "Risks / Issues",
                        "bullets", List.of(
                                String.format(Locale.ROOT, "Churn at %.1f%% is the primary growth risk.", churnRatio * 100.0),
                                String.format(Locale.ROOT, "Cost base of %.2f can pressure margin if revenue growth slows.", cost),
                                "Limited KPI breadth may hide secondary operational risks."
                        )
                ),
                Map.of(
                        "title", "Recommendations",
                        "bullets", List.of(
                                "Prioritize churn reduction initiatives in the next planning cycle.",
                                "Protect margin by monitoring cost-to-revenue ratio monthly.",
                                "Expand KPI coverage (segment, channel, region) for deeper diagnostics."
                        )
                )
        );

        Map<String, Object> simulated = Map.of(
                "key_insights", insights,
                "presentation", slides
        );

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(simulated);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
