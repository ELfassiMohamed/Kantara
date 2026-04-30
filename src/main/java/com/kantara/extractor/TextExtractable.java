package com.kantara.extractor;

import java.util.List;

public interface TextExtractable {
    String extractText(String filePath);
    List<String> extractSections(String text);
}
