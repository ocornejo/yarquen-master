package org.yarquen.web.record;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.yarquen.account.Account;
import org.yarquen.account.AccountRepository;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.skill.Skill;
import org.yarquen.trust.Trust;
import org.yarquen.web.enricher.EnricherController;
import org.yarquen.web.enricher.EnrichmentRecord;
import org.yarquen.web.enricher.EnrichmentRecordRepository;

/**
 * 
 * @author Choon-ho Yoon
 * @date Mar 20, 2013
 * 
 */
@Controller
@SessionAttributes({ EnricherController.REFERER })
@RequestMapping("/articles/record")
public class RecordController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RecordController.class);

	@Resource
	private EnrichmentRecordRepository enrichmentRecordRepository;
	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AccountRepository accountRepository;

	private String lastSearchReferer;

	@RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
	public String initRecordView(@PathVariable("articleId") String articleId,
			Model model, HttpServletRequest request) {
		LOGGER.trace("looking for versions of article");
		final Article article = articleRepository.findOne(articleId);
		if (article != null) {
			final List<EnrichmentRecord> articleHistory = enrichmentRecordRepository
					.findByArticleId(articleId, new Sort(new Sort.Order(
							Sort.Direction.DESC, "versionDate")));
			final Map<EnrichmentRecord, Account> historyWrapper = new LinkedHashMap<EnrichmentRecord, Account>();

			for (EnrichmentRecord enrichmentRecord : articleHistory) {
				final Account account = accountRepository
						.findOne(enrichmentRecord.getAccountId());
				historyWrapper.put(enrichmentRecord, account);
			}
			if (articleHistory != null && !articleHistory.isEmpty()) {
				model.addAttribute("articleRecords", historyWrapper);
			}

			LOGGER.debug("Article ID: [{}] has {} enrichment records.",
					articleId, articleHistory.size());
			model.addAttribute("articleId", articleId);
			// FIXME this is bad, very bad
			if (lastSearchReferer == null
					|| lastSearchReferer.isEmpty()
					|| !request.getHeader("Referer")
							.contains("articles/record")) {
				lastSearchReferer = request.getHeader("Referer");
			}
			LOGGER.debug("last referer: [{}]", lastSearchReferer);
			model.addAttribute("referer", lastSearchReferer);

			return "articles/recordHistory";
		} else {
			throw new RuntimeException("Article ID " + articleId
					+ " does not exist.");
		}
	}

	@RequestMapping(value = "/articleView/{articleId}", method = RequestMethod.GET)
	public String originalArticleView(@PathVariable String articleId,
			Model model) {
		final Article article = articleRepository.findOne(articleId);
		if (article == null) {
			throw new RuntimeException(
					"I came from an article and now it doesn't exist?");
		}
		model.addAttribute("article", article);
		model.addAttribute("diffMode", false);
		return "articles/record";
	}

	@RequestMapping(value = "/{articleId}/{date}/{mode}", method = RequestMethod.GET)
	public String recordView(@PathVariable String articleId,
			@PathVariable("date") long dateTime,
			@PathVariable("mode") boolean diffMode, Model model,
			HttpServletRequest request) {
		final Date recordDate = new Date(dateTime);
		final List<EnrichmentRecord> futureRecords = enrichmentRecordRepository
				.findByArticleIdAndVersionDateBetween(articleId, recordDate,
						Calendar.getInstance(TimeZone.getTimeZone("UTC"))
								.getTime(), new Sort(new Sort.Order(
								Sort.Direction.ASC, "versionDate")));
		final EnrichmentRecord actualRecord = enrichmentRecordRepository
				.findByVersionDateAndArticleId(recordDate, articleId);
		LOGGER.debug("Records until [{}] of article id [{}] is [{}] records",
				new Object[] { recordDate, articleId, futureRecords.size() });
		// For visibility mode, when true shows the difference, when false shows
		// the plain record
		model.addAttribute("diffMode", diffMode);

		if (actualRecord != null) {
			model.addAttribute("actualRecord", actualRecord);
			model.addAttribute("actualAccount",
					accountRepository.findOne(actualRecord.getAccountId()));
			
			/* Trust operations */
			Trust trustAction = new Trust();
			Node user = trustAction.getNode(accountRepository.findOne(actualRecord.getAccountId()).getId());
			
			Account userDetails = (Account) SecurityContextHolder.getContext()
					.getAuthentication().getDetails();
			
			Node me = trustAction.getNode(userDetails.getId());
			
			if(trustAction.checkAdjacency(me,user)){
				model.addAttribute("trustValue",trustAction.getAdjacencyTrust(me,user));
				model.addAttribute("knows",true);
			}
			else{
				model.addAttribute("trustValue","5");
			}
		}

		Article article = articleRepository.findOne(articleId);
		if (article == null) {
			throw new RuntimeException(
					"no article, something fishy is going on here");
		}
		if (futureRecords != null && futureRecords.size() != 0) {
			article = rebuildArticle(article, futureRecords);
			model.addAttribute("nextRecord", futureRecords.get(0));
			final String accountId = futureRecords.get(0).getAccountId();
			final Account account = accountRepository.findOne(accountId);
			if (account == null) {
				throw new RuntimeException("no account for id " + accountId);
			}
			model.addAttribute("accountNext", account);
		}
		// if in diffmode, then we take the keywords, provided skills and
		// required skills for showing porpuse
		if (diffMode) {

			if (actualRecord.getAddedKeywords() != null
					&& !actualRecord.getAddedKeywords().isEmpty()) {
				for (String addedKeyword : actualRecord.getAddedKeywords()) {
					article.getKeywords().remove(addedKeyword);
				}
			}
			if (actualRecord.getAddedProvidedSkills() != null
					&& !actualRecord.getAddedProvidedSkills().isEmpty()) {
				for (Skill addedProvidedSkill : actualRecord
						.getAddedProvidedSkills()) {
					article.getProvidedSkills().remove(addedProvidedSkill);
				}
			}
			if (actualRecord.getAddedRequiredSkills() != null
					&& !actualRecord.getAddedRequiredSkills().isEmpty()) {
				for (Skill addedRequiredSkill : actualRecord
						.getAddedRequiredSkills()) {
					article.getRequiredSkills().remove(addedRequiredSkill);
				}
			}
		}

		model.addAttribute("article", article);

		final List<EnrichmentRecord> pastRecords = enrichmentRecordRepository
				.findByArticleIdAndVersionDateBefore(articleId, recordDate,
						new Sort(new Sort.Order(Sort.Direction.ASC,
								"versionDate")));
		if (pastRecords != null && !pastRecords.isEmpty()) {
			model.addAttribute("prevRecord",
					pastRecords.get(pastRecords.size() - 1));
			final String accountId = pastRecords.get(pastRecords.size() - 1)
					.getAccountId();
			final Account account = accountRepository.findOne(accountId);
			if (account == null) {
				throw new RuntimeException("no account for id " + accountId);
			}
			model.addAttribute("accountPrev", account);
		}

		return "articles/record";

	}

	private Article rebuildArticle(Article article,
			List<EnrichmentRecord> records) {
		LOGGER.debug("building article to point: {}", records.size());
		for (EnrichmentRecord enrichmentRecord : records) {

			if (enrichmentRecord.getOldTitle() != null) {
				article.setTitle(enrichmentRecord.getOldTitle());
			}
			if (enrichmentRecord.isChangedAuthor()) {
				article.setAuthor(enrichmentRecord.getOldAuthor());
			}
			if (enrichmentRecord.isChangedDate()) {
				article.setDate(enrichmentRecord.getOldDate());
			}
			if (enrichmentRecord.isChangedSummary()) {
				article.setSummary(enrichmentRecord.getOldSummary());
			}
			if (enrichmentRecord.getOldUrl() != null) {
				article.setUrl(enrichmentRecord.getOldUrl());
			}

			if (enrichmentRecord.getAddedKeywords() != null
					&& !enrichmentRecord.getAddedKeywords().isEmpty()) {
				for (String addedKeyword : enrichmentRecord.getAddedKeywords()) {
					LOGGER.debug("Removing Keyword: {}", addedKeyword);
					article.getKeywords().remove(addedKeyword);
				}
			}

			if (enrichmentRecord.getRemovedKeywords() != null
					&& !enrichmentRecord.getRemovedKeywords().isEmpty()) {
				for (String removedKeywords : enrichmentRecord
						.getRemovedKeywords()) {
					article.getKeywords().add(removedKeywords);
				}
			}

			if (enrichmentRecord.getAddedProvidedSkills() != null
					&& !enrichmentRecord.getAddedProvidedSkills().isEmpty()) {
				for (Skill addedProvidedSkill : enrichmentRecord
						.getAddedProvidedSkills()) {
					article.getProvidedSkills().remove(addedProvidedSkill);
				}
			}

			if (enrichmentRecord.getRemovedProvidedSkills() != null
					&& !enrichmentRecord.getRemovedProvidedSkills().isEmpty()) {
				for (Skill removedProvidedSkill : enrichmentRecord
						.getRemovedProvidedSkills()) {
					article.getProvidedSkills().add(removedProvidedSkill);
				}
			}

			if (enrichmentRecord.getAddedRequiredSkills() != null
					&& !enrichmentRecord.getAddedRequiredSkills().isEmpty()) {
				for (Skill addedRequiredSkill : enrichmentRecord
						.getAddedRequiredSkills()) {
					article.getRequiredSkills().remove(addedRequiredSkill);
				}
			}

			if (enrichmentRecord.getRemovedRequiredSkills() != null
					&& !enrichmentRecord.getRemovedRequiredSkills().isEmpty()) {
				for (Skill removedRequiredSkill : enrichmentRecord
						.getRemovedRequiredSkills()) {
					article.getRequiredSkills().add(removedRequiredSkill);
				}
			}
		}
		return article;
	}
}
