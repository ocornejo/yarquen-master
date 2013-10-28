package org.yarquen.crawler.coextractor.infoq;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.ccil.cowan.tagsoup.Parser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.infoq.InfoqCOExtractor;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * Tests for {@link InfoqCOExtractor}
 * 
 * @author Choon-ho Yoon
 * @date Apr 11, 2013
 * @version
 * 
 */
public class ArchInfoqCOExtractorTest {

	/**
	 * Strip out XML namespace, so that XPath can be easily used to extract
	 * elements.
	 * 
	 */
	private static class DowngradeXmlFilter extends XMLFilterImpl {

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			String lower = localName.toLowerCase();
			super.endElement(XMLConstants.NULL_NS_URI, lower, lower);
		}

		@Override
		public void endPrefixMapping(String prefix) {
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes atts) throws SAXException {
			String lower = localName.toLowerCase();

			AttributesImpl attributes = new AttributesImpl();
			for (int i = 0; i < atts.getLength(); i++) {
				String local = atts.getLocalName(i);
				String qname = atts.getQName(i);
				if (!XMLConstants.NULL_NS_URI.equals(atts.getURI(i).length())
						&& !local.equals(XMLConstants.XMLNS_ATTRIBUTE)
						&& !qname
								.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
					attributes.addAttribute(atts.getURI(i), local, qname,
							atts.getType(i), atts.getValue(i));
				}
			}

			super.startElement(XMLConstants.NULL_NS_URI, lower, lower,
					attributes);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) {
		}
	}

	private SAXReader reader;

	@Before
	public void setup() {
		reader = new SAXReader(new Parser());
		reader.setXMLFilter(new DowngradeXmlFilter());
		reader.setEncoding("UTF-8");
	}

	@Test
	public void real_question_why_testing() throws DocumentException {
		final InputStream resourceAsStream = getClass().getResourceAsStream(
				"/The Real Question is Why .html");
		Assert.assertNotNull(resourceAsStream);
		final Document parsedContent = reader.read(resourceAsStream);

		final InfoqCOExtractor articleExtractor = new InfoqCOExtractor();
		String url = "http://www.infoq.com/articles/real-question-why";
		String hostAddress = "";
		String parsedText = getPlainText(parsedContent);
		String language = "";
		String title = "";
		Outlink[] outlinks = new Outlink[] {};
		Map<String, String> parsedMeta = new HashMap<String, String>();
		final ParsedDatum parsedDatum = new ParsedDatum(url, hostAddress,
				parsedText, language, title, outlinks, parsedMeta);
		final ArticleDatum article = articleExtractor.extractArticles(
				parsedDatum, parsedContent,
				(List<Outlink>) new ArrayList<Outlink>());

		Assert.assertEquals("The Real Question is Why?", article.getTitle());
		Assert.assertEquals("Gordon Weir and Russ Miles", article.getAuthor());
		Assert.assertEquals("04/08/2013", article.getDate());
		Assert.assertEquals(
				"In the 1990s we were at an all time low in the software development industry. Programming was becoming a mainstream profession and we were mostly terrible at it. A gathering of some of the best minds in the industry occurred in 2001 and, while I suspect they couldn’t agree on a methodology, what they did agree on has had the biggest impact on the industry, ever. What it did was answer the problems of the ‘90s. And they created a community for like-minded people to discuss how best to deliver software. Now that Agile is becoming mainstream and we are getting a lot better at building software successfully, a new problem is starting to become apparent.",
				article.getSummary());
		Assert.assertEquals("http://www.infoq.com/articles/real-question-why",
				article.getUrl());

		Assert.assertEquals(5, article.getKeywords().length);
		String[] kw = new String[] { "Process", "Adopting Agile",
				"Agile in the Enterprise", "Business Value", "Agile" };
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(article.getKeywords()[i], kw[i]);
		}
	}

	@Test
	public void key_takeaway_testing() throws DocumentException {
		final InputStream resourceAsStream = getClass()
				.getResourceAsStream(
						"/Key Takeaway Points and Lessons Learned from QCon London 2013.html");
		Assert.assertNotNull(resourceAsStream);
		final Document parsedContent = reader.read(resourceAsStream);

		final InfoqCOExtractor articleExtractor = new InfoqCOExtractor();
		String url = "http://www.infoq.com/articles/QCon-London-2013";
		String hostAddress = "";
		String parsedText = getPlainText(parsedContent);
		String language = "";
		String title = "";
		Outlink[] outlinks = new Outlink[] {};
		Map<String, String> parsedMeta = new HashMap<String, String>();
		final ParsedDatum parsedDatum = new ParsedDatum(url, hostAddress,
				parsedText, language, title, outlinks, parsedMeta);
		final ArticleDatum article = articleExtractor.extractArticles(
				parsedDatum, parsedContent,
				(List<Outlink>) new ArrayList<Outlink>());

		Assert.assertEquals(
				"Key Takeaway Points and Lessons Learned from QCon London 2013",
				article.getTitle());
		Assert.assertEquals("Abel Avram", article.getAuthor());
		Assert.assertEquals("04/05/2013", article.getDate());
		Assert.assertEquals(
				"Going into its seventh year, QCon London 2013 featured thought-provoking and engaging keynotes from MIT’s Barbara Liskov, Perl Boffin Damian Conway, as well as Wiki Creator Ward Cunningham, and Greg Young.",
				article.getSummary());
		Assert.assertEquals("http://www.infoq.com/articles/QCon-London-2013",
				article.getUrl());

		Assert.assertEquals(8, article.getKeywords().length);
		String[] kw = new String[] { "Enterprise Architecture", "Operations",
				"Process", "Architecture", "Development", "QCon London 2013",
				"QCon", "New York Jun 10-14" };
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(article.getKeywords()[i], kw[i]);
		}
	}

	@Test
	public void choosing_right_esb_testing() throws DocumentException {
		final InputStream resourceAsStream = getClass().getResourceAsStream(
				"/Choosing the Right ESB for Your Integration Needs.html");
		Assert.assertNotNull(resourceAsStream);
		final Document parsedContent = reader.read(resourceAsStream);

		final InfoqCOExtractor articleExtractor = new InfoqCOExtractor();
		String url = "http://www.infoq.com/articles/ESB-Integration";
		String hostAddress = "";
		String parsedText = getPlainText(parsedContent);
		String language = "";
		String title = "";
		Outlink[] outlinks = new Outlink[] {};
		Map<String, String> parsedMeta = new HashMap<String, String>();
		final ParsedDatum parsedDatum = new ParsedDatum(url, hostAddress,
				parsedText, language, title, outlinks, parsedMeta);
		final ArticleDatum article = articleExtractor.extractArticles(
				parsedDatum, parsedContent,
				(List<Outlink>) new ArrayList<Outlink>());

		Assert.assertEquals(
				"Choosing the Right ESB for Your Integration Needs",
				article.getTitle());
		Assert.assertEquals("Kai Wähner", article.getAuthor());
		Assert.assertEquals("04/02/2013", article.getDate());
		Assert.assertEquals(
				"Different applications within companies and between different companies need to communicate with each other. The Enterprise Service Bus (ESB) has been established as a tool to support application integration. But what is an ESB? When is it better to use an integration suite? And which product is best suited for the next project? This article explains why there is no silver bullet and why an ESB can also be the wrong choice. Selecting the right product is essential for project success.",
				article.getSummary());
		Assert.assertEquals("http://www.infoq.com/articles/ESB-Integration",
				article.getUrl());

		Assert.assertEquals(25, article.getKeywords().length);
		String[] kw = new String[] { "Enterprise Architecture", "Websphere",
				"WSO2", "Application Servers", "Mule", "Fuse", "FuseSource",
				"SOA Platforms", "ESB", "Fusion Middleware", "Java", "Oracle",
				"RedHat", "SOA", "IBM", "Middleware", "Web Servers",
				"Enterprise Architecture", "Architecture", "Infrastructure",
				"Web Development", "Enterprise", "Integration", "Talend",
				"Services" };
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(article.getKeywords()[i], kw[i]);
		}
	}

	private String getPlainText(Document doc) {
		BodyContentHandler bodyContentHandler = new BodyContentHandler();
		XHTMLContentHandler xhtmlContentHandler = new XHTMLContentHandler(
				bodyContentHandler, new Metadata());
		SAXWriter writer = new SAXWriter(xhtmlContentHandler);
		try {
			writer.write(doc);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}

		return bodyContentHandler.toString();
	}
}
