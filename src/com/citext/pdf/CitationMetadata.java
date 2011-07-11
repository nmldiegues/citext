package com.citext.pdf;

import java.util.ArrayList;
import java.util.List;

public class CitationMetadata {
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
