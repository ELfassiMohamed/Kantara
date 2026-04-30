package com.kantara.ai;

import java.util.List;
import java.util.Map;

public interface PayloadAssembler {
    Map<String, Object> buildPayload(List<Map<String, String>> excelData, List<String> pdfSections);
}
