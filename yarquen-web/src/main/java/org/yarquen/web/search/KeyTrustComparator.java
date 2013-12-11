package org.yarquen.web.search;

import java.util.Comparator;

import org.yarquen.article.KeywordTrust;


public class KeyTrustComparator implements Comparator<KeywordTrust> {

	@Override
	public int compare(KeywordTrust o1, KeywordTrust o2) {
		// TODO Auto-generated method stub
		 if (o1.getTrust() > o2.getTrust()) return -1;
	     if (o1.getTrust() < o2.getTrust()) return 1;
	     return 0;
		
	}
	
}
