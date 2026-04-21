package com.kantara.cli;

import com.kantara.ai.AiResponse;
import com.kantara.ai.AiService;
import com.kantara.ai.PayloadBuilder;
import com.kantara.extractor.DataExtractor;
import com.kantara.extractor.PdfExtractor;
import com.kantara.generator.PptGenerator;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private final DataExtractor dataExtractor;
    private final PdfExtractor pdfExtractor;
    private final PayloadBuilder payloadBuilder;
    private final AiService aiService;
    private final PptGenerator pptGenerator;

    public Main() {
        this(new DataExtractor(), new PdfExtractor(), new PayloadBuilder(), new AiService(), new PptGenerator());
    }

    public Main(
            DataExtractor dataExtractor,
            PdfExtractor pdfExtractor,
            PayloadBuilder payloadBuilder,
            AiService aiService,
            PptGenerator pptGenerator
    ) {
        this.dataExtractor = dataExtractor;
        this.pdfExtractor = pdfExtractor;
        this.payloadBuilder = payloadBuilder;
        this.aiService = aiService;
        this.pptGenerator = pptGenerator;
    }

    public static void main(String[] args) {
        String excelPath = args.length > 0 ? args[0] : "data/actors.xlsx";
        String pdfPath = args.length > 1 ? args[1] : "data/report.pdf";
        String outputPath = args.length > 2 ? args[2] : "output.pptx";

        try {
            new Main().runPipeline(excelPath, pdfPath, outputPath);
            logInfo("Pipeline completed successfully: " + outputPath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[Kantara] Pipeline failed: " + e.getMessage(), e);
        }
    }

    public void runPipeline(String excelPath, String pdfPath, String outputPath) {
        logInfo("Extracting Excel...");
        List<Map<String, String>> excelData = dataExtractor.extract(excelPath);
        if (excelData.isEmpty()) {
            throw new IllegalStateException("Excel data is empty: " + excelPath);
        }

        logInfo("Extracting PDF...");
        String pdfText = pdfExtractor.extractText(pdfPath);
        List<String> pdfSections = pdfExtractor.extractSections(pdfText);
        if (pdfSections.isEmpty()) {
            LOGGER.warning("[Kantara] PDF content is empty. Continuing without PDF sections.");
        }

        logInfo("Building payload...");
        Map<String, Object> payload = payloadBuilder.buildPayload(excelData, pdfSections);

        logInfo("Calling AI...");
        String rawAiResponse = aiService.generateInsights(payload);

        logInfo("Parsing AI response...");
        AiResponse aiResponse;
        try {
            aiResponse = aiService.parseAiResponse(rawAiResponse);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("AI response is invalid: " + e.getMessage(), e);
        }

        logInfo("Generating PPT...");
        pptGenerator.generatePresentation(aiResponse.presentation(), outputPath);
    }

    private static void logInfo(String message) {
        LOGGER.info("[Kantara] " + message);
    }
}

