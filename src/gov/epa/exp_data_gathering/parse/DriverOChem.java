package gov.epa.exp_data_gathering.parse;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverOChem {
	
	public static void scrapeOChem(String propertyNumber,int endIndex) {
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\GSincl01\\Documents\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("https://ochem.eu/");
		driver.manage().window().maximize();
		
		try {
			WebElement login = driver.findElement(By.linkText("log in"));
			login.click();
			Thread.sleep(5000);
			
			driver.switchTo().frame(2);
			
			try {
				WebElement button1 = driver.findElement(By.className("button-link"));
				button1.click();
				Thread.sleep(5000);
				
				WebElement button2 = driver.findElement(By.className("button-link"));
				button2.click();
				Thread.sleep(5000);
			} catch (Exception ex) {
				System.out.println("Already logged in!");
			}
			
			String url = "https://ochem.eu/epbrowser/show.do?property="+propertyNumber;
			driver.navigate().to(url);
			Thread.sleep(15000);
			
			driver.switchTo().frame(2);
			
			WebElement displayCount = driver.findElement(By.name("pagesize"));
			displayCount.click();
			displayCount.sendKeys("100" + Keys.ENTER);
			Thread.sleep(15000);
			
			for (int i = 1; i <= endIndex; i++) {
				WebElement selectAll = driver.findElement(By.cssSelector("[title=\"Select records on currently visible page\"]"));
				selectAll.click();
				Thread.sleep(20000);
				
				if (i < endIndex) {
					WebElement pageInput = driver.findElement(By.id("pageInput"));
					pageInput.click();
					Thread.sleep(2000);
					
					pageInput.sendKeys(""+Keys.DELETE+Keys.DELETE+Keys.BACK_SPACE+Keys.BACK_SPACE);
					Thread.sleep(2000);
					
					String nextPage = String.valueOf(i+1);
					pageInput.sendKeys(Keys.DELETE+nextPage+Keys.ENTER);
					Thread.sleep(15000);
				}
			}
			
			driver.navigate().to("https://ochem.eu/basket/show.do");
			Thread.sleep(5000);
			
			driver.switchTo().frame(2);
			
			// WebElement selectedRecords = driver.findElement(By.cssSelector("[test=\"test-Selected records\"]"));
			WebElement recordMenu = driver.findElement(By.cssSelector("[src=\"img/icons/xls.gif\"]"));
			recordMenu.click();
			WebElement exportBasket = driver.findElement(By.cssSelector("[action=\"basket\"]"));
			exportBasket.click();
			Thread.sleep(2000);
			
			driver.switchTo().defaultContent();
			driver.switchTo().frame(3);
			
			WebElement externalID = driver.findElement(By.name("EXTERNAL_ID"));
			externalID.click();
			WebElement articleN = driver.findElement(By.name("N"));
			articleN.click();
			WebElement pubID = driver.findElement(By.name("ARTICLE"));
			pubID.click();
			WebElement selectUnits = driver.findElement(By.name("unit-"+propertyNumber));
			selectUnits.click();
			// Will need switch statement to set units for other properties
			selectUnits.sendKeys("g/L"+Keys.ENTER);
			
			WebElement getXLS = driver.findElement(By.cssSelector("[format=\"xls\"]"));
			getXLS.click();
			Thread.sleep(30000);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			driver.quit();
		}
	}
	
	public static void main(String[] args) {
		scrapeOChem("46",1);
	}
	
}
