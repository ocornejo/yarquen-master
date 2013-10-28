package org.yarquen.web.lucene;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {@link BatchIndexBuilder} itest
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * 
 */
@IfProfileValue(name = "test-groups", value = "itests")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/indexBuilder-context.xml" })
public class BatchIndexBuilderTest {
	@Resource
	private BatchIndexBuilder indexBuilder;

	@Test
	public void test() throws IOException {
		// FIXME: this is not a test :)
		indexBuilder.createIndex();
	}
}
