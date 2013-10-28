package org.yarquen.web.search;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.article.Article;
import org.yarquen.web.search.facet.tree.FacetTree;
import org.yarquen.web.search.facet.tree.FacetTreeNode;

import com.google.common.collect.ObjectArrays;

/**
 * Facet url generator
 * 
 * @author Jorge Riquelme Santana
 * @date 04-04-2013
 * 
 */
public class FacetUrlGenerator {
	private static Logger LOGGER = LoggerFactory
			.getLogger(FacetUrlGenerator.class);

	private static final String BASE_URL = "http://asdf.cl/jkl";
	private String query;

	public FacetUrlGenerator(String query) {
		this.query = query;
	}

	private List<YarquenFacet> gatherAppliedFacets(YarquenFacets facets) {
		final List<YarquenFacet> appliedFacets = new ArrayList<>();

		// author
		for (YarquenFacet authorFacet : facets.getAuthor()) {
			if (authorFacet.isApplied()) {
				appliedFacets.add(authorFacet);
			}
		}
		// year
		for (YarquenFacet yearFacet : facets.getYear()) {
			if (yearFacet.isApplied()) {
				appliedFacets.add(yearFacet);
			}
		}
		// keywords
		for (YarquenFacet kwFacet : facets.getKeyword()) {
			if (kwFacet.isApplied()) {
				appliedFacets.add(kwFacet);
			}
		}

		return appliedFacets;
	}

	public void generateUrls(YarquenFacets facets) {
		// applied author, year and keywords
		final List<YarquenFacet> appliedFacets = gatherAppliedFacets(facets);
		// all applied facets
		final List<YarquenFacet> allAppliedFacets = new ArrayList<>();
		allAppliedFacets.addAll(appliedFacets);

		// applied provided skills
		final List<YarquenFacet> appliedProvidedSkills = gatherAppliedFacetsFromTree(facets
				.getProvidedSkillTree());
		for (YarquenFacet yarquenFacet : appliedProvidedSkills) {
			yarquenFacet.setName(Article.Facets.PROVIDED_SKILL.toString());
		}
		allAppliedFacets.addAll(appliedProvidedSkills);

		// applied required skills
		final List<YarquenFacet> appliedRequiredSkills = gatherAppliedFacetsFromTree(facets
				.getRequiredSkillTree());
		for (YarquenFacet yarquenFacet : appliedRequiredSkills) {
			yarquenFacet.setName(Article.Facets.REQUIRED_SKILL.toString());
		}
		allAppliedFacets.addAll(appliedRequiredSkills);

		if (LOGGER.isTraceEnabled()) {
			final StringBuilder msgBuilder = new StringBuilder();
			for (YarquenFacet facet : allAppliedFacets) {
				msgBuilder.append(String.format("[%s=%s] ", facet.getName(),
						facet.getCode()));
			}
			LOGGER.trace("facets: {}", msgBuilder.toString());
		}

		final String currentUrl = generateUrl(allAppliedFacets);
		LOGGER.trace("currentUrl: {}", currentUrl);
		facets.setCurrentUrl(currentUrl);

		// author
		for (YarquenFacet authorFacet : facets.getAuthor()) {
			final String generatedUrl = generateUrl(allAppliedFacets,
					authorFacet);
			authorFacet.setUrl(generatedUrl);
		}
		// year
		for (YarquenFacet yearFacet : facets.getYear()) {
			final String generatedUrl = generateUrl(allAppliedFacets, yearFacet);
			yearFacet.setUrl(generatedUrl);
		}
		// keywords
		for (YarquenFacet kwFacet : facets.getKeyword()) {
			final String generatedUrl = generateUrl(allAppliedFacets, kwFacet);
			kwFacet.setUrl(generatedUrl);
		}
		// provided skills
		if (facets.getProvidedSkillTree().getChildren() != null) {
			for (FacetTreeNode node : facets.getProvidedSkillTree()
					.getChildren()) {
				generateUrlForTreeNodes(node, true, appliedFacets,
						appliedProvidedSkills, appliedRequiredSkills);
			}
		}
		// required skills
		if (facets.getRequiredSkillTree().getChildren() != null) {
			for (FacetTreeNode node : facets.getRequiredSkillTree()
					.getChildren()) {
				generateUrlForTreeNodes(node, false, appliedFacets,
						appliedProvidedSkills, appliedRequiredSkills);
			}
		}
	}

	private String codeWithoutHead(String code) {
		return code.substring(code.indexOf('.') + 1);
	}

