package com.citext.pdf;

import java.io.File;
import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

public class BibUtils {

	public static String readPdf(File file) throws IOException {
		return readPdf(file.getAbsolutePath());
	}

	public static String readPdf(String fileName) throws IOException {
		PdfReader reader = new PdfReader(fileName);
		int n = reader.getNumberOfPages();

		StringBuilder result = new StringBuilder();
		for (int i = 1; i <= n; i++) {
			result.append(PdfTextExtractor.getTextFromPage(reader, i, new SimpleTextExtractionStrategy()));
		}

		return result.toString();
	}

}
