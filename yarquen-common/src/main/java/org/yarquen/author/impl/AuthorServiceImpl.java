package org.yarquen.author.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.author.Author;
import org.yarquen.author.AuthorRepository;
import org.yarquen.author.AuthorService;

/**
 * {@link AuthorService} implementation
 * 
 * @author Jorge Riquelme Santana
 * @date 16-04-2013
 * 
 */
@Service
public class AuthorServiceImpl implements AuthorService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AuthorServiceImpl.class);

	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AuthorRepository authorRepository;

	@Override
	public void updateAuthors() {
		LOGGER.info("updating authors...");
		int c = 0;
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final Author authorFound = authorRepository.findByName(article
					.getAuthor());
			if (authorFound == null) {
				final Author newAuthor = new Author();
				newAuthor.setName(article.getAuthor());
				LOGGER.debug("adding new author: {}", newAuthor.getName());
				authorRepository.save(newAuthor);
				c++;
			}
		}
		LOGGER.info("{} new authors added", c);
	}
}
