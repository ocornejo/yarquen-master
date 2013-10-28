package org.yarquen.article;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Article repository
 * 
 * @author Jorge Riquelme Santana
 * @date 09/01/2013
 * 
 */
public interface ArticleRepository extends CrudRepository<Article, String> {
	Article findByTitle(String title);

	Article findByUrl(String url);

	/**
	 * Article list just with id, title, providedSkills and requiredSkill if
	 * exists
	 * 
	 * @return article list
	 */
	@Query(value = "{providedSkills:{$exists:true}}", fields = "{title:1,requiredSkills:1,providedSkills:1}")
	List<Article> findAllWithProvidedSkills();
}
