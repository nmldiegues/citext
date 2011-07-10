package com.palm.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

/*
 * TODO fetch all citations in the paper
 * TODO fetch the actual citation to the bibtex
 * TODO parse the authors and paper name from the bibtex
 * TODO ant build file. move articles to resources. create jar
 * TODO refactor to proper classes
 */
public class ParserApp {

	private static int lastIdxParsed = 0;

	public static void main(String[] args) throws Exception {
		String str = readPdf("article2.pdf");

		helperPrinter(str, findCitation(str));
		helperPrinter(str, findCitation(str));
		helperPrinter(str, findCitation(str));
		helperPrinter(str, findCitation(str));
		helperPrinter(str, findCitation(str));
	}

	public static void helperPrinter(String article, CitationMetadata citationMetadata) {
		for (Integer bibNum : citationMetadata.getReferencesUsed()) {
			System.out.println(retrieveBibtex(article, bibNum));
		}
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
			Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isMultipleCitation(String input, String separator) {
		for (String str : input.split(separator)) {
			if (!isCitation(str)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isMultipleCitationNested(String input, String outsideSep, String nestedSep) {
		for (String str : input.split(outsideSep)) {
			if (!isMultipleCitation(str, nestedSep)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Produces a list of numbers in the next citation found in the article.
	 * Supports the following types: [1] [1-3] [1, 4, 7] [1-3, 5, 8, 13-17, 21]
	 * With the following possible hyphens: - (45) – (150) — (151)
	 * 
	 * @param input
	 *            The article text.
	 * @return A list containing all the numbers in the next citation.
	 * @throws NoMoreCitationException
	 *             If there are no more citations in the text, given the
	 *             internal state kept regarding the cursor in the article.
	 */
	public static CitationMetadata findCitation(String input) throws NoMoreCitationException {
		String[] separators = { "-", "–", "—" };

		while (true) {
			CitationQuote quote = findCitationAux(input);
			String result = quote.getBibNum();
			if (isCitation(result)) {
				return new CitationMetadata(Collections.singletonList(Integer.parseInt(result)), "");
			}

			for (String sep : separators) {
				if (isMultipleCitationNested(result, ",", sep)) {
					List<Integer> listResult = new ArrayList<Integer>();
					for (String str : result.split(",")) {
						if (str.contains(sep)) {
							listResult.addAll(batchCitationNumbers(str, sep));
						} else {
							listResult.add(Integer.parseInt(str.trim()));
						}
					}
					return new CitationMetadata(listResult, "");
				}
			}

			if (isMultipleCitation(result, ",")) {
				List<Integer> listResult = new ArrayList<Integer>();
				for (String str : result.split(",")) {
					listResult.add(Integer.parseInt(str.trim()));
				}
				return new CitationMetadata(listResult, "");
			}

			for (String sep : separators) {
				if (isMultipleCitation(result, sep)) {
					return new CitationMetadata(batchCitationNumbers(result, sep), "");
				}
			}
		}
	}

	/**
	 * Given a citation containing interval of numbers, returns a list of all
	 * the numbers.
	 * 
	 * @param input
	 *            Assumes that the input consists of something like 1-4.
	 * @param separator
	 *            The actual separator of the number interval provided.
	 * @return List containing all integers given in the input interval.
	 */
	private static List<Integer> batchCitationNumbers(String input, String separator) {
		String[] bounds = input.split(separator);
		Integer lowerBound = Integer.parseInt(bounds[0].trim());
		Integer upperBound = Integer.parseInt(bounds[1].trim());
		List<Integer> listResult = new ArrayList<Integer>();
		listResult.add(lowerBound);
		for (int i = lowerBound + 1; i < upperBound; i++) {
			listResult.add(i);
		}
		listResult.add(upperBound);
		return listResult;
	}

	private static CitationQuote findCitationAux(String input) throws NoMoreCitationException {
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
		return new CitationQuote(filterStringSequence(
				input.substring(openBracketIdx + 1, openBracketIdx + closeBracketIdx), "-\n", "-", "\n"), "");
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

	private static class CitationQuote {
		private String bibNum;
		private String citation;

		public CitationQuote() {
		}

		public CitationQuote(String bibNum, String citation) {
			this.bibNum = bibNum;
			this.citation = citation;
		}

		public String getBibNum() {
			return bibNum;
		}

		public void setBibNum(String bibNum) {
			this.bibNum = bibNum;
		}

		public String getCitation() {
			return citation;
		}

		public void setCitation(String citation) {
			this.citation = citation;
		}

	}

	private static class CitationMetadata {
		private List<Integer> referencesUsed;
		private String citation;

		public CitationMetadata() {
			this.referencesUsed = new ArrayList<Integer>();
		}

		public CitationMetadata(List<Integer> referencesUsed, String citation) {
			this.referencesUsed = referencesUsed;
			this.citation = citation;
		}

		public List<Integer> getReferencesUsed() {
			return referencesUsed;
		}

		public void setReferencesUsed(List<Integer> referencesUsed) {
			this.referencesUsed = referencesUsed;
		}

		public void addReferenceUsed(Integer referenceUsed) {
			this.referencesUsed.add(referenceUsed);
		}

		public String getCitation() {
			return citation;
		}

		public void setCitation(String citation) {
			this.citation = citation;
		}

	}
}
