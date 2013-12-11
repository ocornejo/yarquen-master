package org.yarquen.web.article;

import org.yarquen.article.KeywordTrust;
import org.yarquen.article.Rating;

public interface ArticleService {
	
	String addRating(String id, Rating rating);
	String addKeywordTrust(String id, KeywordTrust kwt);
	String removeKeywordTrust(String id, String keyword);
	String removeKeyword(String id, String keyword);

}
