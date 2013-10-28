package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.skill.Skill;

/**
 * Lucene index builder
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * 
 */
public class BatchIndexBuilder {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchIndexBuilder.class);

	@Resource
	private ArticleRepository articleRepository;
	private FacetFields facetDocumentBuilder;
	private Directory indexDirectory;
	@Value("#{config.indexDirectory}")
	private String indexDirectoryPath;
	private IndexWriter indexWriter;
	private Directory taxoDirectory;
	@Value("#{config.taxoDirectory}")
	private String taxoDirectoryPath;
	private TaxonomyWriter taxoWriter;

	public void createIndex() throws IOException {
		// first commit to create directories
		indexWriter.commit();
		taxoWriter.commit();

		// categories
		for (Article.Facets f : Article.Facets.values()) {
			taxoWriter.addCategory(new CategoryPath(f.toString()));
		}
		taxoWriter.commit();

		final Iterable<Article> articles = articleRepository.findAll();
		int c = 0;
		for (Article article : articles) {
			addArticle(indexWriter, taxoWriter, article);
			c++;
		}
		// commit
		taxoWriter.commit();
		indexWriter.commit();

		taxoWriter.close();
		indexWriter.close();

		taxoDirectory.close();
		indexDirectory.close();
		LOGGER.debug("{} articles indexed", c);
	}

	@PostConstruct
	public void init() throws IOException {
		final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_41, analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);

		indexDirectory = new NIOFSDirectory(new File(indexDirectoryPath));
		indexWriter = new IndexWriter(indexDirectory, config);

		taxoDirectory = new NIOFSDirectory(new File(taxoDirectoryPath));
		taxoWriter = new DirectoryTaxonomyWriter(taxoDirectory,
				OpenMode.CREATE_OR_APPEND);
		facetDocumentBuilder = new FacetFields(taxoWriter);
	}

	private void addArticle(IndexWriter indexWriter, TaxonomyWriter taxoWriter,
			Article article) throws IOException {
		// doc
		final Document doc = new Document();
		// add fields
		addFieldsToDoc(doc, article);
		// add facets
		addFacetsToDoc(doc, article);

		// add doc to index
		indexWriter.addDocument(doc);
	}

	private void addFacetsToDoc(Document doc, Article article)
			throws IOException {
		final List<CategoryPath> facets = new ArrayList<CategoryPath>();

		if (article.getAuthor() != null) {
			facets.add(new CategoryPath(Article.Facets.AUTHOR.toString(),
					article.getAuthor()));
		}
		if (article.getDate() != null) {
			final String date = article.getDate();
			// FIXME: the date shouldn't be a plain String :o
			final String year = date.substring(date.lastIndexOf("/") + 1);
			facets.add(new CategoryPath(Article.Facets.YEAR.toString(), year));
		}
		if (article.getKeywords() != null) {
			for (String kw : article.getKeywords()) {
				facets.add(new CategoryPath(Article.Facets.KEYWORD.toString(),
						kw));
			}
		}
		if (article.getProvidedSkills() != null) {
			for (Skill skill : article.getProvidedSkills()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.PROVIDED_SKILL
								.toString());
				final CategoryPath categoryPath = new CategoryPath(components);
				facets.add(categoryPath);
			}
		}
		if (article.getRequiredSkills() != null) {
			for (Skill skill : article.getRequiredSkills()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.REQUIRED_SKILL
								.toString());
				final CategoryPath categoryPath = new CategoryPath(components);
				facets.add(categoryPath);
			}
		}

		facetDocumentBuilder.addFields(doc, facets);
	}

	private void addFieldsToDoc(Document doc, Article article) {
		doc.add(new TextField(Article.Fields.ID.toString(), article.getId(),
				Field.Store.YES));
		doc.add(new TextField(Article.Fields.PLAIN_TEXT.toString(), article
				.getPlainText(), Field.Store.YES));
		doc.add(new TextField(Article.Fields.TITLE.toString(), article
				.getTitle(), Field.Store.YES));
		doc.add(new StringField(Article.Fields.URL.toString(),
				article.getUrl(), Field.Store.YES));
	}
}
