package org.yarquen.web.account;

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
public class EditAccountTest {
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
	public void testEditAccountSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.linkText("edit")).click();
		try {
			assertEquals("Test", driver.findElement(By.id("firstName"))
					.getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("User", driver.findElement(By.id("familyName"))
					.getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("test@test.cl", driver.findElement(By.id("email"))
					.getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("testuser", driver.findElement(By.id("username"))
					.getAttribute("value"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Test Name");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("User Name");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("testtest@testtest.cl");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("testusertest");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		driver.findElement(By.linkText("Test User")).click();
		assertEquals("Test Name User Name",
				driver.findElement(By.cssSelector("h1 > span")).getText());
		assertEquals("testusertest", driver.findElement(By.cssSelector("h3"))
				.getText());
		assertEquals("testtest@testtest.cl",
				driver.findElement(By.linkText("testtest@testtest.cl"))
						.getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditAccountLoginSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.linkText("edit")).click();
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("testusertest");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		driver.findElement(By.linkText("Logout")).click();
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testusertest");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		assertEquals("testusertest", driver.findElement(By.cssSelector("h3"))
				.getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditAccountEditpasswordSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.linkText("edit")).click();
		driver.findElement(By.linkText("Change Password")).click();
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("testtesttest");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("testtesttest");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("your password has been updated, please login.", driver
				.findElement(By.xpath("//div/span")).getText());
		driver.findElement(By.linkText("Logout")).click();
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtesttest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals("Test User", driver.findElement(By.linkText("Test User"))
				.getText());
		driver.findElement(By.linkText("Test User")).click();
		assertEquals("Test User",
				driver.findElement(By.cssSelector("h1 > span")).getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditAccountEmptyFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.linkText("edit")).click();
		driver.findElement(By.id("firstName")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("");
		driver.findElement(By.id("familyName")).click();
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("");
		driver.findElement(By.id("email")).click();
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("Enter your first name",
				driver.findElement(By.cssSelector("span.help-inline"))
						.getText());
		assertEquals(
				"Enter your family name",
				driver.findElement(
						By.xpath("//form[@id='registerHere']/fieldset/div[2]/div/span"))
						.getText());
		assertEquals(
				"Enter your email address",
				driver.findElement(
						By.xpath("//form[@id='registerHere']/fieldset/div[3]/div/span"))
						.getText());
		assertEquals(
				"Enter your username",
				driver.findElement(
						By.xpath("//form[@id='registerHere']/fieldset/div[4]/div/span"))
						.getText());
		driver.findElement(By.id("cancel")).click();
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
