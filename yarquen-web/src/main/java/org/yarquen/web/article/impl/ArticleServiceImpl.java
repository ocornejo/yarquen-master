package org.yarquen.web.article.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.article.KeywordTrust;
import org.yarquen.article.Rating;
import org.yarquen.skill.Skill;
import org.yarquen.web.article.ArticleService;


@Service
public class ArticleServiceImpl implements ArticleService{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleServiceImpl.class);
	
	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AccountService accountService;

	@Override
	public String addRating(String id,Rating rating) {
		Article article = articleRepository.findOne(id);

		try {
			List<Rating> ratings = new ArrayList<Rating>(article.getRatings());
			//user information
			Account userDetails = (Account) SecurityContextHolder.getContext()
					.getAuthentication().getDetails();
			
			Account account = accountService.findOne(userDetails.getId());
			String idAccount = account.getId();
			
			int index = -1;

			for (int i = 0; i < ratings.size(); i++)
			    if (ratings.get(i).getId().compareTo(idAccount)==0) {
			        index = i;
			        break;
			    }
			if(index!=-1)
				ratings.set(index, rating);
			else
				ratings.add(rating);
			
			article.setRatings(ratings);
			LOGGER.info("{}: {}  added successfull", rating.getId(), rating.getRating());
			articleRepository.save(article);
			return "Ok";
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to insert new rating");
			return "Error";
		}
		
	}

	@Override
	public String addKeywordTrust(String id, KeywordTrust kwt) {
		Article article = articleRepository.findOne(id);

		try {
			List<KeywordTrust> kwts = new ArrayList<KeywordTrust>(article.getKeywordsTrust());
			kwts.add(kwt);
			article.setKeywordsTrust(kwts);
			
			LOGGER.info("{}: {}  added successfull", kwt.getId(), kwt.getName());
			articleRepository.save(article);
			return "Ok";
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to insert new keywordTrust");
			return "Error";
		}
	}

	@Override
	public String removeKeywordTrust(String id, String keyword) {
		Article article = articleRepository.findOne(id);
		
		try {
			List<KeywordTrust> kwts = new ArrayList<KeywordTrust>(article.getKeywordsTrust());
			int index = -1;
			for (int i = 0; i < kwts.size(); i++)
			    if (kwts.get(i).getName().compareTo(keyword)==0) {
			        index = i;
			        break;
			    }
			if(index != -1){
				kwts.remove(index);
				article.setKeywordsTrust(kwts);
				
				LOGGER.info("The keywordTrust {} was removed successfully", keyword);
				articleRepository.save(article);
				return "Ok";
			}
			else{
				LOGGER.error("The KeywordTrust not exist in the Database");
				return "Error";
			}
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to remove the keywordTrust");
			return "Error";
		}
		
	}

	@Override
	public String removeKeyword(String id, String keyword) {
		Article article = articleRepository.findOne(id);
		try {
			List<String> kw = new ArrayList<String>(article.getKeywords());
			kw.remove(keyword);
			article.setKeywords(kw);
			LOGGER.info("The keyword {} was removed successfully", keyword);
			articleRepository.save(article);
			return "Ok";
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to remove the keyword");
			return "Error";
		}
	}

	@Override
	public String addProvidedSkill(String id, Skill skill) {
		Article article = articleRepository.findOne(id);

		try {
			List<Skill> skills = new ArrayList<Skill>(article.getProvidedSkills());
			skills.add(skill);
			article.setProvidedSkills(skills);
			
			LOGGER.info("{} provided skill added successfull", skill.getAsText());
			articleRepository.save(article);
			return "Ok";
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to insert new provided skill");
			return "Error";
		}
	}

	@Override
	public String removeProvidedSkill(String id, Skill skill) {
		// TODO Auto-generated method stub
		Article article = articleRepository.findOne(id);
		
		try {
			List<Skill> skills = new ArrayList<Skill>(article.getProvidedSkills());
			int index = -1;
			for (int i = 0; i < skills.size(); i++)
			    if (skills.get(i).getAsText().compareTo(skill.getAsText())==0) {
			        index = i;
			        break;
			    }
			if(index != -1){
				skills.remove(index);
				article.setProvidedSkills(skills);
				
				LOGGER.info("The provided skill {} was removed successfully", skill.getAsText());
				articleRepository.save(article);
				return "Ok";
			}
			else{
				LOGGER.error("The provided skill not exist in the Database");
				return "Error";
			}
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to remove the provided skill");
			return "Error";
		}
	}

	@Override
	public String addRequiredSkill(String id, Skill skill) {
		Article article = articleRepository.findOne(id);
		try {
			List<Skill> skills = new ArrayList<Skill>(article.getRequiredSkills());
			skills.add(skill);
			article.setRequiredSkills(skills);
			
			LOGGER.info("{} required skill added successfull", skill.getAsText());
			articleRepository.save(article);
			return "Ok";
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to insert new required skill");
			return "Error";
		}
	}

	@Override
	public String removeRequiredSkill(String id, Skill skill) {
		Article article = articleRepository.findOne(id);
		
		try {
			List<Skill> skills = new ArrayList<Skill>(article.getRequiredSkills());
			int index = -1;
			for (int i = 0; i < skills.size(); i++)
			    if (skills.get(i).getAsText().compareTo(skill.getAsText())==0) {
			        index = i;
			        break;
			    }
			if(index != -1){
				skills.remove(index);
				article.setRequiredSkills(skills);
				
				LOGGER.info("The required skill {} was removed successfully", skill.getAsText());
				articleRepository.save(article);
				return "Ok";
			}
			else{
				LOGGER.error("The required skill not exist in the Database");
				return "Error";
			}
		} catch (Exception e) {
			LOGGER.error("An error ocurred while trying to remove the required skill");
			return "Error";
		}
	}

}
