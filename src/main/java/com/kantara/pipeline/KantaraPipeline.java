package com.kantara.pipeline;

import com.kantara.ai.AiResponse;
import com.kantara.ai.AiService;
import com.kantara.ai.PayloadBuilder;
import com.kantara.config.Config;
import com.kantara.exception.ExtractionException;
import com.kantara.extractor.DataExtractor;
import com.kantara.extractor.PdfExtractor;
import com.kantara.generator.PptGenerator;
import com.kantara.generator.PresentationGenerator;
import com.kantara.ai.InsightGenerator;
import com.kantara.ai.PayloadAssembler;
import com.kantara.extractor.Extractable;
import com.kantara.extractor.TextExtractable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class KantaraPipeline {

    private static final Logger LOGGER = Logger.getLogger(KantaraPipeline.class.getName());

    private final Extractable dataExtractor;
    private final TextExtractable pdfExtractor;
    private final PayloadAssembler payloadBuilder;
    private final InsightGenerator aiService;
    private final PresentationGenerator pptGenerator;
    private boolean verbose;

    public KantaraPipeline() {
        this(new DataExtractor(), new PdfExtractor(), new PayloadBuilder(), new AiService(), new PptGenerator());
    }

    public KantaraPipeline(
            Extractable dataExtractor,
            TextExtractable pdfExtractor,
            PayloadAssembler payloadBuilder,
            InsightGenerator aiService,
            PresentationGenerator pptGenerator
    ) {
        this.dataExtractor = dataExtractor;
        this.pdfExtractor = pdfExtractor;
        this.payloadBuilder = payloadBuilder;
        this.aiService = aiService;
        this.pptGenerator = pptGenerator;
    }

    public void runPipeline(String excelPath, String pdfPath, String outputPath, Config config) {
        logInfo("Extracting documents in parallel...");
        List<Map<String, String>> excelData;
        List<String> pdfSections;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<Map<String, String>>> excelFuture =
                CompletableFuture.supplyAsync(() -> {
                    logInfo("Extracting Excel...");
                    List<Map<String, String>> data = dataExtractor.extract(excelPath);
                    if (data.isEmpty()) {
                        throw new ExtractionException("Excel data is empty: " + excelPath);
                    }
                    return data;
                }, executor);

            CompletableFuture<List<String>> pdfFuture =
                CompletableFuture.supplyAsync(() -> {
                    logInfo("Extracting PDF...");
                    String text = pdfExtractor.extractText(pdfPath);
                    List<String> sections = pdfExtractor.extractSections(text);
                    if (sections.isEmpty()) {
                        LOGGER.warning("[Kantara] PDF content is empty. Continuing without PDF sections.");
                    }
                    return sections;
                }, executor);

            excelData = excelFuture.join();
            pdfSections = pdfFuture.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new ExtractionException("Extraction failed unexpectedly.", cause);
        }

        logInfo("Building payload...");
        Map<String, Object> payload = payloadBuilder.buildPayload(excelData, pdfSections);

        logInfo("Calling AI...");
        String rawAiResponse = aiService.generateInsights(payload, config);

        logInfo("Parsing AI response...");
        AiResponse aiResponse = aiService.parseAiResponse(rawAiResponse);

        logInfo("Generating PPT...");
        pptGenerator.generatePresentation(aiResponse.presentation(), outputPath);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private void logInfo(String message) {
        if (verbose) {
            LOGGER.info("[Kantara] " + message);
        }
    }
}
