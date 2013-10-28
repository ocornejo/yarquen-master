package org.yarquen.web.search;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.yarquen.web.search.facet.tree.FacetTree;

/**
 * Facets
 * 
 * @author Jorge Riquelme Santana
 * @date 11/01/2013
 * 
 */
public class YarquenFacets {
	private List<YarquenFacet> author;
	private String currentUrl;
	private List<YarquenFacet> keyword;
	private FacetTree providedSkillTree;
	private FacetTree requiredSkillTree;
	private List<YarquenFacet> year;

	public List<YarquenFacet> getAuthor() {
		return author;
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public List<YarquenFacet> getKeyword() {
		return keyword;
	}

	public String getNewAuthorApplied(String newAuthor) {

		for (YarquenFacet facetAuthor : author) {
			if (facetAuthor.getCode().equals(newAuthor)) {
				return facetAuthor.getUrl();
			}
		}

		return appendParameterToUrl(currentUrl, "author", newAuthor);
	}

	public String getNewDateApplied(String newDate) {
		final String newYear = newDate.split("/")[2];

		for (YarquenFacet facetDate : year) {
			if (facetDate.getCode().equals(newYear)) {
				return facetDate.getUrl();
			}
		}
		return appendParameterToUrl(currentUrl, "year", newDate);
	}

	public String getNewKeywordApplied(String newKeyword) {

		for (YarquenFacet facetKeyword : keyword) {
			if (facetKeyword.getCode().equals(newKeyword)) {
				return facetKeyword.getUrl();
			}
		}

		return appendParameterToUrl(currentUrl, "keyword", newKeyword);
	}

	public FacetTree getProvidedSkillTree() {
		return providedSkillTree;
	}

	public FacetTree getRequiredSkillTree() {
		return requiredSkillTree;
	}

	public List<YarquenFacet> getYear() {
		return year;
	}

	public boolean isAuthorApplied(String newAuthor) {
		for (YarquenFacet facetAuthor : author) {
			if (facetAuthor.getCode().equals(newAuthor)) {
				return facetAuthor.isApplied() ? true : false;
			}
		}
		return false;
	}

	public boolean isKeywordApplied(String newKeyword) {
		for (YarquenFacet facetKeyword : keyword) {
			if (facetKeyword.getCode().equals(newKeyword)) {
				return facetKeyword.isApplied() ? true : false;
			}
		}
		return false;
	}

	public boolean isYearApplied(String newDate) {
		final String newYear = newDate.split("/")[2];
		for (YarquenFacet facetDate : year) {
			if (facetDate.getCode().equals(newYear)) {
				return facetDate.isApplied() ? true : false;
			}
		}
		return false;
	}

	public void setAuthor(List<YarquenFacet> author) {
		this.author = author;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	public void setKeyword(List<YarquenFacet> keyword) {
		this.keyword = keyword;
	}

	public void setProvidedSkillTree(FacetTree providedSkillTree) {
		this.providedSkillTree = providedSkillTree;
	}

	public void setRequiredSkillTree(FacetTree requiredSkillTree) {
		this.requiredSkillTree = requiredSkillTree;
	}

	public void setYear(List<YarquenFacet> year) {
		this.year = year;
	}

	private String appendParameterToUrl(String url, String parameter,
			String value) {
		final String baseUrl = "http://localhost/";
		URIBuilder uriBuilder = null;
		try {
			uriBuilder = new URIBuilder(baseUrl + url);
		} catch (URISyntaxException e) {
			throw new RuntimeException("error in the matrix", e);
		}

		uriBuilder.addParameter(parameter, value);

		try {
			final String generatedUri = uriBuilder.build().toString()
					.substring(baseUrl.length());
			return generatedUri;
		} catch (URISyntaxException e) {
			throw new RuntimeException("invalid uri x_X", e);
		}
	}
}
