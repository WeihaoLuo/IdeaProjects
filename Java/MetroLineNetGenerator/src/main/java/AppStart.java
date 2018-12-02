import de.siegmar.fastcsv.writer.CsvWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppStart {
    public static void main(String[] args) throws IOException {
        if(args.length == 2) {
            String chromeDriverPath = args[0];
            String csvPath = args[1];

            //配置selenium driver
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            final ChromeOptions  chromeOptions= new ChromeOptions();
            chromeOptions.setHeadless(true);
            WebDriver driver = new ChromeDriver(chromeOptions);

            //爬取数据，写出数据到指定csv
            try {
                Collection<String[]> lineNet = getLineNetData(driver);
                writeAsCsv(csvPath, lineNet);
                driver.close();
            } catch (Exception e) {
                e.printStackTrace();
                driver.close();
            }
        }
    }

    private static Collection<String[]> getLineNetData(WebDriver driver) {

        //创建lineNet集合，所有结果目标数据保存到此集合中
        Collection<String[]> lineNet = new LinkedList<>();

        boolean noResult = true;
        while (noResult) {
            //前往地铁站官网
            driver.get("http://www.gzmtr.com/");

            //获取地铁站所有路线
            List<WebElement> metroLines = driver.findElements(By.cssSelector("#lineAndStation > div"));

            //先添加csv header
            if(metroLines.size() > 0) {
                lineNet.add(new String[]{"RouteName", "orderNum", "stationName"});

                //添加线路名、站序号码、站名
                for (WebElement metroLine : metroLines) {
                    String lineName = metroLine.findElement(By.cssSelector(".no span")).getAttribute("innerHTML");
                    List<WebElement> stationElements = metroLine.findElements(By.cssSelector(".stage_background a"));
                    for (int i = 0; i < stationElements.size(); i++) {
                        lineNet.add(new String[]{lineName, Integer.toString(i), stationElements.get(i).getAttribute("innerHTML")});
                    }
                }

                System.out.println("Elements are found. Done.");
                noResult = false;
            } else {
                System.err.println("Nothing are found. Retrying ...");
            }
        }
        return lineNet;
    }

    private static void writeAsCsv(String csvOutputPath, Collection<String[]> data) throws IOException {
        File file = new File(csvOutputPath);
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.write(file, StandardCharsets.UTF_8, data);
    }
}
