package org.yarquen.web.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
				final YarquenFacets facetsCount = new YarquenFacets();
				final List<SearchResult> results = articleSearcher.search(
						searchFields, facetsCount);

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
				}

				return "articles/search";
			} catch (IOException e) {
				throw new RuntimeException("ahg!", e);
			} catch (ParseException e) {
				throw new RuntimeException("ua!", e);
			}
		}
	}
	
	@RequestMapping(value="/articles/rate/{id}",method = RequestMethod.GET)
	public String addNewRating(@PathVariable String id, 
			@RequestParam("rating") String rating, Model model, SearchFields sf){
		
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

			} catch (Exception e) {
				LOGGER.error("can't add rating", e);

			}
		}

		model.addAttribute("searchFields",sf);
		return "articles/search";
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
