package org.yarquen.web.search.facet.tree;

import org.junit.Test;
import org.yarquen.category.CategoryBranch;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 25-03-2013
 * 
 */
public class FaceTreeTest {
	@Test
	public void test() {
		final FacetTree tree = new FacetTree();

		final CategoryBranch b1 = new CategoryBranch();
		b1.addSubCategory("L1", "l1");
		b1.addSubCategory("L2", "l2");
		tree.addBranch(b1, 0d);
	}
}
