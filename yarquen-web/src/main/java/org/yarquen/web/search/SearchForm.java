package org.yarquen.web.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.article.Rating;
import org.yarquen.category.CategoryService;
import org.yarquen.skill.Skill;
import org.yarquen.web.article.ArticleService;
import org.yarquen.web.enricher.SkillPropertyEditorSupport;
import org.yarquen.web.lucene.ArticleSearcher;
import org.yarquen.web.lucene.CustomComparator;

/**
 * Search form
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * 
 */
@Controller
public class SearchForm {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchForm.class);
	
	@Resource
	private ArticleSearcher articleSearcher;
	@Resource
	private CategoryService categoryService;
	@Resource
	private ArticleService articleService;
	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AccountService accountService;
	
	private static final int MAX_RECOMMENDATIONS = 5;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Skill.class,
				new SkillPropertyEditorSupport(categoryService));
	}

	@RequestMapping(value = "/articles", method = RequestMethod.GET)
	public String processSubmit(SearchFields searchFields,
			BindingResult result, Model model, HttpServletRequest request) {
		final String query = searchFields.getQuery();
		LOGGER.debug("searching query: {}", query);
		if (query == null || query.trim().equals("")) {
			return "articles/search";
		} else {
			try {
				Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
				boolean loggedIn = true; 
				for (GrantedAuthority it : authorities){ 
					if (it.toString().compareTo("ROLE_ANONYMOUS")==0) 
						loggedIn = false;
				}

				final YarquenFacets facetsCount = new YarquenFacets();
				
				final List<SearchResult> results;	
				
				//con trust
				if(loggedIn){
					results = articleSearcher.searchWT(
						searchFields, facetsCount,loggedIn);
				}
				else{
					results = articleSearcher.search(
							searchFields, facetsCount);
				}			
				// send back applied facets
				addAppliedFacetsToModel(searchFields, facetsCount, model);

				model.addAttribute("facets", facetsCount);

				// calc facet's url
				final FacetUrlGenerator urlGenerator = new FacetUrlGenerator(
						query);
				urlGenerator.generateUrls(facetsCount);

				// result
				if (!results.isEmpty()) {
					model.addAttribute("results", results);
					List<SearchResult> bestResults = new ArrayList<SearchResult>(results);
					
					Collections.sort(bestResults, new CustomComparator());
					int size = MAX_RECOMMENDATIONS;
					
					if(results.size()<MAX_RECOMMENDATIONS)
						size = results.size();
					
					bestResults = bestResults.subList(0, size);
					
					Iterator<SearchResult> i = bestResults.iterator();
					while(i.hasNext()){
						SearchResult sr = i.next();
						if(sr.getRatingFinal()==0)
							i.remove();
					}
					if(!bestResults.isEmpty())
						model.addAttribute("bestResults",bestResults);
				}

				return "articles/search";
			} catch (IOException e) {
				throw new RuntimeException("ahg!", e);
			} catch (ParseException e) {
				throw new RuntimeException("ua!", e);
			}
		}
	}

	
	@RequestMapping(value="/articles/rate",method = RequestMethod.GET)
	public void addNewRating(
			@RequestParam("id") String id, 
			@RequestParam("rating") String rating,
			HttpServletResponse rsp) throws IOException{
		
		LOGGER.trace("setup new rating for article id={}", id);
		
		//get user information
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		
		Account account = accountService.findOne(userDetails.getId());
		String idAccount = account.getId();
		
	    LOGGER.trace("the user id={} is rating the article id={}", idAccount,id);
	     
		final Article article = articleRepository.findOne(id);
		if (article == null) {
			throw new RuntimeException("Article " + id + " not found");
		} else {
			LOGGER.debug("rating article id={} title={}", article.getId(),
					article.getTitle());
			try {
				Rating rate = new Rating();
				rate.setId(idAccount);
				rate.setRating(rating);
				String code = articleService.addRating(id,rate);
				LOGGER.info("Rating added with state = {}",code);
				rsp.setStatus(HttpServletResponse.SC_OK);
				rsp.getWriter().print(code);

			} catch (Exception e) {
				LOGGER.error("can't add rating", e);
				rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				rsp.getWriter().print("Error");

			}
		}
	}

	@RequestMapping(value = "/articles/search", method = RequestMethod.GET)
	public String setupForm(Model model) {
		model.addAttribute("searchFields", new SearchFields());
		return "articles/search";
	}

	private void addAppliedFacetsToModel(SearchFields searchFields,
			YarquenFacets facetsCount, Model model) {
		// if the search fields contains an author...
		if (searchFields.getAuthor() != null) {
			// I look for it into the result facets, to mark it as applied
			for (YarquenFacet authorFacet : facetsCount.getAuthor()) {
				if (authorFacet.getCode().equals(searchFields.getAuthor())) {
					authorFacet.setApplied(true);
					model.addAttribute("authorFacet", authorFacet);
					break;
				}
			}
		}

		// idem for year
		if (searchFields.getYear() != null) {
			for (YarquenFacet yearFacet : facetsCount.getYear()) {
				if (yearFacet.getCode().equals(searchFields.getYear())) {
					yearFacet.setApplied(true);
					model.addAttribute("yearFacet", yearFacet);
					break;
				}
			}
		}

		// idem for keywords
		if (searchFields.getKeyword() != null) {
			// in this case, though, I have to collect eventual multiple facets
			final List<YarquenFacet> appliedKw = new ArrayList<YarquenFacet>();
			for (YarquenFacet kwFacet : facetsCount.getKeyword()) {
				// if the facet is part of the query, mark it as applied and add
				// it to appliedKw list
				if (searchFields.getKeyword().contains(kwFacet.getCode())) {
					kwFacet.setApplied(true);
					appliedKw.add(kwFacet);
				}
			}
			model.addAttribute("keywordFacets", appliedKw);
		}
	}
}
