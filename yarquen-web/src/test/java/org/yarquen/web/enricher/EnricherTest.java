package org.yarquen.web.enricher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.web.utils.YarquenDBUtils;

@IfProfileValue(name = "test-groups", value = "ftests")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/functional-test.xml")
public class EnricherTest {
	@Value("#{config.baseUrl}")
	private String baseUrl;
	@Value("#{config.implicitWaitSecs}")
	private int implicitWaitSecs;

	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		YarquenDBUtils.restoreDatabase();
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testEnrichSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("search");
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		driver.findElement(By.cssSelector("span.icon-pencil")).click();
		try {
			assertEquals("Designing Search (Part 1): Search Box Design", driver
					.findElement(By.id("title")).getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals(
					"http://groovy.dzone.com/articles/designing-search-part-1-search",
					driver.findElement(By.id("url")).getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("Tony Russell-rose",
					driver.findElement(By.id("author")).getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("01/19/2012", driver.findElement(By.id("date"))
					.getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(
				By.xpath("//form[@id='enrichmentForm']/fieldset/div[5]/div"))
				.click();
		try {
			assertEquals(
					"In \n we reviewed models of information seeking, from an early focus on \ndocuments and queries through to a more nuanced understanding of search \nas an information journey driven by dynamic information needs. While \neach model emphasizes different aspects of the search process, what they\n share is the principle that search begins with an  which is articulated in some form of .\n What follows below is the first in a mini-series of articles exploring \nthe process of query formulation, starting with the most ubiquitous of \ndesign elements: the search box.",
					driver.findElement(By.id("summary")).getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		assertEquals("search",
				driver.findElement(By.cssSelector("span.tagit-label"))
						.getText());
		assertEquals("dzcontent_tutorial",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[2]/span"))
						.getText());
		assertEquals("Web Design",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[3]/span"))
						.getText());
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys(
				"Designing Search (Part 5): Search Box Design");
		driver.findElement(By.id("url")).clear();
		driver.findElement(By.id("url"))
				.sendKeys(
						"http://groovy.dzone.com/articles/designing-search-part-1-search/");
		driver.findElement(By.id("author")).clear();
		driver.findElement(By.id("author")).sendKeys("Tony Russell-Rose");
		driver.findElement(By.id("date")).clear();
		driver.findElement(By.id("date")).sendKeys("01/19/2013");
		driver.findElement(By.cssSelector("fieldset")).click();
		driver.findElement(By.id("summary")).clear();
		driver.findElement(By.id("summary"))
				.sendKeys(
						"In \n we reviewed models of information seeking, from an early focus on \ndocuments and queries through to a more nuanced understanding of search \nas an information journey driven by dynamic information needs. While \neach model emphasizes different aspects of the search process, what they\n share is the principle that search begins with an  which is articulated in some form of .\n What follows below is the first in a mini-series of articles exploring \nthe process of query formulation, starting with the most ubiquitous of \ndesign elements the search box.");
		Thread.sleep(1000);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillSave")).click();
		Thread.sleep(1000);
		driver.findElement(By.linkText("Comunicación")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillProvided")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillSave")).click();
		Thread.sleep(1000);
		driver.findElement(By.name("submit")).click();
		assertEquals(
				"Designing Search (Part 5): Search Box Design",
				driver.findElement(
						By.linkText("Designing Search (Part 5): Search Box Design"))
						.getText());
		assertEquals("Tony Russell-Rose",
				driver.findElement(By.linkText("Tony Russell-Rose")).getText());
		assertEquals(
				"01/19/2013",
				driver.findElement(
						By.xpath("//ol[@id='results']/li/div/span/small[4]/span[2]"))
						.getText());
		assertEquals(
				"http://groovy.dzone.com/articles/designing-search-part-1-search/",
				driver.findElement(
						By.id("urlhttp://groovy.dzone.com/articles/designing-search-part-1-search/"))
						.getText());
		assertEquals(
				"Tutorial",
				driver.findElement(
						By.xpath("(//a[contains(text(),'Tutorial')])[2]"))
						.getText());
		driver.findElement(By.linkText("+ more details")).click();
		assertEquals(
				"In we reviewed models of information seeking, from an early focus on documents and queries through to a more nuanced understanding of search as an information journey driven by dynamic information needs. While each model emphasizes different aspects of the search process, what they share is the principle that search begins with an which is articulated in some form of . What follows below is the first in a mini-series of articles exploring the process of query formulation, starting with the most ubiquitous of design elements the search box.",
				driver.findElement(By.cssSelector("div.resultDetails > p"))
						.getText());
		driver.findElement(By.linkText("Yarquen")).click();
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEnrichEmptytitleurlauthorFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("empty");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("span.icon-pencil")).click();
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys("");
		driver.findElement(By.id("url")).clear();
		driver.findElement(By.id("url")).sendKeys("");
		driver.findElement(By.id("author")).clear();
		driver.findElement(By.id("author")).sendKeys("");
		driver.findElement(By.name("submit")).click();
		assertEquals("may not be empty",
				driver.findElement(By.cssSelector("span.text-error > span"))
						.getText());
		assertEquals(
				"may not be empty",
				driver.findElement(
						By.xpath("//form[@id='enrichmentForm']/fieldset/div[2]/div/span/span"))
						.getText());
		assertEquals(
				"size must be between 5 and 100",
				driver.findElement(
						By.xpath("//form[@id='enrichmentForm']/fieldset/div[3]/div/span[2]/span"))
						.getText());
		driver.findElement(By.linkText("Yarquen")).click();
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEnrichKeywordsSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("games");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(
				By.xpath("//ol[@id='results']/li[2]/div/span/a/span")).click();
		assertEquals("retro",
				driver.findElement(By.cssSelector("span.tagit-label"))
						.getText());
		assertEquals("Tips and Tricks",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[2]/span"))
						.getText());
		assertEquals("Windows 8",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[3]/span"))
						.getText());
		assertEquals(".NET",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[4]/span"))
						.getText());
		assertEquals("& Windows",
				driver.findElement(By.xpath("//ul[@id='keywords']/li[5]/span"))
						.getText());
		driver.findElement(By.cssSelector("span.ui-icon.ui-icon-close"))
				.click();
		driver.findElement(By.name("submit")).click();
		assertEquals(
				"Tips and Tricks",
				driver.findElement(
						By.xpath("(//a[contains(text(),'Tips and Tricks')])[2]"))
						.getText());
		assertEquals(
				"Windows 8",
				driver.findElement(
						By.xpath("(//a[contains(text(),'Windows 8')])[3]"))
						.getText());
		assertEquals(
				".NET",
				driver.findElement(
						By.xpath("(//a[contains(text(),'.NET')])[2]"))
						.getText());
		assertEquals(
				"& Windows",
				driver.findElement(
						By.xpath("(//a[contains(text(),'& Windows')])[2]"))
						.getText());
		driver.findElement(By.linkText("Yarquen")).click();
		driver.findElement(By.linkText("Logout")).click();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

}
