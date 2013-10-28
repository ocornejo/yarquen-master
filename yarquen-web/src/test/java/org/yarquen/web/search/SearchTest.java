package org.yarquen.web.search;

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
public class SearchTest {
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
	public void testSearchNotermsSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		assertEquals("No results found",
				driver.findElement(By.id("searchResultsText")).getText());
		driver.findElement(By.linkText("Yarquen")).click();
	}

	@Test
	public void testSearchGamesSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("Games");
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		assertEquals("Windows Phone 8: Games",
				driver.findElement(By.linkText("Windows Phone 8: Games"))
						.getText());
		assertEquals("Chris Koenig",
				driver.findElement(By.linkText("Chris Koenig")).getText());
		assertEquals(
				"09/27/2012",
				driver.findElement(
						By.xpath("//ol[@id='results']/li/div/span/small[4]/span[2]"))
						.getText());
		assertEquals("http://dzone.com/articles/windows-phone-8-games", driver
				.findElement(By.cssSelector("li > div > small.muted"))
				.getText());
		assertEquals(
				"Designing Retro-Looking Games for Windows 8",
				driver.findElement(
						By.linkText("Designing Retro-Looking Games for Windows 8"))
						.getText());
		assertEquals("Jesse Freeman",
				driver.findElement(By.linkText("Jesse Freeman")).getText());
		assertEquals(
				"10/24/2012",
				driver.findElement(
						By.xpath("//ol[@id='results']/li[2]/div/span/small[4]/span[2]"))
						.getText());
		driver.findElement(By.linkText("Yarquen")).click();
	}

	@Test
	public void testSearchFacetsSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("Games");
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		assertEquals("Windows Phone 8: Games",
				driver.findElement(By.linkText("Windows Phone 8: Games"))
						.getText());
		driver.findElement(By.linkText("Simon Jackson")).click();
		assertEquals(
				"Converting DirectX .X Files for Use in Games",
				driver.findElement(
						By.linkText("Converting DirectX .X Files for Use in Games"))
						.getText());
		assertEquals("Simon Jackson",
				driver.findElement(By.linkText("Simon Jackson")).getText());
		assertEquals(
				"11/08/2012",
				driver.findElement(
						By.xpath("//ol[@id='results']/li/div/span/small[4]/span[2]"))
						.getText());
		assertEquals(
				"XNA to MonoGame... And Beyond!",
				driver.findElement(
						By.linkText("XNA to MonoGame... And Beyond!"))
						.getText());
		assertEquals(
				"Simon Jackson",
				driver.findElement(
						By.xpath("(//a[contains(text(),'Simon Jackson')])[2]"))
						.getText());
		assertEquals(
				"10/29/2012",
				driver.findElement(
						By.xpath("//ol[@id='results']/li[2]/div/span/small[4]/span[2]"))
						.getText());
		driver.findElement(By.linkText("Yarquen")).click();
	}

	@Test
	public void testSearchFacets2Success() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.cssSelector("div.hero-unit > a")).click();
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("query")).sendKeys("facets");
		driver.findElement(
				By.cssSelector("form.form-search > button.btn.btn-primary"))
				.click();
		driver.findElement(By.linkText("2012")).click();
		driver.findElement(By.linkText("Cloud")).click();
		assertEquals(
				"Defining the Enterprise Cloud Service: Singular Focus on the Customer",
				driver.findElement(
						By.linkText("Defining the Enterprise Cloud Service: Singular Focus on the Customer"))
						.getText());
		assertEquals(
				"Eric Berg",
				driver.findElement(
						By.xpath("(//a[contains(text(),'Eric Berg')])[2]"))
						.getText());
		assertEquals(
				"10/16/2012",
				driver.findElement(
						By.xpath("//ol[@id='results']/li/div/span/small[4]/span[2]"))
						.getText());
		assertEquals("Cloud", driver.findElement(By.linkText("Cloud"))
				.getText());
		driver.findElement(By.linkText("+ more details")).click();
		assertEquals("No summary was found for this result :(", driver
				.findElement(By.cssSelector("p")).getText());
		driver.findElement(By.linkText("Yarquen")).click();
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
