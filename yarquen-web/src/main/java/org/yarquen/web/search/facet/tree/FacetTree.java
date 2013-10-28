package org.yarquen.web.search.facet.tree;

import org.yarquen.article.Article;

import com.google.common.base.Strings;

/**
 * Skill facet tree
 * 
 * @author Jorge Riquelme Santana
 * @date 18-03-2013
 * 
 */
public class FacetTree extends FacetTreeNode {
	private boolean required;

	public void genereteCompleteCodes() {
		completeCode = required ? Article.Facets.REQUIRED_SKILL.toString()
				: Article.Facets.PROVIDED_SKILL.toString();
		if (getChildren() != null) {
			for (FacetTreeNode node : getChildren()) {
				node.genereteCompleteCodes(completeCode);
			}
		}
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String toPrettyString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(required ? "REQUIRED\n" : "PROVIDED\n");
		for (FacetTreeNode node : getChildren()) {
			toPrettyString(node, builder, 1);
		}
		return builder.toString();
	}

	private void toPrettyString(FacetTreeNode node, StringBuilder builder,
			int indentation) {
		builder.append(Strings.repeat(" ", indentation));
		builder.append("-");
		builder.append(node.getCode());
		builder.append("\n");
		for (FacetTreeNode child : node.getChildren()) {
			toPrettyString(child, builder, indentation + 1);
		}
	}
}
