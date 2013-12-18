package org.yarquen.web.article;

import org.yarquen.article.KeywordTrust;
import org.yarquen.article.Rating;
import org.yarquen.skill.Skill;

public interface ArticleService {
	
	String addRating(String id, Rating rating);
	String addKeywordTrust(String id, KeywordTrust kwt);
	String removeKeywordTrust(String id, String keyword);
	String removeKeyword(String id, String keyword);
	String addProvidedSkill(String id, Skill skill);
	String removeProvidedSkill(String id, Skill skill);
	String addRequiredSkill(String id, Skill skill);
	String removeRequiredSkill(String id, Skill skill);


}
