import de.siegmar.fastcsv.writer.CsvWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppStart {
    public static void main(String[] args) {
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
                Collection<String[]> lineNet = generateLineNet(driver);
                writeAsCsv(csvPath, lineNet);
            } catch (Exception e) {
                e.printStackTrace();
                driver.close();
            }
        }
    }

    private static Collection<String[]> generateLineNet(WebDriver driver) {
        //前往地铁站官网
        driver.get("http://www.gzmtr.com/");

        //获取地铁站所有路线
        List<WebElement> metroLines = driver.findElements(By.cssSelector("#lineAndStation > div"));

        //创建lineNet集合，所有结果目标数据保存到此集合中
        Collection<String[]> lineNet = new LinkedList<>();

        //先添加csv header
        lineNet.add(new String[]{"RouteName", "orderNum", "stationName"});

        //添加线路名、站序号码、站名
        for (WebElement metroLine : metroLines) {
            String lineName = metroLine.findElement(By.cssSelector(".no span")).getAttribute("innerHTML");
            List<WebElement> stationElements = metroLine.findElements(By.cssSelector(".stage_background a"));
            for(int i = 0; i < stationElements.size(); i ++) {
                lineNet.add(new String[] {lineName, Integer.toString(i), stationElements.get(i).getAttribute("innerHTML")});
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
