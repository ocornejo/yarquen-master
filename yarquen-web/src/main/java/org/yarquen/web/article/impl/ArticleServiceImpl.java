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
import org.yarquen.article.Rating;
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
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			LOGGER.error("An error ocurred while trying to insert new rating");
			e.printStackTrace();
			return "Error";
		}
		
	}

}
