package org.yarquen.author;

import org.springframework.data.repository.CrudRepository;

/**
 * Author repo
 * 
 * @author Jorge Riquelme Santana
 * @date 20/01/2013
 * 
 */
public interface AuthorRepository extends CrudRepository<Author, String> {

	Author findByName(String author);
}
