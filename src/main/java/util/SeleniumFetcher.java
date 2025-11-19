package util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
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
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = null;

        try {
            driver = new ChromeDriver(options);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // ❗ Ждём появления хотя бы одного блока объявления
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[data-marker='item']")
            ));
            //
            System.out.println("===== HTML START =====");
            System.out.println(driver.getPageSource());
            System.out.println("===== HTML END =====");
            System.out.println("✅ Страница загружена успешно");
            return new FetchResult(driver.getPageSource(), null);

        } catch (TimeoutException e) {
            System.out.println("⏳ Не удалось дождаться загрузки страницы: " + url);
            return new FetchResult("", null);

        } catch (Exception e) {
            System.out.println("❌ Ошибка Selenium: " + e.getMessage());
            return new FetchResult("", null);

        } finally {
            if (driver != null) driver.quit();
        }
    }
}