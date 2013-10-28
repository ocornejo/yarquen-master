package org.yarquen.category;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.validation.Valid;

import com.google.common.collect.Lists;

/**
 * Category branch (path from root to leaf)
 * 
 * @author Jorge Riquelme Santana
 * @date 21/01/2013
 * 
 */
public class CategoryBranch {
	public static final String CODE_SEPARATOR = ".";
	public static final String NAME_SEPARATOR = "/";

	public static CategoryBranch incompleteFromCode(String code) {
		// escape code separator in regex
		final String[] components = code.split("\\" + CODE_SEPARATOR);
		final CategoryBranch branch = new CategoryBranch();
		for (String component : components) {
			branch.addSubCategory(component, null);
		}
		return branch;
	}

	public static CategoryBranch parse(String branch) {
		final CategoryBranch categoryBranch = new CategoryBranch();
		final StringTokenizer tokenizer = new StringTokenizer(branch,
				CODE_SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			final String code = tokenizer.nextToken();
			categoryBranch.addSubCategory(code, null);
		}
		return categoryBranch;
	}

	@Valid
	private List<CategoryBranchNode> nodes = new ArrayList<CategoryBranchNode>();

	public CategoryBranch() {
	}

	private CategoryBranch(List<CategoryBranchNode> nodes) {
		this.nodes = checkNotNull(nodes);
	}

	public CategoryBranch addSubCategory(String code, String name) {
		nodes.add(new CategoryBranchNode(code, name));
		return this;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}

		if (anObject instanceof CategoryBranch) {
			final CategoryBranch categoryBranch = (CategoryBranch) anObject;
			if (this.getCode().equals(categoryBranch.getCode())) {
				return true;
			}
		}
		return false;

	}

	public String getCode() {
		final StringBuilder sb = new StringBuilder();
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getCode());
			sb.append(CODE_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getCode(String categoryCode) {
		final StringBuilder sb = new StringBuilder();
		sb.append(categoryCode);
		sb.append(CODE_SEPARATOR);
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getCode());
			sb.append(CODE_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String[] getCodeAsArray() {
		final String[] codeArray = new String[nodes.size()];
		int i = 0;
		for (CategoryBranchNode node : nodes) {
			codeArray[i++] = node.getCode();
		}
		return codeArray;
	}

	public String[] getCodeAsArray(String categoryCode) {
		final String[] codeArray = new String[nodes.size() + 1];
		int i = 0;
		codeArray[i++] = categoryCode;
		for (CategoryBranchNode node : nodes) {
			codeArray[i++] = node.getCode();
		}
		return codeArray;
	}

	public String getName() {
		final StringBuilder sb = new StringBuilder();
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getName());
			sb.append(NAME_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public List<CategoryBranchNode> getNodes() {
		return nodes;
	}

	public CategoryBranch tail() {
		checkState(nodes.size() > 1);

		final Iterator<CategoryBranchNode> iterator = nodes.iterator();
		iterator.next();
		final List<CategoryBranchNode> tailNodes = Lists.newArrayList(iterator);

		return new CategoryBranch(tailNodes);
	}

	public void setNodes(List<CategoryBranchNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		return getCode();
	}
}
