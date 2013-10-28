package org.yarquen.web.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.yarquen.article.Article;

/**
 * @author maliq
 * @date 20/03/2013
 * 
 */
public class Path {
	private int length = 0;
	private List<Article> articles;

	public boolean getHasDuplicate() {
		Set<Article> set = new HashSet<Article>(articles);

		if (set.size() < articles.size()) {
			return true;
		}
		return false;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public List<Article> getArticles() {
		if (articles == null) {
			articles = new ArrayList<Article>();
		}
		return articles;
	}

	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}

	@Override
	public String toString() {
		String s = "";
		for (Article a : articles) {
			s = s + a.getId() + "-";
		}
		return s.substring(0, s.length() - 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((articles == null) ? 0 : articles.hashCode());
		result = prime * result + length;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (articles == null) {
			if (other.articles != null)
				return false;
		} else if (!articles.equals(other.articles))
			return false;
		if (length != other.length)
			return false;
		return true;
	}

}
