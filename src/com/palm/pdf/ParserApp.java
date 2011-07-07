package com.palm.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

public class ParserApp {

	private static int lastIdxParsed = 0;

	public static void main(String[] args) throws Exception {
		PdfReader reader = new PdfReader("article.pdf");
		int n = reader.getNumberOfPages();
		System.out.println("N: " + n);

		// FIXME merge all pages into 1 huge string
		String str = PdfTextExtractor.getTextFromPage(reader, 1, new SimpleTextExtractionStrategy());
		System.out.println(str);

		System.out.println(filterCharacter(findCitation(str), '\n', '-'));
		System.out.println(filterCharacter(findCitation(str), '\n', '-'));
	}

	public static String filterCharacter(String input, Character... chars) {
		for (Character character : chars) {
			int nextNewLine = -1;
			while ((nextNewLine = input.indexOf(character)) != -1) {
				input = input.substring(0, nextNewLine) + input.substring(nextNewLine + 1);
			}
		}
		return input;
	}

	public static String findCitation(String input) throws NoMoreCitationException {
		input = input.substring(lastIdxParsed);
		int openBracketIdx = input.indexOf('[');
		if (openBracketIdx == -1) {
			throw new NoMoreCitationException();
		}
		int closeBracketIdx = input.substring(openBracketIdx).indexOf(']');
		if (closeBracketIdx == -1) {
			throw new NoMoreCitationException();
		}
		lastIdxParsed = openBracketIdx + closeBracketIdx;
		return input.substring(openBracketIdx + 1, openBracketIdx + closeBracketIdx);
	}

	private static class NoMoreCitationException extends RuntimeException {

	}
}
