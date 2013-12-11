package org.yarquen.web.enricher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.yarquen.account.Account;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.article.KeywordTrust;
import org.yarquen.author.Author;
import org.yarquen.author.AuthorRepository;
import org.yarquen.category.CategoryService;
import org.yarquen.keyword.Keyword;
import org.yarquen.keyword.KeywordRepository;
import org.yarquen.keyword.KeywordService;
import org.yarquen.skill.Skill;
import org.yarquen.web.article.ArticleService;
import org.yarquen.web.lucene.ArticleSearcher;

/**
 * Search form
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * 
 */
@Controller
@SessionAttributes({ EnricherController.REFERER })
@RequestMapping(value = "/articles/enricher/{id}")
public class EnricherController {
	public static final String REFERER = "referer";
	private static final String ARTICLE = "article";
	private static final String AUTHORS = "authors";
	private static final String CATEGORIES = "categories";
	private static final String KEYWORDS = "keywords";
	
	
	static final Logger LOGGER = LoggerFactory
			.getLogger(EnricherController.class);

	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private ArticleSearcher articleSearcher;
	@Resource
	private ArticleService articleService;
	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private CategoryService categoryService;
	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;
	@Resource
	private KeywordRepository keywordRepository;
	@Resource
	private KeywordService keywordService;
	@Resource
	private EnrichmentRecordRepository enrichmentRecordRepository;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Skill.class,
				new SkillPropertyEditorSupport(categoryService));
	}

	@RequestMapping(method = RequestMethod.POST, params = "cancel")
	public String returnToSearch(@ModelAttribute(REFERER) String referer,
			RedirectAttributes redirAtts) {
		if (referer != null) {
			LOGGER.trace("cancel => referer: '{}'", referer.toString());
			return "redirect:" + referer;
		} else {
			LOGGER.trace("cancel => no referer, returning to search");
			return "redirect:/articles";
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(@PathVariable String id, Model model,
			HttpServletRequest request) {
		LOGGER.trace("setup enrichment form for article id={}", id);
		final Article article = articleRepository.findOne(id);
		if (article == null) {
			throw new RuntimeException("Article " + id + " not found");
		} else {
			LOGGER.info("enriching article id={} title={}", article.getId(),
					article.getTitle());

			// FIXME: find a better way to achieve this, this mechanism may fail
			// in some browsers
			// save referer
			final String referer = request.getHeader("Referer");
			LOGGER.trace("referer: {}", referer);
			model.addAttribute(REFERER, referer);

			// article to enrich
			LOGGER.trace("articles: {}", article);
			model.addAttribute(ARTICLE, article);

			// authors
			List<String> authorsName = getAuthors();
			model.addAttribute(AUTHORS, authorsName);

			// keywords
			final List<String> keywordsName = getKeywords();
			model.addAttribute(KEYWORDS, keywordsName);

			// categories
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute(CATEGORIES, categoryTree);
		}
		return "articles/enricher";
	}

	@RequestMapping(method = RequestMethod.POST, params = "submit")
	public String update(@ModelAttribute(REFERER) String referer,
			@Valid @ModelAttribute(ARTICLE) Article article,
			BindingResult result, Model model, RedirectAttributes redirAtts) {

		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());

			// authors
			List<String> authorsName = getAuthors();
			model.addAttribute(AUTHORS, authorsName);

			// keywords
			final List<String> keywordsName = getKeywords();
			model.addAttribute(KEYWORDS, keywordsName);

			// categories
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute(CATEGORIES, categoryTree);

			return "articles/enricher";
		} else {
			LOGGER.trace("pars: article={}", article);
			final String id = article.getId();
			LOGGER.trace(
					"id:{}\n author:{}\n date:{}\n  summary:{}\n title:{}\n url:{}",
					new Object[] { id, article.getAuthor(), article.getDate(),
							article.getSummary(), article.getTitle(),
							article.getUrl() });
			if (article.getKeywords() != null) {
				LOGGER.trace("{} keywords: {}", article.getKeywords().size(),
						article.getKeywords());
			} else {
				LOGGER.trace("no keywords");
			}

			if (article.getProvidedSkills() != null) {
				LOGGER.trace("{} provided skills: {}", article
						.getProvidedSkills().size(), article
						.getProvidedSkills());
			} else {
				LOGGER.trace("no provided skills");
			}
			if (article.getRequiredSkills() != null) {
				LOGGER.trace("{} required skills: {}", article
						.getRequiredSkills().size(), article
						.getRequiredSkills());
			} else {
				LOGGER.trace("no required skills");
			}

			// get persisted article
			Article persistedArticle = articleRepository.findOne(id);
			
			if (persistedArticle == null) {
				throw new RuntimeException("Article " + id + " not found");
			} else {
				//update
				LOGGER.trace("saving new version of article id {}", id);
				EnrichmentRecord er = saveArticleDiff(persistedArticle, article);	
				
				LOGGER.trace("updating article {} hoola {} ", id,persistedArticle.getKeywordsTrust());
				persistedArticle.setAuthor(article.getAuthor());
				persistedArticle.setDate(article.getDate());		
				//persistedArticle.setKeywords(article.getKeywords());
				persistedArticle.setSummary(article.getSummary());
				persistedArticle.setTitle(article.getTitle());
				persistedArticle.setUrl(article.getUrl());
				persistedArticle.setProvidedSkills(article.getProvidedSkills());
				persistedArticle.setRequiredSkills(article.getRequiredSkills());

				Article updatedArticle = articleRepository
						.save(persistedArticle);
				
				//trust keywords enrichment				
				if(er.isChangedAddedKeywords()){
					for(String kwd: er.getAddedKeywords()){
						try{
							KeywordTrust kwt = new KeywordTrust();
							kwt.setId(er.getAccountId());
							kwt.setName(kwd);
							List<KeywordTrust> kwtPersisted = new ArrayList<KeywordTrust>(persistedArticle.getKeywordsTrust());
							
							if(!kwtPersisted.isEmpty()){
								boolean found = false;
								for(KeywordTrust temp: kwtPersisted){
									if(temp.getName().compareTo(kwt.getName())==0){
										found = true;
									}
								}
								if(!found){
									String code = articleService.addKeywordTrust(er.getArticleId(), kwt);
									LOGGER.info("KeywordTrust added with state = {}",code);
								}
							}
							else{
								String code = articleService.addKeywordTrust(er.getArticleId(), kwt);
								LOGGER.info("KeywordTrust added with state = {}",code);	
							}
							
							
						} catch (Exception e) {
							LOGGER.error("can't add KeywordTrust", e);
						}
					}
					
				}
				
				if(er.isChangedRemovedKeywordsTrust()){
					for(KeywordTrust kwd: er.getRemovedKeywordsTrust()){
						try{
							String code = articleService.removeKeywordTrust(er.getArticleId(),kwd.getName());
							LOGGER.info("KeywordTrust removed with state = {}",code);
						} catch (Exception e) {
							LOGGER.error("can't remove KeywordTrust", e);
						}
					}
				}
				
				if(er.isChangedRemovedKeywords()){
					for(String kwd: er.getRemovedKeywords()){
						try{
							String code = articleService.removeKeyword(er.getArticleId(),kwd);
							LOGGER.info("Keyword removed with state = {}",code);
						} catch (Exception e) {
							LOGGER.error("can't remove Keyword", e);
						}
					}
				}
					
				// reindex
				LOGGER.trace("reindexing article {}", id);
				
				try {
					articleSearcher.reindexArticle(updatedArticle);
					addAuthorAndKeywords(updatedArticle);
				} catch (IOException e) {
					final String msg = "something wen't wrong while reindexing Article "
							+ id + "(" + updatedArticle.getTitle() + ")";
					LOGGER.error(msg, e);
					throw new RuntimeException(msg, e);
				}

				final String message = "article \"" + article.getTitle()
						+ "\" successfully enriched";
				LOGGER.trace("adding flash paramenter: enrichmentMessage={}",
						message);
				redirAtts.addFlashAttribute("enrichmentMessage", message);


				
				if (referer != null) {
					LOGGER.trace("update => referer: '{}'", referer.toString());
					final int i = referer.indexOf('?');
					if (i != -1) {
						referer = referer.substring(i + 1);
						try {
							referer = URLDecoder.decode(referer, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							LOGGER.error("error decoding referer", e);
						}
						LOGGER.trace("params extracted: {}", referer);
					}

					return "redirect:/articles?" + referer;
				} else {
					LOGGER.trace("update => no referer, returning to search");
					return "redirect:/articles";
				}
			}
		}
	}

	/**
	 * Finds the difference between the new article and the persisted article
	 * and saves the difference and history to database
	 * 
	 * @param persistedArticle
	 *            Latest Persisted Article
	 * @param updatedArticle
	 *            Updated Article
	 */
	private EnrichmentRecord saveArticleDiff(Article persistedArticle,
			Article updatedArticle) {

		LOGGER.trace("finding differences between articles");
		final EnrichmentRecord enrichmentRecord = new EnrichmentRecord();
		enrichmentRecord.setArticleId(persistedArticle.getId());
		enrichmentRecord.setVersionDate(Calendar.getInstance(
				TimeZone.getTimeZone("UTC")).getTime());

		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		enrichmentRecord.setAccountId(userDetails.getId());

		// Finds the diff between articles and saves it in the enrichmentRecord
		boolean changed = findDiffBetweenArticles(persistedArticle,
				updatedArticle, enrichmentRecord);

		if (changed) {

			enrichmentRecordRepository.save(enrichmentRecord);
			LOGGER.trace("enrichment record saved for article id: {}",
					enrichmentRecord.getArticleId());
			
			
			
		} else {
			LOGGER.trace("enrichment record not created, there was no change");
		}
		return enrichmentRecord;

	}
	
	
	

	/**
	 * Compares every field of a persisted and updated article and keeps the
	 * difference on a enrichmentRecord
	 * 
	 * @param persistedArticle
	 *            Last persisted article
	 * @param updatedArticle
	 *            Article with changes
	 * @param enrichmentRecord
	 *            Object for keeping diff
	 * @return {@code true} if there was a diff, {@code false} if there was not
	 */
	private boolean findDiffBetweenArticles(Article persistedArticle,
			Article updatedArticle, EnrichmentRecord enrichmentRecord) {

		LOGGER.trace("finding diff between articles, for article id: {}",
				persistedArticle.getId());

		boolean changed = false;

		// Comparing Authors
		if (!StringUtils.equals(persistedArticle.getAuthor(),
				updatedArticle.getAuthor())) {
			enrichmentRecord.setNewAuthor(updatedArticle.getAuthor());
			enrichmentRecord.setOldAuthor(persistedArticle.getAuthor());
			enrichmentRecord.setChangedAuthor(true);
			changed = true;
		} else {
			enrichmentRecord.setChangedAuthor(false);
		}

		// Comparing Dates
		LOGGER.debug("Dates are: {} and {}", persistedArticle.getDate(),
				updatedArticle.getDate());
		LOGGER.debug(
				"so the difference is {}",
				StringUtils.equals(persistedArticle.getDate(),
						updatedArticle.getDate()));
		if (!StringUtils.equals(persistedArticle.getDate(),
				updatedArticle.getDate())) {
			enrichmentRecord.setNewDate(updatedArticle.getDate());
			enrichmentRecord.setOldDate(persistedArticle.getDate());
			enrichmentRecord.setChangedDate(true);
			changed = true;
		} else {
			enrichmentRecord.setChangedDate(false);
		}

		// Comparing Summary
		if (!StringUtils.equals(persistedArticle.getSummary(),
				updatedArticle.getSummary())) {
			enrichmentRecord.setNewSummary(updatedArticle.getSummary());
			enrichmentRecord.setOldSummary(persistedArticle.getSummary());
			enrichmentRecord.setChangedSummary(true);
			changed = true;
		} else {
			enrichmentRecord.setChangedSummary(false);
		}

		List<String> addedKeywordsTrust= null;
		List<String> removedKeywords= null;
		List<KeywordTrust> removedKeywordsTrust= null;
		
		Iterator<String> updatedKeywords = null;
		if(updatedArticle.getKeywords()!=null)
			updatedKeywords = updatedArticle.getKeywords().iterator();
		
		List<String> persistedKeywords = null;
		if(persistedArticle.getKeywords()!=null) 
			persistedKeywords =new ArrayList<String>(persistedArticle.getKeywords());
		
		List<KeywordTrust> persistedKeywordsTrust = null;
		if(persistedArticle.getKeywordsTrust()!=null)
			persistedKeywordsTrust= new ArrayList<KeywordTrust>(persistedArticle.getKeywordsTrust());
		
		if (updatedArticle.getKeywords() != null
		 		&& !updatedArticle.getKeywords().isEmpty()) {
			
			while(updatedKeywords.hasNext()){
				
				String upKey = updatedKeywords.next();
				if(persistedArticle.getKeywords() !=null && persistedArticle.getKeywords().contains(upKey)){
					updatedKeywords.remove();
					persistedKeywords.remove(upKey);
				}
				
				if(persistedArticle.getKeywordsTrust() !=null && contains(persistedArticle.getKeywordsTrust(),upKey)){
					updatedKeywords.remove();
					persistedKeywordsTrust.remove(returnIndex(persistedKeywordsTrust,upKey));
				}
				
			}
			
			if(updatedArticle.getKeywords().size()>0){
				LOGGER.info("agregaron kws Trust");
				addedKeywordsTrust = new ArrayList<String>(updatedArticle.getKeywords());
			}
			
			if(persistedKeywords!=null && persistedKeywords.size()>0){
				LOGGER.info("borraron kws del sistema");
				removedKeywords = new ArrayList<String>(persistedKeywords);
			}
			if(persistedKeywordsTrust!=null && persistedKeywordsTrust.size()>0){
				LOGGER.info("borraron kws Trust");
				removedKeywordsTrust = new ArrayList<KeywordTrust>(persistedKeywordsTrust);
			}
			
		}
		
		if (addedKeywordsTrust!=null && !addedKeywordsTrust.isEmpty() || 
				removedKeywords!=null && !removedKeywords.isEmpty() || 
				removedKeywordsTrust!=null && !removedKeywordsTrust.isEmpty()) {
			changed = true;
		}
		if (addedKeywordsTrust!=null && !addedKeywordsTrust.isEmpty()) {
			enrichmentRecord.setAddedKeywords(addedKeywordsTrust);
			enrichmentRecord.setChangedAddedKeywords(true);
		}
		else{
			enrichmentRecord.setChangedAddedKeywords(false);
		}
		if (removedKeywords!=null && !removedKeywords.isEmpty()) {
			enrichmentRecord.setRemovedKeywords(removedKeywords);
			enrichmentRecord.setChangedRemovedKeywords(true);
		}
		else{
			enrichmentRecord.setChangedRemovedKeywords(false);
		}
		
		if (removedKeywordsTrust!=null && !removedKeywordsTrust.isEmpty()) {
			enrichmentRecord.setRemovedKeywordsTrust(removedKeywordsTrust);
			enrichmentRecord.setChangedRemovedKeywordsTrust(true);
		}
		else{
			enrichmentRecord.setChangedRemovedKeywordsTrust(false);
		}

		// Comparing Title
		if (!persistedArticle.getTitle().equals(updatedArticle.getTitle())) {
			enrichmentRecord.setNewTitle(updatedArticle.getTitle());
			enrichmentRecord.setOldTitle(persistedArticle.getTitle());
			changed = true;
		}

		// Comparing URL
		if (!persistedArticle.getUrl().equals(updatedArticle.getUrl())) {
			enrichmentRecord.setNewUrl(updatedArticle.getUrl());
			enrichmentRecord.setOldUrl(persistedArticle.getUrl());
			changed = true;
		}

		// Comparing Provided Skills
		final List<Skill> addedProvidedSkills = new ArrayList<Skill>();
		final List<Skill> removedProvidedSkills = new ArrayList<Skill>();
		if (updatedArticle.getProvidedSkills() != null
				&& !updatedArticle.getProvidedSkills().isEmpty()) {
			for (Skill updatedProvidedSkill : updatedArticle
					.getProvidedSkills()) {
				if (persistedArticle.getProvidedSkills() != null
						&& !persistedArticle.getProvidedSkills().contains(
								updatedProvidedSkill)) {
					addedProvidedSkills.add(updatedProvidedSkill);
				}
			}
		}
		if (persistedArticle.getProvidedSkills() != null
				&& !persistedArticle.getProvidedSkills().isEmpty()) {
			for (Skill persistedProvidedSkill : persistedArticle
					.getProvidedSkills()) {
				if (updatedArticle.getProvidedSkills() != null
						&& !updatedArticle.getProvidedSkills().contains(
								persistedProvidedSkill)) {
					removedProvidedSkills.add(persistedProvidedSkill);
				}
			}
		}
		if (!addedProvidedSkills.isEmpty() || !removedProvidedSkills.isEmpty()) {
			changed = true;
		}
		if (!addedProvidedSkills.isEmpty()) {
			enrichmentRecord.setAddedProvidedSkills(addedProvidedSkills);
		}
		if (!removedProvidedSkills.isEmpty()) {
			enrichmentRecord.setRemovedProvidedSkills(removedProvidedSkills);
		}

		// Comparing Required Skills
		final List<Skill> addedRequiredSkills = new ArrayList<Skill>();
		final List<Skill> removedRequiredSkills = new ArrayList<Skill>();
		if (updatedArticle.getRequiredSkills() != null
				&& !updatedArticle.getRequiredSkills().isEmpty()) {
			for (Skill updatedRequiredSkill : updatedArticle
					.getRequiredSkills()) {
				if (persistedArticle.getRequiredSkills() != null
						&& !persistedArticle.getRequiredSkills().contains(
								updatedRequiredSkill)) {
					addedRequiredSkills.add(updatedRequiredSkill);
				}
			}
		}
		if (persistedArticle.getRequiredSkills() != null
				&& !persistedArticle.getRequiredSkills().isEmpty()) {
			for (Skill persistedRequiredSkill : persistedArticle
					.getRequiredSkills()) {
				if (updatedArticle.getRequiredSkills() != null
						&& !updatedArticle.getRequiredSkills().contains(
								persistedRequiredSkill)) {
					removedRequiredSkills.add(persistedRequiredSkill);
				}
			}
		}
		if (!addedRequiredSkills.isEmpty() || !removedRequiredSkills.isEmpty()) {
			changed = true;
		}
		if (!addedRequiredSkills.isEmpty()) {
			enrichmentRecord.setAddedRequiredSkills(addedRequiredSkills);
		}
		if (!removedRequiredSkills.isEmpty()) {
			enrichmentRecord.setRemovedRequiredSkills(removedRequiredSkills);
		}

		return changed;

	}

	private void addAuthorAndKeywords(Article article) {
		// add author if doesn't exists
		final String authorName = article.getAuthor();
		if (authorName != null) {
			final Author author = authorRepository.findByName(authorName);
			if (author == null) {
				final Author newAuthor = new Author();
				newAuthor.setName(authorName);
				LOGGER.trace("adding author {}", authorName);
				authorRepository.save(newAuthor);
			}
		}

		// add inexistent keywords
		final List<KeywordTrust> keywords = article.getKeywordsTrust();
		for (KeywordTrust kw : keywords) {
			final Keyword keywordFound = keywordRepository.findByName(kw.getName());
			if (keywordFound == null) {
				final Keyword keyword = new Keyword();
				keyword.setName(kw.getName());
				LOGGER.trace("adding keyword {}", kw);
				keywordRepository.save(keyword);
			}
		}
	

	}

	private List<String> getAuthors() {
		final List<String> authorsName = new LinkedList<String>();
		final Iterable<Author> authors = authorRepository.findAll();
		for (Author author : authors) {
			authorsName.add(author.getName());
		}
		return authorsName;
	}

	private List<String> getKeywords() {
		final List<String> keywordsName = new LinkedList<String>();
		final Iterable<Keyword> keywords = keywordRepository.findAll();
		for (Keyword keyword : keywords) {
			keywordsName.add(keyword.getName());
		}
		return keywordsName;
	}
	
	public static boolean contains(List<KeywordTrust> list, String name) {
	    for (KeywordTrust object : list) {
	        if (object.getName().compareTo(name) ==0) {
	            return true;
	        }
	    }
	    return false;
	}
	public static int returnIndex(List<KeywordTrust> list, String name) {
	    int i = 0;
		for (KeywordTrust object : list) {
	        if (object.getName().compareTo(name) ==0) {
	            return i;
	        }
	        i++;
	    }
	    return -1;
	}
}
