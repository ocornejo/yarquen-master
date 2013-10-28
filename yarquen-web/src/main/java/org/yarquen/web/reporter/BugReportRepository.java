package org.yarquen.web.reporter;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * Report and Suggestions Repository
 * 
 * @author Choon-ho Yoon
 * @date Apr 16, 2013
 * @version
 * 
 */
public interface BugReportRepository extends CrudRepository<BugReport, String> {

	List<BugReport> findAll(Sort sort);

}
