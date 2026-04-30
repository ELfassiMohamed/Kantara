package com.kantara.extractor;

import java.util.List;
import java.util.Map;

public interface Extractable {
    List<Map<String, String>> extract(String filePath);
}
