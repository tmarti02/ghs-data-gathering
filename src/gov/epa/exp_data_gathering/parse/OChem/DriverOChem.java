package gov.epa.exp_data_gathering.parse.OChem;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.CDL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.*;

import gov.epa.api.ExperimentalConstants;


/**
 * 	https://cosmocode.io/how-to-connect-selenium-to-an-existing-browser-that-was-opened-manually/
 
 	Create bat file (Runs chrome with special options so java can find it and creates a special profile with a bunch of files):
 		
 	"C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --user-data-dir="C:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\0 model_management\ghs-data-gathering\data\experimental\OChem\selenium\ChromeProfile" 
    
 */
public class DriverOChem {
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/**
	 * Bulk selects and downloads records from OChem automatically
	 * 
	 * @param propertyName     Use ExperimentalConstants
	 * @param startIndex       What page to start download from
	 * @param endIndex         How many pages (100 records each) to download; 0 to
	 *                         automatically download all
	 * @param chromeDriverPath Path to your chromedriver.exe file
	 */
	public void scrapeOChem(String propertyName, int startIndex, int endIndex, String chromeDriverPath) {
		
		boolean doLogin = false;//whether or not we need to to log into ochem via Java
		boolean useFixedChrome=true;//uses the instance started by the .bat file
		
		String []values=getPropertyNumberAndUnits(propertyName);
		int propertyNumber=Integer.parseInt(values[0]);
		String desiredUnits=values[1];

		// Open new driver and connect to the OChem website
		long defaultWaitTime = 30; // in seconds

		Duration defaultWait = Duration.ofSeconds(defaultWaitTime);
		Duration defaultWaitLonger = Duration.ofSeconds(defaultWaitTime * 20);

		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		WebDriver driver=null;
		
		if(useFixedChrome) {
			ChromeOptions options = new ChromeOptions();
			options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();	
		}
		
		driver.get("https://ochem.eu/");
		driver.manage().window().maximize();

		try {

			if (doLogin) login(defaultWait, driver);

			endIndex = initPages(startIndex, endIndex, propertyNumber, defaultWait, driver);
			getPages(startIndex, endIndex, defaultWait, driver);
			downloadSpreadsheet(propertyNumber, desiredUnits, defaultWait, driver);
			saveSpreadsheetURL(propertyName, defaultWaitLonger, driver);

			// Ten minutes to manually complete download if it gets stuck
			Thread.sleep(600000);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if(!useFixedChrome)	driver.quit();
		}
	}

	private  int initPages(int startIndex, int endIndex, int propertyNumber, Duration defaultWait,
			WebDriver driver) throws InterruptedException {
		String url = "https://ochem.eu/epbrowser/show.do?property=" + propertyNumber;
		driver.navigate().to(url);
		Thread.sleep(6000);
		new WebDriverWait(driver, defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));

		// Display 100 records per page instead of default (5)
		WebElement displayCount = new WebDriverWait(driver, defaultWait)
				.until(ExpectedConditions.elementToBeClickable(By.name("pagesize")));
		Thread.sleep(1000);
		displayCount.click();
		Thread.sleep(1000);
		displayCount.sendKeys("100" + Keys.ENTER);

		// Wait for records to load
		Thread.sleep(1000);

