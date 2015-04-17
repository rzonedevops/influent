/*
 * Copyright (C) 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted(TM), formerly Oculus Info Inc.
 * http://uncharted.software/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package influent.selenium.tests;

import static org.junit.Assert.assertEquals;
import influent.selenium.util.SeleniumUtils;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FlowViewTests extends BrowserParameterizedTest {

	public FlowViewTests(BROWSER browser) { super(browser); };
			
	@Test
	public void initBehaviorTest() {
		
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_FLOW);
		
		List<WebElement> elements = driver.findElements(By.className("column"));
		
		for (WebElement elem : elements) {
			if (elem.isDisplayed()) {
				throw new AssertionError("Initial State is not a clear screen");
			}
		}
	}
	
	@Test
	public void columnLabelTest() {
SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_ACCOUNTS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("joe");		
		elements = driver.findElements(By.className("infGoSearch"));
		element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: infGoSearch element");			
		}
		
		element.click();
		
		element = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".infSearchResultCounts")));
		if (element == null) {
			throw new AssertionError("Couldn't find any search results");
		}
		
		element = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".infOperationsBar")));
		List<WebElement> switchViewButtons  = element.findElements(By.cssSelector("[switchesto]"));
		WebElement switchToFlowButton = null;
		
		for (WebElement e : switchViewButtons) {
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
			}
		}
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementCount = 0;
		for (WebElement e : dataIdElements) {
			if (elementCount == 1) {
				e.click();
				break;
			}
			elementCount++;
		}
		switchToFlowButton.click();
		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".insideBaseballCard")));
		
		//Test that labels are visible in intial state
		elements = driver.findElements(By.className("columnHint"));
		assertEquals(2, elements.size());
		
		//assertEquals("FROM ACCOUNTS", elements.get(0).getText());
		//assertEquals("TO ACCOUNTS", elements.get(1).getText());
		
		//Test column labels are removed after cards are added to column
		elements = driver.findElements(By.className("baseballcardContainer"));
		element = SeleniumUtils.getDisplayedElement(elements);
		//Click card so add to file buttons appear
		element. click();
		
		//Wait a few seconds for page to load. This is needed despite waiting for presence of element
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}
				
		element = element.findElement(By.cssSelector("[title='add to file']"));
		element.click();
		
		//Wait a few seconds for page to load. This is needed despite waiting for presence of element
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}
		
		//find card again
		element = driver.findElement(By.className("file"));
		element = element.findElement(By.className("baseballcardContainer"));
		
		//Click card again so branch right button appears
		element. click();
		
		//Wait a few seconds for page to load. This is needed despite waiting for presence of element
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}
		
		element = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[title='branch right to outflowing destinations']")));
		
		element.click();
		
		//Wait a few seconds for page to load. This is needed despite waiting for presence of element
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}
		
		elements = driver.findElements(By.className("columnHint"));
		assertEquals(0, elements.size());
	}
	
	@Test
	public void displayClusterSummary() {
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_ACCOUNTS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("visionfund");		
		elements = driver.findElements(By.className("infGoSearch"));
		element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: infGoSearch element");			
		}
		
		element.click();
		
		element = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".infSearchResultCounts")));
		if (element == null) {
			throw new AssertionError("Couldn't find any search results");
		}
		
		element = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".infOperationsBar")));
		List<WebElement> switchViewButtons  = element.findElements(By.cssSelector("[switchesto]"));
		WebElement switchToFlowButton = null;
		
		for (WebElement e : switchViewButtons) {
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
			}
		}
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementCount = 0;
		for (WebElement e : dataIdElements) {
			if (elementCount == 1) {
				e.click();
				break;
			}
			elementCount++;
		}
		switchToFlowButton.click();
		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".insideBaseballCard")));
		if (firstTransactionResult == null) {
			throw new AssertionError("Couldn't find any transaction results.   Either timed out or search returned no transactions!");
		}
	}
}
