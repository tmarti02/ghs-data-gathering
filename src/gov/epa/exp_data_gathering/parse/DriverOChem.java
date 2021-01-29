package gov.epa.exp_data_gathering.parse;

import java.io.FileWriter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import gov.epa.api.ExperimentalConstants;

public class DriverOChem {
	
	/**
	 * Bulk selects and downloads records from OChem automatically
	 * 
	 * @param propertyName Use ExperimentalConstants
	 * @param startIndex What page to start download from
	 * @param endIndex How many pages (100 records each) to download; 0 to automatically download all
	 * @param chromeDriverPath Path to your chromedriver.exe file
	 */
	public static void scrapeOChem(String propertyName,int startIndex,int endIndex,String chromeDriverPath) {
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
		
		// Open new driver and connect to the OChem website
		long defaultWait = 30; // in seconds
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		WebDriver driver = new ChromeDriver();
		driver.get("https://ochem.eu/");
		driver.manage().window().maximize();
		
		try {
			try {
				// Click login button
				WebElement login = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.linkText("log in")));
				login.click();
				
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
				
				// Log in as guest
				WebElement button1 = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.className("button-link")));
				button1.click();
				
				// Accept terms
				WebElement button2 = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.className("button-link")));
				button2.click();
			} catch (Exception ex) {
				System.out.println("Already logged in!");
			}
			
			// Go to browser for selected property
			String url = "https://ochem.eu/epbrowser/show.do?property="+propertyNumber;
			driver.navigate().to(url);
			
			new WebDriverWait(driver,defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
			
			// Display 100 records per page instead of default (5)
			WebElement displayCount = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("pagesize")));
			displayCount.click();
			displayCount.sendKeys("100" + Keys.ENTER);
			
			// Wait for records to load
			new WebDriverWait(driver,defaultWait).until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
			
			// Scrape end index if not provided
			if (endIndex==0) {
				WebElement pager = driver.findElement(By.id("pager"));
				List<WebElement> arrows = pager.findElements(By.tagName("a"));
				int autoEndIndex = Integer.parseInt(arrows.get(1).getAttribute("page"));
				endIndex = autoEndIndex;
			}
			
			// Go to start page if not 1
			if (startIndex > 1) {	
				WebElement pageInput = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.id("pageInput")));
				pageInput.click();
				pageInput.clear();
				String nextPage = String.valueOf(startIndex);
				pageInput.sendKeys(nextPage+Keys.ENTER);
				
				// Wait for records to load
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
			}
			
			pageLoop:
			for (int i = startIndex; i <= endIndex; i++) {				
				// Select all records
				WebElement selectAll = new WebDriverWait(driver,defaultWait)
						.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[title=\"Select records on currently visible page\"]")));
				selectAll.click();
				
				// Wait for records to reload after selecting
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
				
				// Double-check popup is gone before proceeding - order of loading gets messed up sometimes
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.attributeContains(By.id("waitingDialog_mask"),"style","display: none"));
				
				// Navigate to next page by typing page number into box (Charlie says this is most reliable)
				if (i < endIndex) {	
					WebElement pageInput = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.id("pageInput")));
					pageInput.click();
					pageInput.clear();
					String nextPage = String.valueOf(i+1);
					pageInput.sendKeys(nextPage+Keys.ENTER);
					
					// Wait for records to load
					try {
						new WebDriverWait(driver,defaultWait).until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
					} catch (TimeoutException ex) {
						System.out.println("Timed out. Downloading pages "+startIndex+"-"+i+".");
						break pageLoop;
					}
				}
			}
			
			// Go to basket static page
			driver.navigate().to("https://ochem.eu/basket/show.do");
			
			try {
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
			} catch (TimeoutException ex) {
				// May encounter an error page when breaking out of pageLoop above
				// Just refresh and it should go through with basket intact
				driver.navigate().refresh();
				new WebDriverWait(driver,defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
			}
			
			// Open record download menu and click export
			WebElement recordMenu = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.cssSelector("[src=\"img/icons/xls.gif\"]")));
			recordMenu.click();
			WebElement exportBasket = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.cssSelector("[action=\"basket\"]")));
			exportBasket.click();
			
			driver.switchTo().defaultContent();
			new WebDriverWait(driver,defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(3));
			
			// Select export options
			WebElement externalID = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("EXTERNAL_ID")));
			externalID.click();
//			WebElement articleN = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("N")));
//			articleN.click();
//			WebElement pubID = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("ARTICLE")));
//			pubID.click();
			WebElement introducer = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("INTRODUCER")));
			introducer.click();
			WebElement comments = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("COMMENTS")));
			comments.click();
			WebElement selectUnits = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("unit-"+propertyNumber)));
			selectUnits.click();
			selectUnits.sendKeys(desiredUnits+Keys.ENTER);
			
			// Go to download page
			WebElement getXLS = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.cssSelector("[format=\"xls\"]")));
			getXLS.click();
			
			// Wait for basket to process and download button to appear
			WebElement downloadButton = new WebDriverWait(driver,20*defaultWait).until(ExpectedConditions.elementToBeClickable(By.className("fancy-button")));
			
			// Add the basket URL to a TXT file
			String basketURL = downloadButton.getAttribute("href");
			String basketURLPath = "Data\\Experimental\\OChem\\excel files\\OChem_BasketURLs.txt";
			FileWriter fw = new FileWriter(basketURLPath,true);
			fw.write(propertyName+": "+basketURL+"\n");
			fw.close();

			// Ten minutes to manually complete download if it gets stuck
			Thread.sleep(600000);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			driver.quit();
		}
	}
	
	public static void main(String[] args) {
		scrapeOChem(ExperimentalConstants.strWaterSolubility,302,0,"C:\\Users\\GSincl01\\Documents\\chromedriver.exe");
	}
	
}
