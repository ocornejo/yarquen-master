package org.yarquen.category;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link CategoryBranch} unit tests
 * 
 * @author Jorge Riquelme Santana
 * @date 28/01/2013
 * 
 */
public class CategoryBranchTest {
	private static final String CATEGORY = "category";
	private static final String ECLIPSE_CODE = "ECLIPSE";
	private static final String ECLIPSE_NAME = "Eclipse";
	private static final String SOFTWARE_CODE = "SW";
	private static final String SOFTWARE_NAME = "Software";
	private CategoryBranch branch;

	@Before
	public void setup() {
		branch = new CategoryBranch();
		branch.addSubCategory(SOFTWARE_CODE, SOFTWARE_NAME).addSubCategory(
				ECLIPSE_CODE, ECLIPSE_NAME);
	}

	@Test
	public void testGetCodeAsArrayDontPrependCategory() {
		Assert.assertArrayEquals(new String[] { SOFTWARE_CODE, ECLIPSE_CODE },
				branch.getCodeAsArray());
	}

	@Test
	public void testGetCodeAsArrayPrependCategory() {
		Assert.assertArrayEquals(new String[] { CATEGORY, SOFTWARE_CODE,
				ECLIPSE_CODE }, branch.getCodeAsArray(CATEGORY));
	}

	@Test
	public void testGetCodeDontPrependCategory() {
		Assert.assertEquals(SOFTWARE_CODE + CategoryBranch.CODE_SEPARATOR
				+ ECLIPSE_CODE, branch.getCode());
	}

	@Test
	public void testGetCodePrependCategory() {
		Assert.assertEquals(CATEGORY + CategoryBranch.CODE_SEPARATOR
				+ SOFTWARE_CODE + CategoryBranch.CODE_SEPARATOR + ECLIPSE_CODE,
				branch.getCode(CATEGORY));
	}

	@Test
	public void testIncompleteFromCode() {
		final CategoryBranch incompleteBranch = CategoryBranch
				.incompleteFromCode(SOFTWARE_CODE
						+ CategoryBranch.CODE_SEPARATOR + ECLIPSE_CODE);
		Assert.assertEquals(SOFTWARE_CODE, incompleteBranch.getNodes().get(0)
				.getCode());
		Assert.assertNull(incompleteBranch.getNodes().get(0).getName());
		Assert.assertEquals(ECLIPSE_CODE, incompleteBranch.getNodes().get(1)
				.getCode());
		Assert.assertNull(incompleteBranch.getNodes().get(1).getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCode() {
		branch.addSubCategory("as" + CategoryBranch.CODE_SEPARATOR + "df",
				"asdf");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidName() {
		branch.addSubCategory("asdf", "as" + CategoryBranch.NAME_SEPARATOR
				+ "df");
	}

	public void testGetTail() {
		final CategoryBranch tail = branch.tail();
		Assert.assertEquals(1, tail.getNodes().size());
		final CategoryBranchNode node = tail.getNodes().get(0);
		Assert.assertEquals(ECLIPSE_CODE, node.getCode());
		Assert.assertEquals(ECLIPSE_NAME, node.getName());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetTailFail() {
		branch = new CategoryBranch();
		branch.addSubCategory(SOFTWARE_CODE, SOFTWARE_NAME);
		branch.tail();
	}
}