		new WebDriverWait(driver, defaultWait)
				.until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));

		// Scrape end index if not provided
		if (endIndex == 0) {
			WebElement pager = driver.findElement(By.id("pager"));
			List<WebElement> arrows = pager.findElements(By.tagName("a"));
			int autoEndIndex = Integer.parseInt(arrows.get(1).getAttribute("page"));
			endIndex = autoEndIndex;
		}

		// Go to start page if not 1
		if (startIndex > 1) {
			WebElement pageInput = new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.elementToBeClickable(By.id("pageInput")));
			pageInput.click();
			pageInput.clear();
			String nextPage = String.valueOf(startIndex);
			pageInput.sendKeys(nextPage + Keys.ENTER);

			// Wait for records to load
			new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
		}
		return endIndex;
	}

	private void saveSpreadsheetURL(String propertyName, Duration defaultWaitLonger, WebDriver driver)
			throws IOException {
		// Wait for basket to process and download button to appear
		WebElement downloadButton = new WebDriverWait(driver, defaultWaitLonger)
				.until(ExpectedConditions.elementToBeClickable(By.className("fancy-button")));

		// Add the basket URL to a TXT file
		String basketURL = downloadButton.getAttribute("href");
		String basketURLPath = "Data\\Experimental\\"+RecordOChem.sourceName+"\\excel files\\OChem_BasketURLs.txt";
		FileWriter fw = new FileWriter(basketURLPath, true);
		fw.write(propertyName + ": " + basketURL + "\n");
		fw.close();
	}

	private void downloadSpreadsheet(int propertyNumber, String desiredUnits, Duration defaultWait,
			WebDriver driver) {
		// Go to basket static page
		driver.navigate().to("https://ochem.eu/basket/show.do");

		try {
			new WebDriverWait(driver, defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
		} catch (TimeoutException ex) {
			// May encounter an error page when breaking out of pageLoop above
			// Just refresh and it should go through with basket intact
			driver.navigate().refresh();
			new WebDriverWait(driver, defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));
		}

		// Open record download menu and click export
		WebElement recordMenu = new WebDriverWait(driver, defaultWait)
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[src=\"img/icons/xls.gif\"]")));
		recordMenu.click();
		WebElement exportBasket = new WebDriverWait(driver, defaultWait)
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[action=\"basket\"]")));
		exportBasket.click();

		driver.switchTo().defaultContent();
		new WebDriverWait(driver, defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(3));

		List<String>options=Arrays.asList("EXTERNAL_ID","INTRODUCER","COMMENTS","INCHI_KEY","RECORDID");
		
		//Export options
		for (String option:options) {
			WebElement checkBox = new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.elementToBeClickable(By.name(option)));
			checkBox.click();
		}
		
		if(desiredUnits!=null) {//TMM added this if statement because there wont be a drop down box to click on for binary endpoints 
			WebElement selectUnits = new WebDriverWait(driver,defaultWait).until(ExpectedConditions.elementToBeClickable(By.name("unit-"+propertyNumber))); 
			selectUnits.click();
			selectUnits.sendKeys(desiredUnits+Keys.ENTER); 
		}

		// Go to download page
		WebElement getXLS = new WebDriverWait(driver, defaultWait)
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[format=\"xls\"]")));
		getXLS.click();
	}

	private void login(Duration defaultWait, WebDriver driver) throws InterruptedException {
		try {
			// Click login button
			WebElement login = new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.elementToBeClickable(By.linkText("log in")));
			login.click();

			new WebDriverWait(driver, defaultWait).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(2));

			// Log in as guest
			WebElement button1 = new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.elementToBeClickable(By.className("button-link")));
			button1.click();

			// Accept terms
			WebElement button2 = new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.elementToBeClickable(By.className("button-link")));
			button2.click();
		} catch (Exception ex) {
			System.out.println("Already logged in!");
		}

		Thread.sleep(6000);
	}

	private void getPages(int startIndex, int endIndex, Duration defaultWait, WebDriver driver)
			throws InterruptedException {
		
		pageLoop: for (int i = startIndex; i <= endIndex; i++) {
			// Select all records
			WebElement selectAll = new WebDriverWait(driver, defaultWait).until(ExpectedConditions
					.elementToBeClickable(By.cssSelector("[title=\"Select records on currently visible page\"]")));
			Thread.sleep(1000);
			selectAll.click();

			// Wait for records to reload after selecting
			new WebDriverWait(driver, defaultWait)
					.until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));

			// Double-check popup is gone before proceeding - order of loading gets messed
			// up sometimes
			new WebDriverWait(driver, defaultWait).until(
					ExpectedConditions.attributeContains(By.id("waitingDialog_mask"), "style", "display: none"));

			// Navigate to next page by typing page number into box (Charlie says this is
			// most reliable)
			if (i < endIndex) {
				WebElement pageInput = new WebDriverWait(driver, defaultWait)
						.until(ExpectedConditions.elementToBeClickable(By.id("pageInput")));
				pageInput.click();
				pageInput.clear();
				String nextPage = String.valueOf(i + 1);
				pageInput.sendKeys(nextPage + Keys.ENTER);

				// Wait for records to load
				try {
					new WebDriverWait(driver, defaultWait)
							.until(ExpectedConditions.presenceOfElementLocated(By.className("browser-item")));
				} catch (TimeoutException ex) {
					System.out.println("Timed out. Downloading pages " + startIndex + "-" + i + ".");
					break pageLoop;
				}
			}
		}
	}

	public JsonArray getReferences(List<String> ids, String chromeDriverPath) {

		boolean dologin = false;
		boolean useFixedChrome=true;
		
		// Open new driver and connect to the OChem website
		long defaultWaitTime = 30; // in seconds

		Duration defaultWait = Duration.ofSeconds(defaultWaitTime);

		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		
		WebDriver driver = null;

		if(useFixedChrome) {
			ChromeOptions options = new ChromeOptions();
			options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();	
		}

		driver.get("https://ochem.eu/");
		driver.manage().window().maximize();

		try {

			if (dologin) login(defaultWait, driver);

			JsonArray jaRefs = new JsonArray();

			for (int i = 0; i < ids.size(); i++) {
				String id = ids.get(i);
				
				System.out.println((i+1)+"\tA"+id);
				
				String url = "https://ochem.eu/article/profile.do?id=" + id + "&render-mode=popup";
				driver.navigate().to(url);
//				System.out.println("********************\nid="+id+"\n"+driver.getPageSource());
				
				JsonObject joRef = parseForReference(driver.getPageSource());
				jaRefs.add(joRef);
				
				System.out.println(gson.toJson(joRef));

				Thread.sleep(200);
				
//				if(i==10) break;
			}

//			System.out.println(gson.toJson(jaRefs));
			return jaRefs;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
//			driver.quit();
		}
		return null;
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public JsonArray parseRecordsFromExcel(String filePath) {

		JsonArray records = new JsonArray();

		try {

			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheetAt(0);

			Row row0 = sheet.getRow(0);

			List<String> headers = new ArrayList<>();

			for (int j = 0; j < row0.getLastCellNum(); j++) {
				Cell cell = row0.getCell(j);
				String headerj = cell.getStringCellValue();
				headers.add(headerj);
			}

			FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

			int numRows = sheet.getLastRowNum();
			for (int i = 1; i <= numRows; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				JsonObject jo = new JsonObject();

				for (int j = 0; j < row.getLastCellNum(); j++) {
					Cell cell = row.getCell(j);
					
					if(cell==null) continue;

					Object content = null;

					CellType type = cell.getCellType();

					if (type == CellType.STRING) {
						content = cell.getStringCellValue();
					} else if (type == CellType.NUMERIC) {
						content = cell.getNumericCellValue() + "";
					} else if (type == CellType.BOOLEAN) {
						content = cell.getBooleanCellValue() + "";
					} else if (type == CellType.BLANK) {
						content = "";
					} else if (type == CellType.FORMULA) {// 2024-01-23 (TMM)
						type = evaluator.evaluateFormulaCell(cell);
						if (type == CellType.STRING) {
							content = cell.getStringCellValue();
						} else if (type == CellType.NUMERIC) {
							content = cell.getNumericCellValue() + "";
						} else if (type == CellType.BOOLEAN) {
							content = cell.getBooleanCellValue() + "";
						} else if (type == CellType.BLANK) {
							content = "";
						}
					}
//					System.out.println(headers.get(j)+"\t"+content);

					jo.addProperty(headers.get(j), (String) content);
					records.add(jo);
				}
			}
//			System.out.println(gson.toJson(records));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;

	}

	private JsonObject parseForReference(String strHTML) {
		Document doc = Jsoup.parse(strHTML);
		JsonObject jo = getReference(doc);
//		System.out.println(gson.toJson(jo));
		return jo;
	}


	private JsonObject getReference(Document doc) {
		Elements tables = doc.select("table");
//			System.out.println(tables.size());

		Element table = tables.get(1);
		Elements trs = table.select("tr");

		JsonObject jo = new JsonObject();

		for (Element tr : trs) {
			Elements tds = tr.select("td");
			String name = tds.get(0).text().replace(":", "");
			String value = tds.get(1).text();
			
			if(name.equals("Title")) {
				Elements links=tds.select("a[href]");
				if(links.size()>0) {
					jo.addProperty("URL", links.get(0).attr("abs:href"));
				}
			}
			
			jo.addProperty(name, value);
//				System.out.println(name);
		}
		return jo;
	}

	private List<String> getArticleIdsFromOchemFile(String filepath) {

		
		JsonArray ja=null;
		
		if(filepath.contains("xls")) {
			ja = parseRecordsFromExcel(filepath);		
		} else if (filepath.contains("csv")) {
			ja = parseRecordsFromCSV(filepath);
		} else {
			return null;
		}
	

		List<String> articleIds = new ArrayList<>();

		for (JsonElement je : ja) {
			JsonObject jo = je.getAsJsonObject();
			String articleId = jo.get("ARTICLEID").getAsString().replace("A", "");
			if (!articleIds.contains(articleId))
				articleIds.add(articleId);
		}
		Collections.sort(articleIds);
		
//		for (String articleId : articleIds) {
//			System.out.println(articleId);
//		}
		
		return articleIds;
	}

	public static JsonArray parseRecordsFromCSV(String filepath) {

		try {
			
			InputStream inputStream = new FileInputStream(filepath);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			JsonArray ja = gson.fromJson(json, JsonArray.class);
			return ja;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private List<String>getArticleIdsFromFolder(String folderPath) {
		
		File folder=new File(folderPath);
		
		List<String>articleIds_all=new ArrayList<>();
		
		for (File file:folder.listFiles()) {
			System.out.println(file.getAbsolutePath());
			
			List<String>articleIds=getArticleIdsFromOchemFile(file.getAbsolutePath());
			
			if(articleIds==null) continue;
			
			for(String articleId:articleIds) 
				if(!articleIds_all.contains(articleId)) articleIds_all.add(articleId);
			
		}
		
		for (int i=0;i<articleIds_all.size();i++) {
			System.out.println(i+"\t"+articleIds_all.get(i));
		}
		
		return articleIds_all;
		
	}
	private String [] getPropertyNumberAndUnits(String propertyName) {
		
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
		case ExperimentalConstants.strLogKOW:
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
//			desiredUnits = "g/L";
			desiredUnits = "log(mol/L)";
			break;
		case ExperimentalConstants.strDMSOSolubility:
			propertyNumber = 531;
			desiredUnits = "Soluble";
			break;
		}
		
		String [] results= {propertyNumber+"",desiredUnits};
		return results;
	}
	public static void main(String[] args) {
		DriverOChem d=new DriverOChem();
		
		String chromeDriverPath = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\driver\\chromedriver.exe";
		System.out.println(new File(chromeDriverPath).exists());
		
//		d.scrapeOChem(ExperimentalConstants.strDMSOSolubility,1,2,chromeDriverPath); 
//		d.scrapeOChem(ExperimentalConstants.strMeltingPoint, 1, 350, chromeDriverPath);//
//		d.scrapeOChem(ExperimentalConstants.strDensity, 351, 675, chromeDriverPath);//675
		
		
//		if(true)return;
		
		String folderPath="data\\experimental\\OChem_2024_04_03\\excel files";
		String filePathArticleJson=folderPath+File.separator+"article_lookup.json";
		List<String>articleIds=d.getArticleIdsFromFolder(folderPath);
//		List<String> articleIds = Arrays.asList("68601");
		JsonArray ja=d.getReferences(articleIds, chromeDriverPath);		
		d.saveReferencesArray(filePathArticleJson, ja);
		
//		JsonArray ja=d.loadReferencesArray(filePathArticleJson);		
		List<ReferenceOChem>refs=d.getReferenceList(ja);
//		System.out.println(gson.toJson(refs));
//		System.out.println(ja.size());
		
	}

	class ReferenceOChem {
		String URL;
		String Title;
		String Authors;
		String JournalReference;
		String ArticleIdentifer;
		public String PubMedReference;
	}
	
	
	private void saveReferencesArray(String filePath, JsonArray ja) {
		try {
			FileWriter fw=new FileWriter(filePath);
			fw.write(gson.toJson(ja));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	JsonArray loadReferencesArray(String filePath) {
		try {
			JsonArray ja=gson.fromJson(new FileReader(filePath), JsonArray.class);
			return ja;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	List<ReferenceOChem>getReferenceList(JsonArray ja) {
		List<ReferenceOChem>refs=new ArrayList<>();
		
		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			
//			System.out.println(gson.toJson(jo));
			
			ReferenceOChem ref=new ReferenceOChem();
			ref.ArticleIdentifer=jo.get("Internal identifier").getAsString();
			ref.Title=jo.get("Title").getAsString();
			ref.Authors=jo.get("Authors").getAsString();
			ref.JournalReference=jo.get("Journal reference").getAsString();
			refs.add(ref);	
			
			if(jo.get("PubMed reference")!=null)
				ref.PubMedReference=jo.get("PubMed reference").getAsString();;
				
			if(jo.get("URL")!=null)
				ref.URL=jo.get("URL").getAsString();
			
		}
		
		return refs;
	}

}
