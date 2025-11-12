package util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import parser.FetchResult;

import java.time.Duration;

public class SeleniumFetcher {
    public FetchResult fetchPageSource(String url) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        ;
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = null;

        try {
            driver = new ChromeDriver(options);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

            WebElement priceElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-marker='item-price']"))
            );

            String priceText = priceElement.getText().trim();
            System.out.println("💰 Цена найдена: " + priceText);

            String html = driver.getPageSource();

            return new FetchResult(html, priceText);

        } catch (TimeoutException e) {
            System.out.println("⏳ Не удалось дождаться цены: " + url);
            return new FetchResult("", null);

        } catch (Exception e) {
            System.out.println("❌ Ошибка Selenium: " + e.getMessage());
            return new FetchResult("", null);

        } finally {
            if (driver != null) driver.quit();
        }
    }
}