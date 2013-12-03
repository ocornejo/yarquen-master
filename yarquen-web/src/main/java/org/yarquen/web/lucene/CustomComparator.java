package org.yarquen.web.lucene;

import java.util.Comparator;

import org.yarquen.web.search.SearchResult;

public class CustomComparator implements Comparator<SearchResult> {

	@Override
	public int compare(SearchResult o1, SearchResult o2) {
		// TODO Auto-generated method stub
        if (o1.getRatingFinal() > o2.getRatingFinal()) return -1;
        if (o1.getRatingFinal() < o2.getRatingFinal()) return 1;
        return 0;
	}

}
