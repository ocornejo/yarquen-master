package org.yarquen.web.search;

import java.util.List;

import javax.annotation.Resource;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;
import org.yarquen.article.Rating;
import org.yarquen.trust.Trust;


/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * 
 */

public class SearchResult {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchResult.class);
	
	@Resource
	private AccountService accountService;
	
	private String author;
	private String date;
	private String id;
	private List<String> keywords;
	private float score;
	private String summary;
	private String title;
	private String url;
    private double ratingFinal;
	
	private List<Rating> ratings;

	public List<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}

	public double getRatingFinal() {
		return this.ratingFinal;
	}

	public void setRatingFinalDirect(double rate){
		this.ratingFinal = rate;
	}
	
	public void setRatingFinal(List<Rating> ratings, String id, Trust trustAction, Node source) {
		
		double rate = 0;
		
		if(ratings.size()>0){
			for(Rating rt: ratings){
				if(source.getProperty("accountID").toString().compareTo(rt.getId())==0){
					this.ratingFinal = Double.parseDouble(rt.getRating());
					return;
				}
				Node sink = trustAction.getNode(rt.getId());
				rate = Double.parseDouble(rt.getRating()) * trustAction.getTrust(source, sink);
				this.ratingFinal+=rate;
			}
			this.ratingFinal = (double)(ratingFinal/10);
		}
		else
			this.ratingFinal= 0;
	}


	public String getAuthor() {
		return author;
	}

	public String getDate() {
		return date;
	}

	public String getId() {
		return id;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public float getScore() {
		return score;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
