package gov.epa.exp_data_gathering.parse;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import gov.epa.api.ExperimentalConstants;

public class DriverOChem {
	
	/**
	 * Bulk selects and downloads records from OChem automatically
	 * 
	 * @param propertyName Use ExperimentalConstants
	 * @param endIndex How many pages (100 records each) to download
	 * @param chromeDriverPath Path to your chromedriver.exe file
	 */
	public static void scrapeOChem(String propertyName,int endIndex,String chromeDriverPath) {
		int propertyNumber = 0;
		String desiredUnits = "";
		switch (propertyName) {
		case ExperimentalConstants.strDensity:
			propertyNumber = 510;
			desiredUnits = "g/cm3";
			break;
		case ExperimentalConstants.strMeltingPoint:
			propertyNumber = 1;
			desiredUnits = "Celsius";
			break;
		case ExperimentalConstants.strBoilingPoint:
			propertyNumber = 297;
			desiredUnits = "Celsius";
			break;
		case ExperimentalConstants.strFlashPoint:
			propertyNumber = 886;
			desiredUnits = "Celsius";
			break;
		case ExperimentalConstants.str_pKA:
			propertyNumber = 190;
			desiredUnits = "Log unit";
			break;
		case ExperimentalConstants.strLogKow:
			propertyNumber = 2;
			desiredUnits = "Log unit";
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			propertyNumber = 257;
			desiredUnits = "m^(3)*Pa/mol";
			break;
		case ExperimentalConstants.strVaporPressure:
			propertyNumber = 223;
			desiredUnits = "mm Hg";
			break;
		case ExperimentalConstants.strWaterSolubility:
			propertyNumber = 46;
			desiredUnits = "g/L";
			break;
		}
		
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		WebDriver driver = new ChromeDriver();
		driver.get("https://ochem.eu/");
		driver.manage().window().maximize();
		
		try {
			try {
				// Click login button
				WebElement login = driver.findElement(By.linkText("log in"));
				login.click();
				Thread.sleep(5000);
				
				driver.switchTo().frame(2);
				
				// Log in as guest
				WebElement button1 = driver.findElement(By.className("button-link"));
				button1.click();
				Thread.sleep(5000);
				
				// Accept terms
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
			
			// Display 100 records per page instead of default (5)
			WebElement displayCount = driver.findElement(By.name("pagesize"));
			displayCount.click();
			displayCount.sendKeys("100" + Keys.ENTER);
			Thread.sleep(15000);
			
			for (int i = 1; i <= endIndex; i++) {
				// Select records on current page
				WebElement selectAll = driver.findElement(By.cssSelector("[title=\"Select records on currently visible page\"]"));
				selectAll.click();
				Thread.sleep(20000);
				
				// Navigate to next page by typing page number into box (Charlie says this is most reliable)
				if (i < endIndex) {
					WebElement pageInput = driver.findElement(By.id("pageInput"));
					pageInput.click();
					Thread.sleep(2000);
					
					pageInput.clear();
					Thread.sleep(2000);
					
					String nextPage = String.valueOf(i+1);
					pageInput.clear();
					pageInput.sendKeys(nextPage+Keys.ENTER);
					Thread.sleep(15000);
				}
			}
			
			// View basket
			driver.navigate().to("https://ochem.eu/basket/show.do");
			Thread.sleep(5000);
			
			driver.switchTo().frame(2);
			
			// Open record download menu and click export
			WebElement recordMenu = driver.findElement(By.cssSelector("[src=\"img/icons/xls.gif\"]"));
			recordMenu.click();
			WebElement exportBasket = driver.findElement(By.cssSelector("[action=\"basket\"]"));
			exportBasket.click();
			Thread.sleep(2000);
			
			driver.switchTo().defaultContent();
			driver.switchTo().frame(3);
			
			// Select options for export
			WebElement externalID = driver.findElement(By.name("EXTERNAL_ID"));
			externalID.click();
			WebElement articleN = driver.findElement(By.name("N"));
			articleN.click();
			WebElement pubID = driver.findElement(By.name("ARTICLE"));
			pubID.click();
			WebElement selectUnits = driver.findElement(By.name("unit-"+propertyNumber));
			selectUnits.click();
			selectUnits.sendKeys(desiredUnits+Keys.ENTER);
			
			// Download!
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
		scrapeOChem(ExperimentalConstants.strMeltingPoint,2,"C:\\Users\\GSincl01\\Documents\\chromedriver.exe");
	}
	
}
