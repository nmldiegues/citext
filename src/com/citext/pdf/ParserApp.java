package com.citext.pdf;

/*
 * TODO parse the authors and paper name from the bibtex
 */

public class ParserApp {

	public static void main(String[] args) throws Exception {
		String str = BibUtils.readPdf("article2.pdf");
		CitationParser parser = new CitationParser(str);

		while (true) {
			try {
				CitationMetadata citationMetadata = parser.findNextCitation();
				for (Integer bibNum : citationMetadata.getReferencesUsed()) {
					System.out.println(parser.retrieveBibtex(bibNum));
				}
				System.out.println("\t" + citationMetadata.getCitation() + "\n");
			} catch (NoMoreCitationException e) {
				break;
			}
		}
	}

}
