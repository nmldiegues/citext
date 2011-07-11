package com.citext.pdf;

public class CitationQuote {
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
