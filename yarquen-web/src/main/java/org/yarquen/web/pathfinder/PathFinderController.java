package org.yarquen.web.pathfinder;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.category.CategoryBranch;
import org.yarquen.skill.Skill;
import org.yarquen.web.account.AccountController;
import org.yarquen.web.enricher.CategoryTreeBuilder;

/**
 * Path finder controller
 * 
 * @author maliq
 * @date 27/03/2013
 * 
 */

@Controller
@RequestMapping("/pathfinder")
public class PathFinderController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountController.class);

	@Resource
	private PathFinderService pathFinderService;

	@Resource
	private ArticleRepository articleRepository;

	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;

	@RequestMapping(value = "/find", method = RequestMethod.GET)
	public String find(@RequestParam("branchCode") String branch,
			@RequestParam("branchName") String name,
			@RequestParam("skillLevel") Integer skillLevel, Model model) {
		final CategoryBranch categoryBranch = new CategoryBranch();
		final StringTokenizer tokenizer = new StringTokenizer(branch, ".");
		while (tokenizer.hasMoreTokens()) {
			final String code = tokenizer.nextToken();
			categoryBranch.addSubCategory(code, null);
		}
		Skill skill = new Skill();
		skill.setCategoryBranch(categoryBranch);
		skill.setLevel(skillLevel);
		LOGGER.debug("finding paths for: {}", skill.getCode());
		List<Path> pathList = pathFinderService.find("", skill);
		if (pathList.isEmpty()) {
			model.addAttribute("error", "Not Found Path for " + name);
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute("categories", categoryTree);
			return "articles/setupPathFinder";
		}
		model.addAttribute("pathList", pathList);
		return "articles/showPaths";

	}

	@RequestMapping(value = "/setup", method = RequestMethod.GET)
	public String find(Model model) {
		final List<Map<String, Object>> categoryTree = categoryTreeBuilder
				.buildTree();
		model.addAttribute("categories", categoryTree);
		return "articles/setupPathFinder";

	}

	@RequestMapping(value = "/article", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Article getArticle(@RequestParam("articleId") String articleId) {
		Article article = articleRepository.findOne(articleId);
		LOGGER.debug("getting {} article", article);
		return article;

	}

}