	private void generateUrlForTreeNodes(FacetTreeNode node, boolean provided,
			List<YarquenFacet> appliedFacets,
			List<YarquenFacet> appliedProvidedSkills,
			List<YarquenFacet> appliedRequiredSkills) {
		URIBuilder uriBuilder = null;
		try {
			uriBuilder = new URIBuilder(BASE_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException("error in the matrix", e);
		}

		// add the query string...
		uriBuilder.addParameter("query", query);

		// add each applied facet(author, year and keyword facets), unless it's
		// the current facet
		for (YarquenFacet f : appliedFacets) {
			uriBuilder.addParameter(f.getName(), f.getCode());
		}

		// required/provided code removed
		final String nodeCode = codeWithoutHead(node.getCompleteCode());

		// add provided & required skill facets
		if (provided) {
			// all required skill facets
			for (YarquenFacet f : appliedRequiredSkills) {
				uriBuilder.addParameter(f.getName(), f.getCode());
			}

			// TODO: finish this!
			// here we have to do some magic
			// for (YarquenFacet f : appliedProvidedSkills) {
			// if (!f.getCode().startsWith(nodeCode)) {
			// uriBuilder.addParameter(f.getName(), f.getCode());
			// }
			// }
		} else {
			// all provided skill facets
			for (YarquenFacet f : appliedProvidedSkills) {
				uriBuilder.addParameter(f.getName(), f.getCode());
			}

			// ta daah!
		}

		// finally, add the current facet only if it isn't applied
		if (!node.isSelected()) {
			final String name = provided ? Article.Facets.PROVIDED_SKILL
					.toString() : Article.Facets.REQUIRED_SKILL.toString();
			uriBuilder.addParameter(name, nodeCode);
		}

		try {
			final String generatedUri = uriBuilder.build().toString()
					.substring(BASE_URL.length());
			LOGGER.trace("generated uri: {}", generatedUri);
			node.setUrl(generatedUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException("invalid uri, douh!", e);
		}

		// now, generate url for children
		if (node.getChildren() != null) {
			for (FacetTreeNode child : node.getChildren()) {
				generateUrlForTreeNodes(child, provided, appliedFacets,
						appliedProvidedSkills, appliedRequiredSkills);
			}
		}
	}

	private String generateUrl(List<YarquenFacet> allFacets) {
		URIBuilder uriBuilder = null;
		try {
			uriBuilder = new URIBuilder(BASE_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException("error in the matrix", e);
		}

		// add the query string...
		uriBuilder.addParameter("query", query);

		// add each applied facet, unless it's the current facet
		for (YarquenFacet f : allFacets) {
			uriBuilder.addParameter(f.getName(), f.getCode());
		}

		try {
			final String generatedUri = uriBuilder.build().toString()
					.substring(BASE_URL.length());
			LOGGER.trace("generated uri: {}", generatedUri);
			return generatedUri;
		} catch (URISyntaxException e) {
			throw new RuntimeException("invalid uri, douh!", e);
		}
	}

	private String generateUrl(List<YarquenFacet> allFacets, YarquenFacet facet) {
		URIBuilder uriBuilder = null;
		try {
			uriBuilder = new URIBuilder(BASE_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException("error in the matrix", e);
		}

		// add the query string...
		uriBuilder.addParameter("query", query);

		// add each applied facet, unless it's the current facet
		for (YarquenFacet f : allFacets) {
			if (f != facet) {
				uriBuilder.addParameter(f.getName(), f.getCode());
			}
		}

		// finally, add the current facet only if it isn't applied
		if (!facet.isApplied()) {
			uriBuilder.addParameter(facet.getName(), facet.getCode());
		}

		try {
			final String generatedUri = uriBuilder.build().toString()
					.substring(BASE_URL.length());
			LOGGER.trace("generated uri: {}", generatedUri);
			return generatedUri;
		} catch (URISyntaxException e) {
			throw new RuntimeException("invalid uri, douh!", e);
		}
	}

	private List<YarquenFacet> gatherAppliedFacetsFromTree(FacetTree facetTree) {
		final List<YarquenFacet> appliedFacets = new ArrayList<>();
		if (facetTree.getChildren() != null) {
			for (FacetTreeNode node : facetTree.getChildren()) {
				if (node.isSelected()) {
					gatherAppliedFacetsFromTree(new FacetTreeNode[] { node },
							appliedFacets);
				}
			}
		} else {
			LOGGER.debug("tree {} has no children",
					facetTree.isRequired() ? "REQUIRED" : "PROVIDED");
		}
		return appliedFacets;
	}

	private void gatherAppliedFacetsFromTree(FacetTreeNode[] branch,
			List<YarquenFacet> appliedFacets) {
		final FacetTreeNode lastNode = branch[branch.length - 1];
		boolean addAsFacet = false;
		if (lastNode.getChildren() != null) {
			for (FacetTreeNode node : lastNode.getChildren()) {
				if (node.isSelected()) {
					// keep going until reach a non-selected node
					final FacetTreeNode[] newBranch = ObjectArrays.concat(
							branch, node);
					gatherAppliedFacetsFromTree(newBranch, appliedFacets);
				} else {
					addAsFacet = true;
				}
			}
		} else {
			addAsFacet = true;
		}

		if (addAsFacet) {
			final YarquenFacet yarquenFacet = new YarquenFacet();
			yarquenFacet.setApplied(true);
			yarquenFacet.setValue(branch[branch.length - 1].getName());
			// warning!, I don't set the name here, that has to be done in a
			// superior level where it's known if the facet is required or
			// provided

			// code generation
			final StringBuilder codeBuilder = new StringBuilder();
			for (FacetTreeNode node : branch) {
				codeBuilder.append(node.getCode());
				codeBuilder.append('.');
			}
			codeBuilder.deleteCharAt(codeBuilder.length() - 1);
			yarquenFacet.setCode(codeBuilder.toString());

			appliedFacets.add(yarquenFacet);
		}
	}
}
