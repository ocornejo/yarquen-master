package org.yarquen.web.pathfinder.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.skill.Skill;
import org.yarquen.web.pathfinder.Path;
import org.yarquen.web.pathfinder.PathFinderService;

import com.google.common.collect.Lists;

@Service
public class PathFinderServiceImpl implements PathFinderService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PathFinderServiceImpl.class);

	@Resource
	private ArticleRepository articleRepository;

	public ArticleRepository getArticleRepository() {
		return articleRepository;
	}

	public void setArticleRepository(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	// global cache
	private Map<String, Set<String>> providerListCache;
	private Map<String, Article> articleCache;
	private StopWatch stopWatch;

	// temporal cache
	private Map<String, List<Path>> pathListCache;

	@PostConstruct
	@Scheduled(cron = "0 * * * * *")
	public void init() {
		stopWatch = new StopWatch();
		LOGGER.debug("Init cache!");
		stopWatch.start("Build Cache");
		List<Article> articles = articleRepository.findAllWithProvidedSkills();
		LOGGER.debug("articles with ProvidedSkills {}, {}", articles.size(),
				articles);
		providerListCache = new HashMap<String, Set<String>>();
		articleCache = new HashMap<String, Article>();
		for (Article article : articles) {
			articleCache.put(article.getId(), article);
			for (Skill skill : article.getProvidedSkills()) {
				String code = skill.getCode();
				if (providerListCache.containsKey(code)) {
					providerListCache.get(code).add(article.getId());
				} else {
					Set<String> articleList = new HashSet<String>();
					articleList.add(article.getId());
					providerListCache.put(code, articleList);
				}
			}
		}
		stopWatch.stop();
		LOGGER.debug(stopWatch.prettyPrint());

	}

	@Override
	public List<Path> find(String accounId, Skill skill) {
		stopWatch = new StopWatch();
		stopWatch.start("Find Path");
		LOGGER.debug("finding path for {} ", skill.getCode());
		pathListCache = new HashMap<String, List<Path>>();
		List<Path> pathList = new ArrayList<Path>();
		String code = skill.getCode();

		if (providerListCache.containsKey(code)) {
			LOGGER.debug("has articles the skill {}", code);
			for (String articleId : providerListCache.get(code)) {
				Article article = articleCache.get(articleId);
				Path path;
				boolean hasNarrow = false;
				for (Skill requiredSkill : article.getRequiredSkills()) {
					List<Path> narrows = new ArrayList<Path>();
					if (requiredSkill.getCode() == skill.getCode()) {
						LOGGER.warn("article has the same skill required as well as provided");
					} else {
						narrows = findPath(requiredSkill,
								Lists.newArrayList(articleId));
					}
					if (!narrows.isEmpty()) {
						hasNarrow = true;
						for (Path p : narrows) {
							p.getArticles().add(article);
							// add just unknown path
							if (!pathList.contains(p)) {
								LOGGER.debug(
										"adding article to path {}, by skill {}",
										p.toString(), requiredSkill.getCode());
								pathList.add(p);
							}
						}
					}
				}
				if (!hasNarrow) {
					// no more narrows
					LOGGER.debug("creating path with article {} [no naw]",
							article.getId());
					path = new Path();
					path.getArticles().add(article);
					pathList.add(path);
				}
			}
		}
		stopWatch.stop();
		LOGGER.debug(stopWatch.prettyPrint());
		return pathList;
	}

	private List<Path> findPath(Skill skill, List<String> parents) {
		List<Path> pathList = new ArrayList<Path>();
		String code = skill.getCode();
		if (pathListCache.containsKey(code)) {
			return pathListCache.get(code);
		}

		if (providerListCache.containsKey(code)) {
			for (String articleId : providerListCache.get(code)) {
				if (parents.contains(articleId)) {
					LOGGER.warn("Loop found for {} for skill {}", articleId,
							skill.getCode());
					continue;
				}
				Article article = articleCache.get(articleId);
				if (pathListCache.containsKey(article.getId())) {
					return pathListCache.get(article.getId());
				}
				Path path;
				if (!parents.contains(article.getId())) {
					boolean hasNarrow = false;
					for (Skill requiredSkill : article.getRequiredSkills()) {
						List<Path> narrows = new ArrayList<Path>();
						if (requiredSkill.getCode() == skill.getCode()) {
							LOGGER.warn("article has the same skill required as well as provided");
						} else {
							parents.add(articleId);
							narrows = findPath(requiredSkill, parents);
							parents.remove(articleId);
						}
						if (!narrows.isEmpty()) {
							hasNarrow = true;
							for (Path p : narrows) {
								p.getArticles().add(article);
								// add just unknown path
								if (!pathList.contains(p)) {
									LOGGER.debug(
											"adding  article to path {}, by skill {}",
											p.toString(),
											requiredSkill.getCode());
									pathList.add(p);
								}
							}
						}
					}
					if (!hasNarrow) {
						// no more narrows
						LOGGER.debug("creating path with article {} [no naw]",
								article.getId());
						path = new Path();
						path.getArticles().add(article);
						pathList.add(path);
					}
				}
				pathListCache.put(article.getId(), clonePath(pathList));
			}
		}
		pathListCache.put(code, clonePath(pathList));
		return pathList;

	}

	private List<Path> clonePath(List<Path> pathList) {
		List<Path> clonedPathList = new ArrayList<Path>();
		for (Path path : pathList) {
			Path clonedPath = new Path();
			clonedPath.getArticles().addAll(path.getArticles());
			clonedPathList.add(clonedPath);
		}
		return clonedPathList;
	}
}
