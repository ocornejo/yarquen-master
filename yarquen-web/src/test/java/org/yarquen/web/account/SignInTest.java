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
public class SignInTest {
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
	public void testSigninSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		assertEquals("testuser", driver.findElement(By.cssSelector("h3"))
				.getText());
		assertEquals("Test User",
				driver.findElement(By.cssSelector("h1 > span")).getText());
		assertEquals("test@test.cl",
				driver.findElement(By.linkText("test@test.cl")).getText());
		driver.findElement(By.linkText("Logout")).click();
		assertEquals("Sign In", driver.findElement(By.linkText("Sign In"))
				.getText());
	}

	@Test
	public void testSinginCredentialsFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("dontexist");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals(": Wrong user or password",
				driver.findElement(By.id("error-message")).getText());
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
