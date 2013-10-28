package org.yarquen.web.record;

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
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.web.utils.YarquenDBUtils;

@IfProfileValue(name = "test-groups", value = "ftests")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/functional-test.xml")
public class RecordTest {
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
	public void testRecordOriginalSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("play");
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		driver.findElement(
				By.xpath("//a[@id='Web applications with the Play framework']/span"))
				.click();
		assertEquals("No changes made to the article. See the original", driver
				.findElement(By.cssSelector("div.span4 > div > p")).getText());
		driver.findElement(By.linkText("See the original")).click();
		assertEquals("Web applications with the Play framework", driver
				.findElement(By.xpath("//td[2]")).getText());
		assertEquals("Giorgio Sironi",
				driver.findElement(By.xpath("//tr[2]/td[2]")).getText());
		assertEquals("http://css.dzone.com/articles/web-applications-play",
				driver.findElement(By.xpath("//tr[4]/td[2]")).getText());
		assertEquals(
				"The first impressions from the documentation is very good: Play is a full stack framework already integrate with Hibernate, memcached and other tools. This is not necessarily the best choice for the long term, but it's a bliss for a PHP programmer who wants to experience a new platform without debugging exceptions (as long as the integration works well.)",
				driver.findElement(By.xpath("//td[2]/p")).getText());
		assertEquals("frameworks",
				driver.findElement(By.cssSelector("span.label")).getText());
		assertEquals("java", driver.findElement(By.xpath("//span[2]/span"))
				.getText());
		assertEquals("JVM", driver.findElement(By.xpath("//span[3]/span"))
				.getText());
		assertEquals("play", driver.findElement(By.xpath("//span[4]/span"))
				.getText());
		assertEquals("Scala", driver.findElement(By.xpath("//span[5]/span"))
				.getText());
		assertEquals("Server-side",
				driver.findElement(By.xpath("//span[6]/span")).getText());
		assertEquals("web applications",
				driver.findElement(By.xpath("//span[7]/span")).getText());
		assertEquals("Frameworks",
				driver.findElement(By.xpath("//span[8]/span")).getText());
		assertEquals("No Provided Skills found.",
				driver.findElement(By.xpath("//tr[7]/td[2]/span")).getText());
		assertEquals("No Required Skills found.",
				driver.findElement(By.xpath("//tr[8]/td[2]/span")).getText());
		driver.findElement(By.linkText("Logout")).click();
		driver.findElement(By.linkText("Yarquen")).click();
	}

	@Test
	public void testRecordHistorySuccessXhtml() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("play");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(
				By.xpath("//a[@id='enrichWeb applications with the Play framework']/span"))
				.click();
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys(
				"Web Applications with the Play framework");
		driver.findElement(By.id("date")).clear();
		driver.findElement(By.id("date")).sendKeys("11/10/2012");
		assertEquals(
				"",
				driver.findElement(
						By.xpath("//ul[@id='keywords']/li[8]/a/span[2]"))
						.getText());
		driver.findElement(
				By.xpath("//form[@id='enrichmentForm']/fieldset/div[4]/div"))
				.click();
		driver.findElement(By.id("summary")).clear();
		driver.findElement(By.id("summary"))
				.sendKeys(
						"The first impressions from the documentation is very good: Play is a full stack framework already integrate with Hibernate, memcached and other tools. This is not necessarily the best choice for the long term, but it's a bliss for a PHP programmer who wants to experience a new platform without debugging exceptions as long as the integration works well.");
		driver.findElement(By.xpath("//ul[@id='keywords']/li[8]/a/span[2]"))
				.click();
		driver.findElement(By.cssSelector("ins.jstree-icon")).click();
		driver.findElement(
				By.linkText("Gestión de Servicios de Tecnologías de la Información"))
				.click();
		driver.findElement(By.id("newSkillProvided")).click();
		new Select(driver.findElement(By.id("newSkillLevel")))
				.selectByVisibleText("Medium");
		driver.findElement(By.id("newSkillSave")).click();
		driver.findElement(
				By.xpath("//div[@id='categoryTree']/ul/li/ul/li[2]/ins"))
				.click();
		driver.findElement(
				By.xpath("//div[@id='categoryTree']/ul/li/ul/li[2]/ins"))
				.click();
		driver.findElement(
				By.xpath("//div[@id='categoryTree']/ul/li/ul/li[2]/ins"))
				.click();
		driver.findElement(
				By.xpath("//div[@id='categoryTree']/ul/li/ul/li[2]/ul/li[2]/ins"))
				.click();
		driver.findElement(By.linkText("PC Cards")).click();
		driver.findElement(By.id("newSkillRequired")).click();
		new Select(driver.findElement(By.id("newSkillLevel")))
				.selectByVisibleText("Advanced");
		driver.findElement(By.id("newSkillSave")).click();
		driver.findElement(By.name("submit")).click();
		driver.findElement(
				By.xpath("//a[@id='Web Applications with the Play framework']/span"))
				.click();
		assertEquals("Test User", driver.findElement(By.cssSelector("td"))
				.getText());
		driver.findElement(
				By.xpath("//table/tbody[@id='historyRecordsByUser']/tr/td[2]/a"))
				.click();
		assertEquals("Web Applications with the Play framework", driver
				.findElement(By.xpath("//td[2]")).getText());
		assertEquals("Giorgio Sironi",
				driver.findElement(By.xpath("//tr[2]/td[2]")).getText());
		assertEquals("11/10/2012", driver
				.findElement(By.xpath("//tr[3]/td[2]")).getText());
		assertEquals("11/10/2012", driver
				.findElement(By.xpath("//tr[3]/td[2]")).getText());
		assertEquals("http://css.dzone.com/articles/web-applications-play",
				driver.findElement(By.xpath("//tr[4]/td[2]")).getText());
		assertEquals(
				"The first impressions from the documentation is very good: Play is a full stack framework already integrate with Hibernate, memcached and other tools. This is not necessarily the best choice for the long term, but it's a bliss for a PHP programmer who wants to experience a new platform without debugging exceptions as long as the integration works well.",
				driver.findElement(By.xpath("//td[2]/p")).getText());
		assertEquals("frameworks",
				driver.findElement(By.cssSelector("span.label")).getText());
		assertEquals("java", driver.findElement(By.xpath("//span[2]/span"))
				.getText());
		assertEquals("JVM", driver.findElement(By.xpath("//span[3]/span"))
				.getText());
		assertEquals("play", driver.findElement(By.xpath("//span[4]/span"))
				.getText());
		assertEquals("Scala", driver.findElement(By.xpath("//span[5]/span"))
				.getText());
		assertEquals("Server-side",
				driver.findElement(By.xpath("//span[6]/span")).getText());
		assertEquals("web applications",
				driver.findElement(By.xpath("//span[7]/span")).getText());
		assertEquals(
				"Gestión de Servicios de Tecnologías de la Información: Medium",
				driver.findElement(By.xpath("//tr[7]/td[2]/span/span"))
						.getText());
		assertEquals("PC Cards: Advanced",
				driver.findElement(By.xpath("//tr[8]/td[2]/span/span"))
						.getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testRecordHistorydiffSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("play");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(
				By.xpath("//a[@id='enrichWeb applications with the Play framework']/span"))
				.click();
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys("Title Changed");
		driver.findElement(By.name("submit")).click();
		assertEquals("Title Changed",
				driver.findElement(By.linkText("Title Changed")).getText());
		driver.findElement(By.xpath("//a[@id='enrichTitle Changed']/span"))
				.click();
		driver.findElement(By.id("url")).clear();
		driver.findElement(By.id("url")).sendKeys("URL Changed");
		driver.findElement(By.id("author")).clear();
		driver.findElement(By.id("author")).sendKeys("Author Changed");
		driver.findElement(By.name("submit")).click();
		driver.findElement(By.xpath("//a[@id='enrichTitle Changed']/span"))
				.click();
		driver.findElement(By.id("date")).click();
		driver.findElement(By.id("date")).clear();
		driver.findElement(By.id("date")).sendKeys("11/10/2012");
		driver.findElement(By.cssSelector("span.ui-icon.ui-icon-close"))
				.click();
		Thread.sleep(1000);
		driver.findElement(By.cssSelector("span.ui-icon.ui-icon-close"))
				.click();
		Thread.sleep(1000);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillSave")).click();
		Thread.sleep(1000);
		driver.findElement(By.linkText("Software")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillProvided")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("newSkillSave")).click();
		Thread.sleep(1000);
		driver.findElement(By.name("submit")).click();
		driver.findElement(By.xpath("//a[@id='Title Changed']/span")).click();
		driver.findElement(
				By.xpath("//table/tbody[@id='historyRecordsByUser']/tr[3]/td[2]/a"))
				.click();
		driver.findElement(By.linkText("see differences")).click();
		assertEquals("Web applications with the Play framework", driver
				.findElement(By.cssSelector("del")).getText());
		assertEquals("Title Changed", driver.findElement(By.cssSelector("ins"))
				.getText());
		assertEquals("Not Changed",
				driver.findElement(By.xpath("//div[2]/span")).getText());
		assertEquals("Added",
				driver.findElement(By.cssSelector("span.label.label-info"))
						.getText());
		assertEquals(
				"Removed",
				driver.findElement(By.cssSelector("span.label.label-important"))
						.getText());
		assertEquals(
				"Test User",
				driver.findElement(
						By.cssSelector("div.span3 > div > div > strong"))
						.getText());
		driver.findElement(By.cssSelector("a[title=\"next change\"]")).click();
		assertEquals("Giorgio Sironi -> Author Changed",
				driver.findElement(By.xpath("//td[2]/span")).getText());
		assertEquals("http://css.dzone.com/articles/web-applications-play",
				driver.findElement(By.cssSelector("del")).getText());
		assertEquals("URL Changed", driver.findElement(By.cssSelector("ins"))
				.getText());
		driver.findElement(By.cssSelector("a[title=\"next change\"]")).click();
		assertEquals("11/10/2011 -> 11/10/2012",
				driver.findElement(By.xpath("//td[2]/span")).getText());
		assertEquals("URL Changed",
				driver.findElement(By.xpath("//tr[4]/td[2]")).getText());
		assertEquals("Software: Basic",
				driver.findElement(By.cssSelector("span.label.label-info"))
						.getText());
		assertEquals("Informática: Basic",
				driver.findElement(By.xpath("//tr[8]/td[2]/span/span"))
						.getText());
		assertEquals(
				"frameworks",
				driver.findElement(By.cssSelector("span.label.label-important"))
						.getText());
		assertEquals("java", driver.findElement(By.xpath("//span[8]/span"))
				.getText());
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
