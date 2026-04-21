package com.kantara.ai;

import java.util.List;

public record AiResponse(List<String> keyInsights, List<Slide> presentation) {
    public AiResponse {
        keyInsights = keyInsights == null ? List.of() : List.copyOf(keyInsights);
        presentation = presentation == null ? List.of() : List.copyOf(presentation);
    }
}

