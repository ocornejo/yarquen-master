package org.yarquen.web.search;

import java.util.Collections;

import org.junit.Test;
import org.yarquen.article.Article;

/**
 * {@link FacetUrlGenerator} test
 * 
 * @author Jorge Riquelme Santana
 * @date 10-04-2013
 * 
 */
public class FacetUrlGeneratorTest {
	// private FacetUrlGenerator generator = new FacetUrlGenerator("java");

	@Test
	public void test() {
		final YarquenFacet authorFacet = new YarquenFacet();
		authorFacet.setApplied(false);
		authorFacet.setCode("Peter Pan");
		authorFacet.setCount(2);
		authorFacet.setName(Article.Facets.AUTHOR.toString());
		authorFacet.setValue("Peter Pan");

		final YarquenFacet yearFacet = new YarquenFacet();
		yearFacet.setApplied(false);
		yearFacet.setCode("2013");
		yearFacet.setCount(1);
		yearFacet.setName(Article.Facets.YEAR.toString());
		yearFacet.setValue("2013");

		final YarquenFacet kw1Facet = new YarquenFacet();
		kw1Facet.setApplied(true);
		kw1Facet.setCode("groovy");
		kw1Facet.setCount(3);
		kw1Facet.setName(Article.Facets.KEYWORD.toString());
		kw1Facet.setValue("groovy");

		final YarquenFacets facets = new YarquenFacets();
		facets.setAuthor(Collections.singletonList(authorFacet));
		facets.setYear(Collections.singletonList(yearFacet));
		facets.setKeyword(Collections.singletonList(kw1Facet));

		// TODO: finish this
		// generator.generateUrls(facets);

		// Assert.assertEquals("?query=java&keyword=groovy&author=Peter+Pan",
		// authorFacet.getUrl());
		// Assert.assertEquals("?query=java&keyword=groovy&year=2013",
		// yearFacet.getUrl());
		// Assert.assertEquals("?query=java", kw1Facet.getUrl());
	}
}
