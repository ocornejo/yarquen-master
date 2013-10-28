package org.yarquen.account;

import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author Choon-ho Yoon
 * @date 14/03/2013
 * 
 */
public interface PasswordChangeRepository extends
		CrudRepository<PasswordChange, String> {

	PasswordChange findByToken(String token);

}
