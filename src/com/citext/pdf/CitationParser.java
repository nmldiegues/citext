package com.citext.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CitationParser {

	private final String article;
	private int lastIdxParsed;

	public CitationParser(String article) {
		this.article = article;
		this.lastIdxParsed = 0;
	}

	public List<CitationMetadata> fetchAllCitations() {
		List<CitationMetadata> metadatas = new ArrayList<CitationMetadata>();
		while (true) {
			try {
				CitationMetadata citationMetadata = findNextCitation();
				for (Integer bibNum : citationMetadata.getBibNums()) {
					citationMetadata.putReference(bibNum, retrieveBibtex(bibNum));
				}
				metadatas.add(citationMetadata);
			} catch (NoMoreCitationException e) {
				break;
			}
		}
		return metadatas;
	}

	public String filterStringSequence(String input, String... seqs) {
		for (String seq : seqs) {
			int nextNewLine = -1;
			while ((nextNewLine = input.indexOf(seq)) != -1) {
				input = input.substring(0, nextNewLine) + input.substring(nextNewLine + seq.length());
			}
		}
		return input;
	}

	private boolean isSingleCitation(String input) {
		try {
			Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private boolean isMultipleCitation(String input, String separator) {
		for (String str : input.split(separator)) {
			if (!isSingleCitation(str)) {
				return false;
			}
		}
		return true;
	}

	private boolean isMultipleCitationNested(String input, String outsideSep, String nestedSep) {
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
	public CitationMetadata findNextCitation() throws NoMoreCitationException {
		String[] separators = { "-", "–", "—" };

		while (true) {
			CitationMetadata metadata = findCitationAux(article);
			String result = metadata.getBibNumsStr();
			if (isSingleCitation(result)) {
				metadata.setBibNums(Collections.singletonList(Integer.parseInt(result)));
				if (isLastCitationFor(article, metadata.getBibNums().get(0))) {
					continue;
				}
				return metadata;
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
					metadata.setBibNums(listResult);
					return metadata;
				}
			}

			if (isMultipleCitation(result, ",")) {
				List<Integer> listResult = new ArrayList<Integer>();
				for (String str : result.split(",")) {
					listResult.add(Integer.parseInt(str.trim()));
				}
				metadata.setBibNums(listResult);
				return metadata;
			}

			for (String sep : separators) {
				if (isMultipleCitation(result, sep)) {
					metadata.setBibNums(batchCitationNumbers(result, sep));
					return metadata;
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
	private List<Integer> batchCitationNumbers(String input, String separator) {
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

	private boolean isLastCitationFor(String input, Integer bibNum) {
		input = input.substring(lastIdxParsed);
		if (input.indexOf("[" + bibNum + "]") == -1) {
			return true;
		}
		return false;
	}

	private CitationMetadata findCitationAux(String input) throws NoMoreCitationException {
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
		String bibNumsStr = filterStringSequence(input.substring(openBracketIdx + 1, openBracketIdx + closeBracketIdx),
				"-\n", "-", "\n");

		int citationStartIdx = input.substring(0, openBracketIdx + 1).lastIndexOf('.');
		if (citationStartIdx == -1) {
			citationStartIdx = 0;
		} else {
			while (input.charAt(citationStartIdx) < 'A' || input.charAt(citationStartIdx) > 'Z') {
				if (input.charAt(citationStartIdx) >= 'a' && input.charAt(citationStartIdx) <= 'z') {
					break;
				}
				citationStartIdx++;
			}
		}
		if (citationStartIdx >= openBracketIdx) {
			citationStartIdx = 0;
		}
		int citationEndIdx = openBracketIdx + closeBracketIdx
				+ input.substring(openBracketIdx + closeBracketIdx).indexOf('.');
		String citation = filterStringSequence(input.substring(citationStartIdx, citationEndIdx + 1), "-\n").replace(
				'\n', ' ');

		return new CitationMetadata(bibNumsStr, citation);
	}

	private int findReferences(String input) {
		return input.lastIndexOf("References");
	}

	public String retrieveBibtex(Integer bibNum) {
		String input = article;
		input = input.substring(findReferences(input));

		int bibIdx = input.indexOf("[" + bibNum + "]");
		int endIdx = -1;

		if (bibIdx == -1) {
			bibIdx = input.indexOf(bibNum + ".");
			endIdx = input.indexOf((bibNum + 1) + ".");
			if (endIdx == -1) {
				endIdx = input.length() - 1;
			}
		} else {
			endIdx = input.indexOf("[" + (bibNum + 1) + "]");
			if (endIdx == -1) {
				endIdx = input.length() - 1;
			}
		}

		return filterStringSequence(input.substring(bibIdx, endIdx), "-\n", "-").replace('\n', ' ');
	}

}
