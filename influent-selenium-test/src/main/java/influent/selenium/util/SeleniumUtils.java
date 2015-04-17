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

package influent.selenium.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class SeleniumUtils {
	public static final String ACCOUNTS_NAME = "Accounts";
	public static final String TRANSACTIONS_NAME = "Transactions";
	public static final String FLOW_NAME = "Flow";
	public static final String SUMMARY_NAME = "Summary";
	

    public static final String TAB_ID_PREFIX = "infWorkspaceTab";
    public static final String TAB_ACCOUNTS = TAB_ID_PREFIX + ACCOUNTS_NAME;
    public static final String TAB_TRANSACTIONS = TAB_ID_PREFIX + TRANSACTIONS_NAME;
    public static final String TAB_FLOW = TAB_ID_PREFIX + FLOW_NAME;
    public static final String TAB_SUMMARY = TAB_ID_PREFIX + SUMMARY_NAME;

	static public void loginToInfluent(WebDriver driver, String username, String password) {
		try {
			driver.findElement(By.name("username")).sendKeys(username);
			driver.findElement(By.name("password")).sendKeys(password);
			driver.findElement(By.name("submit")).click();
		} catch (NoSuchElementException e) {
			// Do nothing because there may not be a login screen
		}
	}
	
	static public void navigateToTab(WebDriver driver, String id) {
		WebElement element = driver.findElement(By.id(id));
		element.click();
	}

	/**
	 * Checks if the {@link WebElement} has the class 'className' set on it.
	 * @return
	 * 	Returns true if the element has the specified class. False otherwise.
	 */
	static public boolean hasClass(WebElement el, String className) {
		String classes = el.getAttribute("class");
		if (classes != null) {
			return Lists.newArrayList(classes.split(" ")).contains(className);
		}
		return false;
	}
	
	/**
	 * Returns the first element that is being displayed from the list of given elements
	 * @param elements
	 * the list of given elements
	 * @return
	 * the first element that is being displayed from the list of given elements
	 */
	static public WebElement getDisplayedElement(List<WebElement> elements) {
		WebElement element = null;
		for (WebElement webElement : elements) {
			if (webElement.isDisplayed()) {
				element = webElement;
				break;
			}
		}
		return element;
	}
}
