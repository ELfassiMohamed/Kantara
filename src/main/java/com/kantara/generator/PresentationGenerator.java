package com.kantara.generator;

import com.kantara.ai.Slide;
import java.util.List;

public interface PresentationGenerator {
    void generatePresentation(List<Slide> slides, String outputPath);
}
