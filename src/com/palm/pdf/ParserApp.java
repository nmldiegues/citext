package com.palm.pdf;

import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

/*
 * TODO deal with [1-4] [1, 4, 2]
 * TODO fetch the actual citation to the bibtex
 * TODO parse the authors and paper name from the bibtex
 */
public class ParserApp {

	private static int lastIdxParsed = 0;

	public static void main(String[] args) throws Exception {
		String str = readPdf("article.pdf");

		Integer bibNum9 = findCitation(str);
		System.out.println(retrieveBibtex(str, bibNum9));

		Integer bibNum1 = findCitation(str);
		System.out.println(retrieveBibtex(str, bibNum1));

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

	public static String filterStringSequence(String input, String... seqs) {
		for (String seq : seqs) {
			int nextNewLine = -1;
			while ((nextNewLine = input.indexOf(seq)) != -1) {
				input = input.substring(0, nextNewLine) + input.substring(nextNewLine + seq.length());
			}
		}
		return input;
	}

	public static boolean isCitation(String input) {
		try {
			Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static Integer findCitation(String input) throws NoMoreCitationException {
		while (true) {
			String result = filterStringSequence(findCitationAux(input), "-\n", "-", "\n");
			if (isCitation(result)) {
				return Integer.parseInt(result);
			}
		}
	}

	private static String findCitationAux(String input) throws NoMoreCitationException {
		input = input.substring(lastIdxParsed);
		int openBracketIdx = input.indexOf('[');
		if (openBracketIdx == -1) {
			throw new NoMoreCitationException();
		}
		int closeBracketIdx = input.substring(openBracketIdx).indexOf(']');
		if (closeBracketIdx == -1) {
			throw new NoMoreCitationException();
		}
		lastIdxParsed += openBracketIdx + closeBracketIdx;
		return input.substring(openBracketIdx + 1, openBracketIdx + closeBracketIdx);
	}

	private static int findReferences(String input) {
		return input.lastIndexOf("References");
	}

	public static String retrieveBibtex(String input, Integer bibNum) {
		input = input.substring(findReferences(input));
		int bibIdx = input.indexOf("[" + bibNum + "]");
		int endIdx = input.indexOf("[" + (bibNum + 1) + "]");

		if (endIdx == -1) {
			endIdx = input.length() - 1;
		}

		return filterStringSequence(input.substring(bibIdx, endIdx), "-\n", "-").replace('\n', ' ');
	}

	private static class NoMoreCitationException extends Exception {

		private static final long serialVersionUID = 2063675414353506143L;

	}
}
