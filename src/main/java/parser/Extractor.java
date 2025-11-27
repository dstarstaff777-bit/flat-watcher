package parser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class Extractor {
    public static String safe(WebElement item, String css) {
        try { return item.findElement(By.cssSelector(css)).getText(); }
        catch (Exception e) { return ""; }
    }
}