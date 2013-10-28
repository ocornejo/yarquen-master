package org.yarquen.web.search.facet.tree;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryBranchNode;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 18-03-2013
 * 
 */
public class FacetTreeNode {
	protected String completeCode;
	private List<FacetTreeNode> children;
	private String code;
	private double count;
	private String name;
	private boolean selected;
	private String url;

	public FacetTreeNode() {
		children = new ArrayList<>();
	}

	public void activeBranch(CategoryBranch categoryBranch) {
		checkNotNull(categoryBranch);

		final List<CategoryBranchNode> branchNodes = categoryBranch.getNodes();

		final CategoryBranchNode firstNode = branchNodes.get(0);
		final FacetTreeNode child = getChild(firstNode.getCode());
		checkState(child != null);
		child.setSelected(true);
		// active the branch's tail recursively
		if (branchNodes.size() > 1) {
			child.activeBranch(categoryBranch.tail());
		}
	}

	public void addBranch(CategoryBranch categoryBranch, double count) {
		checkNotNull(categoryBranch);

		final List<CategoryBranchNode> branchNodes = categoryBranch.getNodes();

		final CategoryBranchNode firstNode = branchNodes.get(0);
		FacetTreeNode child = getChild(firstNode.getCode());
		if (child == null) {
			// if the head node doesn't exists as a child, create it
			child = new FacetTreeNode();
			child.setCode(firstNode.getCode());
			child.setName(firstNode.getName());
			children.add(child);
		}

		// add the branch's tail recursively
		if (branchNodes.size() > 1) {
			child.addBranch(categoryBranch.tail(), count);
		} else {
			// TODO
			child.setCount(count);
		}
	}

	public List<FacetTreeNode> getChildren() {
		return children;
	}

	public String getCode() {
		return code;
	}

	public String getCompleteCode() {
		return completeCode;
	}

	public double getCount() {
		return count;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setChildren(List<FacetTreeNode> children) {
		this.children = checkNotNull(children);
	}

	public void setCode(String code) {
		this.code = checkNotNull(code);
	}

	public void setCount(double count) {
		this.count = count;
	}

	public void setName(String name) {
		this.name = checkNotNull(name);
	}

	public void setSelected(boolean selected) {
		this.selected = checkNotNull(selected);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected void genereteCompleteCodes(String parentCompleteCode) {
		checkNotNull(parentCompleteCode);
		checkNotNull(code);
		completeCode = parentCompleteCode + "." + code;
		if (children != null) {
			for (FacetTreeNode node : children) {
				node.genereteCompleteCodes(completeCode);
			}
		}
	}

	private FacetTreeNode getChild(String code) {
		checkNotNull(code);
		for (FacetTreeNode node : children) {
			if (node.getCode().equals(code)) {
				return node;
			}
		}
		return null;
	}
}
