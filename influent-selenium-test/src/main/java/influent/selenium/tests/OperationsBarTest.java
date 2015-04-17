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

import influent.selenium.util.SeleniumUtils;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OperationsBarTest extends BrowserParameterizedTest {

	public OperationsBarTest(BROWSER browser) { super(browser); };
			
	@Test
	public void accountsToTransactionsTest() {
		
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_ACCOUNTS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("daniel");		
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
		WebElement switchToTransactionsButton = null;
		WebElement switchToFlowButton = null;
		WebElement switchToAccountsButton = null;
		//Get the operations bar buttons, but also make sure they are in the correct order
		for (WebElement e : switchViewButtons) {
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
				if (switchToTransactionsButton == null || switchToAccountsButton == null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			} else if (switchTo.equals(SeleniumUtils.TRANSACTIONS_NAME)) {
				switchToTransactionsButton = e;
				if (switchToFlowButton != null || switchToAccountsButton == null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			} else if (switchTo.equals("")) {
				switchToAccountsButton = e;
				if (switchToFlowButton != null && switchToTransactionsButton != null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			}
		}
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		if (switchToTransactionsButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to transactions view in infOperationsBar");
		}
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementsToSelect = 2;
		int elementsSelected = 0;
		for (WebElement e : dataIdElements) {
			e.click();
			elementsSelected++;
			if (elementsToSelect == elementsSelected) {
				break;
			}
		}
		switchToTransactionsButton.click();
		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".transactionsSearchResult")));
		if (firstTransactionResult == null) {
			throw new AssertionError("Couldn't find any transaction results.   Either timed out or search returned no transactions!");
		}
	}
	
	@Test
	public void accountsToFlowTest() {
		
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_ACCOUNTS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("daniel");		
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
		WebElement switchToTransactionsButton = null;
		WebElement switchToFlowButton = null;
		for (WebElement e : switchViewButtons) {
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
			} else if (switchTo.equals(SeleniumUtils.TRANSACTIONS_NAME)) {
				switchToTransactionsButton = e;
			}
		}
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		if (switchToTransactionsButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to transactions view in infOperationsBar");
		}
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementsToSelect = 2;
		int elementsSelected = 0;
		for (WebElement e : dataIdElements) {
			e.click();
			elementsSelected++;
			if (elementsToSelect == elementsSelected) {
				break;
			}
		}
		switchToFlowButton.click();
		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".insideBaseballCard")));
		if (firstTransactionResult == null) {
			throw new AssertionError("Couldn't find any transaction results.   Either timed out or search returned no transactions!");
		}
	}
	
	@Test
	public void transactionsToAccountsTest() {
		
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_TRANSACTIONS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("from:a.loan.b311953 to:a.loan.b311953 datatype:loan");		
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
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementsToSelect = 2;
		int elementsSelected = 0;
		for (WebElement e : dataIdElements) {
			e.click();
			elementsSelected++;
			if (elementsToSelect == elementsSelected) {
				break;
			}
		}
		
		List<WebElement> switchViewButtons = driver.findElements(By.className("btn-operationsBar"));		
		
		WebElement switchToAccountsButton = null;
		WebElement switchToFlowButton = null;
		WebElement switchToTransactionsButton = null;
		for (WebElement e : switchViewButtons) {
			if (!e.isDisplayed()) {
				continue;
			}
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
				if (switchToTransactionsButton == null || switchToAccountsButton == null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			} else if (switchTo.equals(SeleniumUtils.ACCOUNTS_NAME)) {
				switchToAccountsButton = e;
				if (switchToFlowButton != null && switchToTransactionsButton != null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			} else if (switchTo.equals("")) {
				switchToTransactionsButton = e;
				if (switchToFlowButton != null || switchToAccountsButton == null) {
					throw new AssertionError("Operations bar buttons are not in the correct order");
				}
			}
		}
		
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		if (switchToAccountsButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to accounts view in infOperationsBar");
		}	
		
		switchToAccountsButton.click();
		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".accountsSearchResult")));
		if (firstTransactionResult == null) {
			throw new AssertionError("Couldn't find any transaction results.   Either timed out or search returned no transactions!");
		}
	}
	
	@Test
	public void transacitonsToFlowTest() {
		
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_TRANSACTIONS);
		
		List<WebElement> elements = driver.findElements(By.id("influent-view-toolbar-search-input"));
		
		WebElement element = SeleniumUtils.getDisplayedElement(elements);
		
		if (element == null) {
			throw new AssertionError("Couldn't find: influent-view-toolbar-search-input element");			
		}
				
		element.sendKeys("from:a.loan.b311953 to:a.loan.b311953 datatype:loan");		
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
		
		// Select first two search results with a data id
		List<WebElement> dataIdElements = driver.findElements(By.cssSelector(".selectSingleResult input"));
		int elementsToSelect = 2;
		int elementsSelected = 0;
		for (WebElement e : dataIdElements) {
			e.click();
			elementsSelected++;
			if (elementsToSelect == elementsSelected) {
				break;
			}
		}
		
		List<WebElement> switchViewButtons = driver.findElements(By.className("btn-operationsBar"));
		WebElement switchToAccountsButton = null;
		WebElement switchToFlowButton = null;
		for (WebElement e : switchViewButtons) {
			if (!e.isDisplayed()) {
				continue;
			}
			String switchTo = e.getAttribute("switchesto");
			if (switchTo.equals(SeleniumUtils.FLOW_NAME)) {
				switchToFlowButton = e;
			} else if (switchTo.equals(SeleniumUtils.ACCOUNTS_NAME)) {
				switchToAccountsButton = e;
			}
		}
		if (switchToFlowButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to flow view in infOperationsBar");
		}
		if (switchToAccountsButton == null) {
			throw new AssertionError("Couldn't find: Button to switch to accounts view in infOperationsBar");
		}		
		
		switchToFlowButton.click();

		WebElement firstTransactionResult = (new WebDriverWait(driver, 120)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".insideBaseballCard")));
		if (firstTransactionResult == null) {
			throw new AssertionError("Couldn't find any transaction results.   Either timed out or search returned no transactions!");
		}
	}


}
