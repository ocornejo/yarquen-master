package org.yarquen.crawler.extractor.infoq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.ArticleExtractor;
import org.yarquen.crawler.util.DocumentUtils;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 05/08/2012
 * 
 */
public class InfoqCOExtractor implements ArticleExtractor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(InfoqCOExtractor.class);

	@Override
	public ArticleDatum extractArticles(ParsedDatum parsedDatum, Document doc,
			List<Outlink> outlinks) {
		LOGGER.info("extracting articles from {} {}", parsedDatum.getUrl(),
				parsedDatum.getHostAddress());

		final Node articleNode = doc
				.selectSingleNode("//div[@class='box-content-5']");
		if (articleNode != null) {

			// Title Extraction
			final Node titleNode = articleNode.selectSingleNode("./h1/a");
			String title = "";
			if (titleNode != null) {
				title = titleNode.getText().trim();
			}
			LOGGER.trace("Title: [{}]", title);

			// Date Extraction
			final Node dateNode = articleNode
					.selectSingleNode("./p[@class='info']/text()[2]");
			String date = "";
			String formattedDate = "";
			if (dateNode != null) {
				date = dateNode.getText().trim();
				formattedDate = parseDate(date);
			}
			LOGGER.trace("Date: [{}]", formattedDate);

			// Author Extraction
			final Node authorNode = articleNode
					.selectSingleNode("./p[@class='info']/strong/a/text()");
			String author = "";
			if (authorNode != null) {
				author = authorNode.getText().trim();
			}
			LOGGER.trace("Author: [{}]", author);

			// Summary Extraction
			final Node summaryNode = articleNode.selectSingleNode("./p[3]");
			String summary = "";
			if (summaryNode != null) {
				summary = summaryNode.getText().trim();
			}
			LOGGER.trace("Summary: [{}]", summary);

			// Keywords Extractor
			String[] keywords = null;
			@SuppressWarnings("unchecked")
			final List<Node> tagNodes = articleNode
					.selectNodes("./dl[@class='tags2']/dd");
			if (tagNodes != null && !tagNodes.isEmpty()) {
				LOGGER.trace("[{}] keywords detected", tagNodes.size());
				keywords = new String[tagNodes.size()];
				int i = 0;
				for (Node node : tagNodes) {
					final String kw = node.selectSingleNode("./a/text()")
							.getText().trim();
					LOGGER.trace("Keyword[{}]: [{}]", i, kw);
					keywords[i++] = kw;
				}
			} else {
				// FIXME
				LOGGER.trace("No Keywords Found.");
				return null;
			}

			// Plain Text Extractor
			String plainText = DocumentUtils.getPlainText(articleNode);

			final ArticleDatum article = new ArticleDatum();
			article.setUrl(parsedDatum.getUrl());
			article.setTitle(title);
			article.setDate(formattedDate);
			article.setSummary(summary);
			article.setAuthor(author);
			article.setKeywords(keywords);
			article.setPlainText(plainText);
			return article;

		} else {
			return null;
		}
	}

	/**
	 * Formats the article date to match articles dates '09/07/2008'
	 * 
	 * @param date
	 * @return
	 */
	private String parseDate(String date) {

		final String datePattern = "on\\s+([a-zA-Z]+.*)";
		final Pattern pattern = Pattern.compile(datePattern);
		final Matcher matcher = pattern.matcher(date);
		String dateString = null;
		if (matcher.find()) {
			dateString = matcher.group(1);
			LOGGER.debug("Date Found: [{}]", dateString);
		} else {
			throw new IllegalArgumentException("Date not found in string "
					+ date + " (expecting pattern " + datePattern + ")");
		}

		final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy",
				Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		final SimpleDateFormat articleFormat = new SimpleDateFormat(
				"MM/dd/yyyy", Locale.ENGLISH);
		articleFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		try {
			final Date fixedDate = sdf.parse(dateString);
			LOGGER.debug("Fixed Date: [{}]", fixedDate);
			final String transformedDate = articleFormat.format(fixedDate);
			LOGGER.debug("New Date: [{}]", transformedDate);
			return transformedDate;

		} catch (ParseException e) {
			LOGGER.debug("error while parsing a date.", e);
		}

		return null;

	}
}
