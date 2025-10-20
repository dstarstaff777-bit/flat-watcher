package util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SeleniumFetcher {
    public static String fetchPageSource(String url) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");


        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("span[itemprop='price']")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-marker='item']"))
            ));
            Thread.sleep(1000);
            return driver.getPageSource();
        } catch (TimeoutException e) {
            System.out.println("Не удалось дождаться загрузки цены для " + url);
            return "";
        } catch (Exception e) {
            System.out.println("Ошибка Selenium: " + e.getMessage());
            return "";
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
