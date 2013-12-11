package org.yarquen.web.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.neo4j.graphdb.Node;
import org.yarquen.account.AccountService;
import org.yarquen.article.KeywordTrust;
import org.yarquen.article.Rating;
import org.yarquen.trust.Trust;


/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * 
 */

public class SearchResult {
	
	@Resource
	private AccountService accountService;
	
	private String author;
	private String date;
	private String id;
	private List<String> keywords;
	private List<KeywordTrust> keywordsTrust;
	private float score;
	private String summary;
	private String title;
	private String url;
    private double ratingFinal;
	
	private List<Rating> ratings;

	private double trustScore;
	
	
	public List<KeywordTrust> getKeywordsTrust() {
		return keywordsTrust;
	}

	public void setKeywordsTrust(List<KeywordTrust> keywordsTrust, Trust trustAction, Node source) {
		
		this.keywordsTrust = new ArrayList<KeywordTrust>();
		
		if(keywordsTrust.size()>0){
			for(KeywordTrust kw : keywordsTrust){
				Node sink = trustAction.getNode(kw.getId());
				kw.setTrust(trustAction.getTrust(source, sink));
				kw.setColor();
				this.keywordsTrust.add(kw);
			}
		}
		if(!this.keywordsTrust.isEmpty()){
			Collections.sort(this.keywordsTrust, new KeyTrustComparator());
		}
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}

	public double getRatingFinal() {
		return this.ratingFinal;
	}
	
	public void setRatingFinal(List<Rating> ratings, Trust trustAction, Node source) {
		
		double rate = 0;
		double trust = 0;
		this.ratingFinal = 0;
		if(ratings.size()>0){
			for(Rating rt: ratings){
				Node sink = trustAction.getNode(rt.getId());
				double trustTemp = trustAction.getTrust(source, sink);
				rate = Double.parseDouble(rt.getRating()) * trustTemp;
				this.ratingFinal+=rate;
				trust += trustTemp;
			}
			if(trust>0){			
				double value = (ratingFinal/trust);
				this.ratingFinal = (double)Math.round( value* 10)/10;
				this.trustScore = trust;
			}
		}
	}
	
	public void setTrustScore(double trustScore){
		this.trustScore = trustScore;
	}
	
	public double getTrustScore() {
		return trustScore;
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

	public void setRatingFinalDirect(int i) {
		this.ratingFinal=i;
	}
	
}
