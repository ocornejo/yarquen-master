package org.yarquen.web.pathfinder.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.category.CategoryBranch;
import org.yarquen.skill.Skill;
import org.yarquen.web.pathfinder.Path;

public class PathFinderServiceImplTest {
	private PathFinderServiceImpl pathFinderServiceImpl;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PathFinderServiceImplTest.class);
	Map<String, Skill> skills;
	Map<String, Article> articles;

	@Before
	public void setUp() throws Exception {
		skills = new HashMap<String, Skill>();
		articles = new HashMap<String, Article>();
		for (int i = 1; i < 8; i++) {
			Skill skill = new Skill();
			CategoryBranch categoryBranch = new CategoryBranch();
			categoryBranch.addSubCategory(Integer.toString(i), null);
			skill.setCategoryBranch(categoryBranch);
			skills.put(Integer.toString(i), skill);

			skill = new Skill();
			categoryBranch = new CategoryBranch();
			categoryBranch.addSubCategory(Integer.toString(i), null);
			skill.setCategoryBranch(categoryBranch);
			skill.setLevel(2);
			skills.put(skill.getCode(), skill);
		}

		Article article;

		article = new Article();
		article.setId("D");
		article.getRequiredSkills().add(skills.get("1"));
		article.getRequiredSkills().add(skills.get("2"));
		article.getProvidedSkills().add(skills.get("4"));
		article.getProvidedSkills().add(skills.get("5"));
		article.getProvidedSkills().add(skills.get("2.2"));
		articles.put("D", article);

		article = new Article();
		article.setId("B");
		article.getRequiredSkills().add(skills.get("3"));
		article.getRequiredSkills().add(skills.get("4"));
		article.getProvidedSkills().add(skills.get("6"));
		articles.put("B", article);

		article = new Article();
		article.setId("C");
		article.getRequiredSkills().add(skills.get("4"));
		article.getRequiredSkills().add(skills.get("5"));
		article.getProvidedSkills().add(skills.get("6"));
		articles.put("C", article);

		article = new Article();
		article.setId("A");
		article.getRequiredSkills().add(skills.get("6"));
		article.getProvidedSkills().add(skills.get("7"));
		articles.put("A", article);
		pathFinderServiceImpl = new PathFinderServiceImpl();
		ArticleRepositoryFake articleRepositoryFake = new ArticleRepositoryFake(
				articles, skills);
		pathFinderServiceImpl.setArticleRepository(articleRepositoryFake);
		pathFinderServiceImpl.init();
	}

	@Test
	public void test5() {
		Skill skill = new Skill();
		CategoryBranch branch = new CategoryBranch();
		branch.addSubCategory("5", "");
		skill.setCategoryBranch(branch);
		List<Path> returned = pathFinderServiceImpl.find("", skill);
		LOGGER.debug("paths found: {}", returned);
		Assert.assertFalse(returned.isEmpty());
		Assert.assertEquals(1, returned.size());
		Assert.assertEquals("D", returned.get(0).toString());

	}

	@Test
	public void test6() {
		CategoryBranch branch = new CategoryBranch();
		branch.addSubCategory("6", "");
		Skill skill = new Skill();
		skill.setCategoryBranch(branch);
		List<Path> returned = pathFinderServiceImpl.find("", skill);
		LOGGER.debug("paths found: {}", returned);
		Assert.assertFalse(returned.isEmpty());
		Assert.assertEquals(2, returned.size());
		Assert.assertEquals("D-B", returned.get(0).toString());
		Assert.assertEquals("D-C", returned.get(1).toString());
	}

	@Test
	public void test6loop() {

		ArticleRepositoryFake articleRepositoryFake = (ArticleRepositoryFake) pathFinderServiceImpl
				.getArticleRepository();
		Article article = articleRepositoryFake.getArticles().get("C");
		article.getProvidedSkills().add(skills.get("2"));
		// pathFinderServiceImpl.setArticleRepository(articleRepositoryFake);
		pathFinderServiceImpl.init();
		CategoryBranch branch = new CategoryBranch();
		branch.addSubCategory("6", "");
		Skill skill = new Skill();
		skill.setCategoryBranch(branch);
		List<Path> returned = pathFinderServiceImpl.find("", skill);
		LOGGER.debug("paths found: {}", returned);
		Assert.assertFalse(returned.isEmpty());
		Assert.assertEquals(2, returned.size());
		Assert.assertEquals("C-D-B", returned.get(0).toString());
		Assert.assertEquals("C-D-C", returned.get(1).toString());
		Assert.assertTrue(returned.get(1).getHasDuplicate());
	}

	@Test
	public void test7() {
		CategoryBranch branch = new CategoryBranch();
		branch.addSubCategory("7", "");
		Skill skill = new Skill();
		skill.setCategoryBranch(branch);
		List<Path> returned = pathFinderServiceImpl.find("", skill);
		LOGGER.debug("paths found: {}", returned);
		Assert.assertFalse(returned.isEmpty());
		Assert.assertEquals(2, returned.size());
		Assert.assertEquals("D-B-A", returned.get(0).toString());
		Assert.assertEquals("D-C-A", returned.get(1).toString());
	}

	class ArticleRepositoryFake implements ArticleRepository {
		Map<String, Article> articles;

		public Map<String, Article> getArticles() {
			return articles;
		}

		Map<String, Skill> skills;

		public ArticleRepositoryFake(Map<String, Article> articles,
				Map<String, Skill> skills) {
			this.articles = articles;
			this.skills = skills;
		}

		@Override
		public List<Article> findAllWithProvidedSkills() {
			return new ArrayList<Article>(articles.values());
		}

		@Override
		public <S extends Article> S save(S entity) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Article> Iterable<S> save(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Article findOne(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean exists(String id) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Iterable<Article> findAll() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<Article> findAll(Iterable<String> ids) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long count() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void delete(String id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void delete(Article entity) {
			// TODO Auto-generated method stub

		}

		@Override
		public void delete(Iterable<? extends Article> entities) {
			// TODO Auto-generated method stub

		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub

		}

		@Override
		public Article findByTitle(String title) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Article findByUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
