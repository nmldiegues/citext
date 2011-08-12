package com.citext.pdf;

import java.util.Map;

public class ParserApp {

	public static void main(String[] args) throws Exception {
		String str = BibUtils.readPdf("article.pdf");
		CitationParser parser = new CitationParser(str);

		for (CitationMetadata metadata : parser.fetchAllCitations()) {
			System.out.println("-------------------------");
			for (Map.Entry<Integer, String> referenceEntry : metadata.getReferences().entrySet()) {
				System.out.println("[" + referenceEntry.getKey() + "] " + referenceEntry.getValue());
			}
			System.out.println("\t" + metadata.getCitation());
		}
	}

}
