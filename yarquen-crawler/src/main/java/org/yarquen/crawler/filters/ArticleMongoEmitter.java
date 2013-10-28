package org.yarquen.crawler.filters;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.crawler.datum.ArticleDatum;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntryCollector;

import com.bixolabs.cascading.NullContext;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 11-04-2013
 * 
 */
@SuppressWarnings("serial")
public class ArticleMongoEmitter extends BaseOperation<NullContext> implements
		Function<NullContext> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleMongoEmitter.class);
	// FIXME: uuuggh :S
	private static ArticleRepository articleRepository;

	public ArticleMongoEmitter() {
		super(new Fields("line"));
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
	}

	@Override
	public void operate(FlowProcess process, FunctionCall<NullContext> funcCall) {
		final ArticleDatum datum = new ArticleDatum(funcCall.getArguments()
				.getTuple());
		LOGGER.debug("emitting xml for {}", datum.getUrl());

		// to mongo
		saveDatum(datum);

		// to control file
		final TupleEntryCollector collector = funcCall.getOutputCollector();
		final String outResult = String.format("%s", datum.getUrl());
		collector.add(new Tuple(outResult));
	}

	@Override
	public void prepare(FlowProcess process,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(process, operationCall);
	}

	public void setArticleRepository(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	private void saveDatum(ArticleDatum articleDatum) {
		final Article article = new Article();
		article.setAuthor(articleDatum.getAuthor());
		article.setDate(articleDatum.getDate());

		final List<String> keywords = new ArrayList<String>(
				articleDatum.getKeywords().length);
		for (String keyword : articleDatum.getKeywords()) {
			keywords.add(keyword);
		}
		article.setKeywords(keywords);

		article.setPlainText(articleDatum.getPlainText());
		article.setSummary(articleDatum.getSummary());
		article.setTitle(articleDatum.getTitle());
		article.setUrl(articleDatum.getUrl());

		final Article foundArticle = articleRepository.findByTitle(article
				.getTitle());
		if (foundArticle == null) {
			articleRepository.save(article);
		} else {
			LOGGER.debug("An article with title {} already exists, ignoring",
					article.getTitle());
		}
	}
}
