package com.kantara.ai;

import java.util.List;

public record Slide(String title, List<String> bullets) {
    public Slide {
        bullets = bullets == null ? List.of() : List.copyOf(bullets);
    }
}

