package com.kantara.cli;

import com.kantara.extractor.PdfExtractor;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		String filePath = "data/report.pdf";
		PdfExtractor extractor = new PdfExtractor();

		try {
			String cleanedText = extractor.extractText(filePath);
			List<String> sections = extractor.extractSections(cleanedText);

			System.out.println("Cleaned text length: " + cleanedText.length());
			System.out.println("Sections found: " + sections.size());
			for (int i = 0; i < sections.size(); i++) {
				System.out.printf("--- Section %d ---%n%s%n%n", i + 1, sections.get(i));
			}
		} catch (Exception e) {
			System.err.println("PDF extraction failed: " + e.getMessage());
		}
	}

}
