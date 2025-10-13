package util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.IIOException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class SeleniumCookieSaver {
    public static void saveCookies(String url, String filePath) {
         WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(10000);
            Set<Cookie> cookies = driver.manage().getCookies();
            try (FileWriter writer = new FileWriter(filePath)) {
                for (Cookie cookie : cookies) {
                    writer.write(cookie.getName() + "=" + cookie.getValue() + ";\n");
                }
                System.out.println("Cookies сохранены в " + filePath);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
