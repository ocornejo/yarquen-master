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
public class SignUpTest {
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
	public void testSignupSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Elizabeth");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Sabrin");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("eli.sab@hotmail.com");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("elizabeth");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("elizabeth");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("elizabeth");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("account created successfully",
				driver.findElement(By.xpath("//div/span")).getText());
	}

	@Test
	public void testSignupUsernameFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Choon");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Yoon");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("choon.yoon@hotmail.com");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("KimDongHwan");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("password");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("password");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("[username: KimDongHwan already exists]", driver
				.findElement(By.cssSelector("#errors > ul > li")).getText());
	}

	@Test
	public void testSignupEmailFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Choon");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Yoon");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys(
				"ffx.tidus.master@hotmail.com");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("KimJaeHoon");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("password");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("password");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("[email: ffx.tidus.master@hotmail.com already exists]",
				driver.findElement(By.cssSelector("#errors > ul > li"))
						.getText());
	}

	@Test
	public void testSignupEmptyFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
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
		assertEquals(
				"Enter your password",
				driver.findElement(
						By.xpath("//form[@id='registerHere']/fieldset/div[5]/div/span"))
						.getText());
		assertEquals(
				"Enter confirm password",
				driver.findElement(
						By.xpath("//form[@id='registerHere']/fieldset/div[6]/div/span"))
						.getText());
	}

	@Test
	public void testSignupPasswordmatchFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Johana");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Busqueli");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("correo@hotmail.com");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("choon-ho");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("password");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("notmatch");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("Password and Confirm Password must match", driver
				.findElement(By.xpath("//div[@id='confPassInput']/span"))
				.getText());
	}

	@Test
	public void testSignupEmailFormatFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("First");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Name");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("correoinvalido");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("username");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("password");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("password");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("Enter valid email address",
				driver.findElement(By.cssSelector("span.help-inline"))
						.getText());
	}

	@Test
	public void testSignupLoginSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign Up")).click();
		driver.findElement(By.id("firstName")).clear();
		driver.findElement(By.id("firstName")).sendKeys("Alvaro");
		driver.findElement(By.id("familyName")).clear();
		driver.findElement(By.id("familyName")).sendKeys("Valdebenito");
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("arvaldeb@hotmail.com");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("arvaldeb");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("arvaldeb");
		driver.findElement(By.id("cpwd")).clear();
		driver.findElement(By.id("cpwd")).sendKeys("arvaldeb");
		driver.findElement(By.cssSelector("button.btn.btn-success")).click();
		assertEquals("account created successfully",
				driver.findElement(By.xpath("//div/span")).getText());
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("arvaldeb");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("arvaldeb");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals("Alvaro Valdebenito",
				driver.findElement(By.linkText("Alvaro Valdebenito")).getText());
		driver.findElement(By.linkText("Alvaro Valdebenito")).click();
		assertEquals("Alvaro Valdebenito",
				driver.findElement(By.cssSelector("h1 > span")).getText());
		assertEquals("arvaldeb", driver.findElement(By.cssSelector("h3"))
				.getText());
		assertEquals("arvaldeb@hotmail.com",
				driver.findElement(By.linkText("arvaldeb@hotmail.com"))
						.getText());
		assertEquals("Alvaro Valdebenito",
				driver.findElement(By.xpath("//div[@id='reports']/div/span"))
						.getText());
		driver.findElement(By.linkText("Logout")).click();
		assertEquals("A focused, semantic, distributed crawler :)", driver
				.findElement(By.cssSelector("p")).getText());
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
