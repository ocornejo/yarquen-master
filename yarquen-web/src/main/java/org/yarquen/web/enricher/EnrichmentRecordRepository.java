package org.yarquen.web.enricher;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for article versioning
 * 
 * @author Choon-ho Yoon
 * @date 19/03/2013
 * 
 */
public interface EnrichmentRecordRepository extends
		CrudRepository<EnrichmentRecord, String> {

	EnrichmentRecord findByVersionDateAndArticleId(Date versionDate,
			String articleId);

	List<EnrichmentRecord> findByArticleId(String articleId, Sort sort);
	
	List<EnrichmentRecord> findByAccountId(String accountId, Sort sort);

	List<EnrichmentRecord> findByArticleIdAndVersionDateBetween(
			String articleId, Date versionDate1, Date versionDate2, Sort sort);

	List<EnrichmentRecord> findByArticleIdAndVersionDateBefore(
			String articleId, Date versionDate, Sort sort);


}
