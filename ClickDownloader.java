package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

public class ClickDownloader {
    public static void main(String[] args) {
        // Set the path to your ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/chrome/chromedriver");

        // Create a new instance of the ChromeDriver
        WebDriver driver = new ChromeDriver();

        // Implicit wait to wait for elements to become available
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // Navigate to the website
        driver.get("https://www.indiavotes.com/lok-sabha/2009/andhra-pradesh/15/27");

        // Find the button element by its XPath, CSS selector, or other suitable locator
        WebElement downloadButton = driver.findElement(By.id("csvexport"));

        // Click the download button
        downloadButton.click();

        // Wait for some time to ensure the file is downloaded
        try {
            Thread.sleep(5000); // Adjust the time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close the browser
        driver.quit();
    }
}
