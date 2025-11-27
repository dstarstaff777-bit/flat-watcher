package parser;

import model.Flat;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class AvitoParser {


    private final String url;


    public AvitoParser(String url) {
        this.url = url;
    }


    public List<Flat> loadFlats() {
        List<Flat> result = new ArrayList<>();
        WebDriver driver = null;


        try {
            driver = SeleniumFactory.create();
            driver.get(url);


            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-marker='item']")));


            List<WebElement> items = driver.findElements(By.cssSelector("div[data-marker='item']"));


            for (WebElement item : items) {
                try {
                    WebElement a = item.findElement(By.cssSelector("a[href]"));
                    String link = "https://www.avito.ru" + a.getAttribute("href");
                    String id = link.substring(link.lastIndexOf('_') + 1);


                    String title = Extractor.safe(item, "h3");
                    String price = Extractor.safe(item, "span[data-marker='item-price']");
                    String rooms = "";
                    String area = "";
                    String metro = Extractor.safe(item, "span[class*='geo-root']");


                    result.add(new Flat(id, title, price, link, rooms, area, metro));
                } catch (Exception ignored) {}
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        finally { if (driver != null) driver.quit(); }


        return result;
    }
}