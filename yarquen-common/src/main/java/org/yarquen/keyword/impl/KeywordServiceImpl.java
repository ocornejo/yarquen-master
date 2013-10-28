package org.yarquen.keyword.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.keyword.Keyword;
import org.yarquen.keyword.KeywordRepository;
import org.yarquen.keyword.KeywordService;

/**
 * {@link KeywordService} implementation.
 * 
 * @author Jorge Riquelme Santana
 * @date 16-04-2013
 * 
 */
@Service
public class KeywordServiceImpl implements KeywordService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(KeywordServiceImpl.class);

	@Resource
	private KeywordRepository keywordRepository;
	@Resource
	private ArticleRepository articleRepository;

	@Override
	public void updateKeywords() {
		LOGGER.info("updating keywords...");
		int c = 0;
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final List<String> keywords = article.getKeywords();
			for (String kw : keywords) {
				final Keyword kwFound = keywordRepository.findByName(kw);
				if (kwFound == null) {
					final Keyword newKeyword = new Keyword();
					newKeyword.setName(kw);
					LOGGER.debug("adding new keyword: {}", newKeyword.getName());
					keywordRepository.save(newKeyword);
					c++;
				}
			}
		}
		LOGGER.info("{} new keywords added", c);
	}
}
