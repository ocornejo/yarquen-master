package org.yarquen.web.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
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
public class EditSkillsTest {
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
	public void testEditskillsSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(500);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Hardware")).click();
		Thread.sleep(500);
		new Select(driver.findElement(By.id("modalSkillLevel")))
				.selectByVisibleText("Medium");
		Thread.sleep(500);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(500);
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals("Informática",
				driver.findElement(By.cssSelector("#Informatica > span"))
						.getText());
		assertEquals("Hardware",
				driver.findElement(By.cssSelector("#Hardware > span"))
						.getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditskillsAddskillFail() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(500);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(1500);
		assertEquals(
				"The Skill Informática is already selected, please change in list",
				closeAlertAndGetItsText());
		Thread.sleep(500);
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditskillsRemoveSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		driver.findElement(
				By.cssSelector("ul.jstree-no-icons > li.jstree-last > ins.jstree-icon"))
				.click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Software de Programación")).click();
		Thread.sleep(500);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(500);
		driver.findElement(By.xpath("//div[@id='categoryTree']/ul/li[3]/ins"))
				.click();
		Thread.sleep(500);
		driver.findElement(By.linkText("Servidores de Redes")).click();
		Thread.sleep(500);
		driver.findElement(By.id("modalSkillSave")).click();
		new Select(driver.findElement(By.id("skills1.level")))
				.selectByVisibleText("Advanced");
		Thread.sleep(500);
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals(
				"Software/Software de Programación",
				driver.findElement(
						By.xpath("//li[@id='Software.SoftwareDeProgramacion']/span"))
						.getText());
		assertEquals(
				"Comunicación/Servidores de Redes",
				driver.findElement(
						By.xpath("//li[@id='Comunicacion.ServidoresDeRedes']/span"))
						.getText());
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		driver.findElement(By.cssSelector("span.categoryCloseBtn.close"))
				.click();
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		assertEquals(
				"Comunicación/Servidores de Redes",
				driver.findElement(
						By.xpath("//li[@id='Comunicacion.ServidoresDeRedes']/span"))
						.getText());
		driver.findElement(By.linkText("Logout")).click();
	}

	@Test
	public void testEditSkillsUpdatelevelSuccess() throws Exception {
		driver.get(baseUrl + "/yarquen/");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("j_username")).clear();
		driver.findElement(By.id("j_username")).sendKeys("testuser");
		driver.findElement(By.id("j_password")).clear();
		driver.findElement(By.id("j_password")).sendKeys("testtest");
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		driver.findElement(By.linkText("Test User")).click();
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		Thread.sleep(1000);
		driver.findElement(By.linkText("Informática")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(1000);
		driver.findElement(By.linkText("Software")).click();
		Thread.sleep(1000);
		driver.findElement(By.id("modalSkillSave")).click();
		Thread.sleep(1000);
		new Select(driver.findElement(By.id("skills0.level")))
				.selectByVisibleText("Advanced");
		Thread.sleep(1000);
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		Thread.sleep(1000);
		assertEquals("(Advanced)",
				driver.findElement(By.xpath("//li[@id='Informatica']/span[2]"))
						.getText());
		assertEquals("(Basic)",
				driver.findElement(By.xpath("//li[@id='Software']/span[2]"))
						.getText());
		driver.findElement(By.cssSelector("h3.modal-header > small > a"))
				.click();
		Thread.sleep(1000);
		new Select(driver.findElement(By.id("skills0.level")))
				.selectByVisibleText("Basic");
		Thread.sleep(1000);
		new Select(driver.findElement(By.id("skills1.level")))
				.selectByVisibleText("Advanced");
		Thread.sleep(1000);
		driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
		Thread.sleep(1000);
		assertEquals("Informática (Basic)",
				driver.findElement(By.id("Informatica")).getText());
		assertEquals("Software",
				driver.findElement(By.cssSelector("#Software > span"))
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

	private String closeAlertAndGetItsText() {
		Alert alert = driver.switchTo().alert();
		String alertText = "";
		try {
			alertText = alert.getText();
		} catch (NoAlertPresentException e) {

		}
		alert.accept();
		return alertText;
	}
}
