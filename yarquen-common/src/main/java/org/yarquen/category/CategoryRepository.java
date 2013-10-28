package org.yarquen.category;

import org.springframework.data.repository.CrudRepository;

/**
 * Category repo
 * 
 * @author Jorge Riquelme Santana
 * @date 18/01/2013
 * 
 */
public interface CategoryRepository extends CrudRepository<Category, String> {
	Category findByCode(String code);
}
