package com.citext.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitationMetadata {

	// "1"
	private String bibNumsStr;

	// 1
	private List<Integer> bibNums;

	// In [1] the authors claim that this is good.
	private String citation;

	// 1 -> "E. Author, B. Author and C. Author. In Proceedings"
	private Map<Integer, String> references;

	public CitationMetadata(String bibNumsStr, String citation) {
		this.bibNumsStr = bibNumsStr;
		this.citation = citation;
		this.bibNums = new ArrayList<Integer>();
		this.references = new HashMap<Integer, String>();
	}

	public CitationMetadata(List<Integer> bibNums, String citation, Map<Integer, String> references) {
		this.bibNums = bibNums;
		this.citation = citation;
		this.references = references;
	}

	public String getBibNumsStr() {
		return bibNumsStr;
	}

	public void setBibNumsStr(String bibNumsStr) {
		this.bibNumsStr = bibNumsStr;
	}

	public List<Integer> getBibNums() {
		return bibNums;
	}

	public void setBibNums(List<Integer> bibNums) {
		this.bibNums = bibNums;
	}

	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

	public Map<Integer, String> getReferences() {
		return references;
	}

	public void setReferences(Map<Integer, String> references) {
		this.references = references;
	}

	public void putReference(Integer bibNum, String reference) {
		this.references.put(bibNum, reference);
	}

}
