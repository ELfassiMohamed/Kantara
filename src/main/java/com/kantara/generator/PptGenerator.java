package com.kantara.generator;

import com.kantara.ai.Slide;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PptGenerator {

    private static final int MAX_SLIDES = 6;
    private static final int MAX_BULLETS_PER_SLIDE = 5;
    private static final int MAX_BULLET_WORDS = 12;
    private static final int MAX_BULLET_CHARS = 95;
    private static final double TITLE_FONT_SIZE = 32.0;
    private static final double BULLET_FONT_SIZE = 22.0;
    private static final double BULLET_SPACING_AFTER = 14.0;

    public void generatePresentation(List<Slide> slides, String outputPath) {
        if (slides == null || slides.isEmpty()) {
            throw new IllegalArgumentException("slides must not be null or empty");
        }
        if (isBlank(outputPath)) {
            throw new IllegalArgumentException("outputPath must not be blank");
        }

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            int createdSlides = 0;
            for (Slide slideData : slides) {
                if (createdSlides >= MAX_SLIDES) {
                    break;
                }

                Slide normalized = normalizeSlide(slideData);
                if (normalized == null) {
                    continue;
                }

                addSlide(ppt, normalized);
                createdSlides++;
            }

            if (createdSlides == 0) {
                throw new IllegalArgumentException("No valid slide content to generate.");
            }

            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                ppt.write(outputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate presentation: " + outputPath, e);
        }
    }

    public void generatePresentationFromMaps(List<Map<String, Object>> slides, String outputPath) {
        if (slides == null || slides.isEmpty()) {
            throw new IllegalArgumentException("slides must not be null or empty");
        }

        List<Slide> converted = new ArrayList<>();
        for (Map<String, Object> item : slides) {
            if (item == null) {
                continue;
            }
            converted.add(new Slide(safeString(item.get("title"), ""), extractBullets(item.get("bullets"))));
        }
        generatePresentation(converted, outputPath);
    }

    private void addSlide(XMLSlideShow ppt, Slide slideData) {
        XSLFSlide slide = ppt.createSlide();
        Dimension pageSize = ppt.getPageSize();
        addTitle(slide, slideData.title(), pageSize);
        addBullets(slide, slideData.bullets(), pageSize);
    }

    private void addTitle(XSLFSlide slide, String title, Dimension pageSize) {
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(40, 20, pageSize.width - 80, 60));
        titleBox.setVerticalAlignment(VerticalAlignment.MIDDLE);
        titleBox.setWordWrap(true);

        XSLFTextParagraph paragraph = titleBox.addNewTextParagraph();
        paragraph.setBullet(false);
        paragraph.setSpaceAfter(6.0);

        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(TITLE_FONT_SIZE);
        run.setFontColor(new Color(33, 37, 41));
    }

    private void addBullets(XSLFSlide slide, List<String> bullets, Dimension pageSize) {
        XSLFTextBox bulletBox = slide.createTextBox();
        bulletBox.setAnchor(new Rectangle(60, 100, pageSize.width - 120, pageSize.height - 140));
        bulletBox.setWordWrap(true);
        bulletBox.setVerticalAlignment(VerticalAlignment.TOP);

        for (String bullet : bullets) {
            XSLFTextParagraph paragraph = bulletBox.addNewTextParagraph();
            paragraph.setBullet(true);
            paragraph.setLeftMargin(18.0);
            paragraph.setIndent(-8.0);
            paragraph.setSpaceAfter(BULLET_SPACING_AFTER);

            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(bullet);
            run.setFontSize(BULLET_FONT_SIZE);
            run.setFontColor(new Color(44, 62, 80));
        }
    }

    private Slide normalizeSlide(Slide original) {
        if (original == null) {
            return null;
        }

        String title = safeString(original.title(), "");
        if (isBlank(title)) {
            return null;
        }

        List<String> normalizedBullets = new ArrayList<>();
        if (original.bullets() != null) {
            for (String bullet : original.bullets()) {
                String cleaned = limitBulletLength(bullet);
                if (!isBlank(cleaned)) {
                    normalizedBullets.add(cleaned);
                }
                if (normalizedBullets.size() >= MAX_BULLETS_PER_SLIDE) {
                    break;
                }
            }
        }

        if (normalizedBullets.isEmpty()) {
            return null;
        }

        return new Slide(title, normalizedBullets);
    }

    private String limitBulletLength(String bullet) {
        String value = safeString(bullet, "");
        if (isBlank(value)) {
            return "";
        }

        String[] words = value.split("\\s+");
        String compactByWords = words.length <= MAX_BULLET_WORDS
                ? value
                : String.join(" ", Arrays.asList(words).subList(0, MAX_BULLET_WORDS)) + "...";

        if (compactByWords.length() <= MAX_BULLET_CHARS) {
            return compactByWords;
        }

        int boundary = Math.max(0, MAX_BULLET_CHARS - 3);
        return compactByWords.substring(0, boundary).trim() + "...";
    }

    private List<String> extractBullets(Object bulletsObject) {
        if (!(bulletsObject instanceof List<?> rawList) || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> bullets = new ArrayList<>();
        for (Object item : rawList) {
            String text = safeString(item, "");
            if (!isBlank(text)) {
                bullets.add(text);
            }
            if (bullets.size() >= MAX_BULLETS_PER_SLIDE) {
                break;
            }
        }
        return bullets;
    }

    private String safeString(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
