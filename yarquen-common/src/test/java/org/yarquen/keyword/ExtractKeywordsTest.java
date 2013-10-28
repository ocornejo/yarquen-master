package org.yarquen.keyword;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Fake test to extract keywords from articles
 * 
 * FIXME: remove this
 * 
 * @author Jorge Riquelme Santana
 * @date 20/01/2013
 * 
 */
@IfProfileValue(name = "test-groups", values = { "itests" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/keyword-context.xml",
		"/article-context.xml" })
public class ExtractKeywordsTest {
	@Resource
	private KeywordService keywordService;

	@Test
	public void test() {
		// FIXME: this isn't a test
		keywordService.updateKeywords();
	}
}
