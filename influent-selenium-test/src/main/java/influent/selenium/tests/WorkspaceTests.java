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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

public class WorkspaceTests extends BrowserParameterizedTest {

    public WorkspaceTests(BROWSER browser) { super(browser); }

    /**
     * Tests expected results for the route.
     * @param expectedTitle
     * @param expectedRoute
     *   The route should be anything after the '#'
     */
    private void assertRoute(String expectedTitle, String expectedRoute) {
    	assertEquals(expectedTitle, driver.getTitle());
    	
    	String curPath = driver.getCurrentUrl();
    	Assert.assertTrue("Current path doesn't start with the startURL: " + startURL, curPath.startsWith(startURL));
    	
    	//only get the remaining part of the path
    	curPath = curPath.substring(startURL.length());
    	int routeLoc = curPath.indexOf('#');
    	if (routeLoc >= 0) {
    		//the path has a route
    		Assert.assertTrue("The route seems to have more of a path before the '#': " + curPath, routeLoc <= 1);
    		curPath = curPath.substring(routeLoc + 1);
    		Assert.assertEquals("The expected route doesn't match.", expectedRoute, curPath);
    	}
    	else {
    		//the path doesn't have a route, so it should be at the root
    		Assert.assertTrue("The current path should be at the root but instead we're at: " + curPath, curPath.length() == 0 || curPath.equals("/"));
    	}
    }
    
    /**
     * Makes sure that we navigate to a route and use the prop starting url.
     * @param route
     */
    private void navigateToRoute(String route) {
    	String path = startURL;
    	if (!path.endsWith("/")) {
    		path = path + "/";
    	}
    	path = path + "#" + route;
    	driver.navigate().to(path);
    }
    
	@Test
	public void urlRoutingTest() {
		
		// Select the Accounts tab
		// check that the page title is correct
		// check that the page URL is correct
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_ACCOUNTS);
		assertRoute("Kiva - Accounts", "/accounts");
		
		// select the Transactions tab
		// check that the page title is correct
		// check that the page URL is correct
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_TRANSACTIONS);
		assertRoute("Kiva - Transactions", "/transactions");
		
		// select the Flow tab
		// check that the page title is correct
		// check that the page URL is correct
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_FLOW);
		assertRoute("Kiva - Flow", "/flow");
		
		// select the Summary tab
		// check that the page title is correct
		// check that the page URL is correct
		SeleniumUtils.navigateToTab(driver, SeleniumUtils.TAB_SUMMARY);
		assertRoute("Kiva - Summary", "/summary");
		
		// press the back button to get the flow view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().back();
		assertRoute("Kiva - Flow", "/flow");
		
		// press the back button to get the transactions view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().back();
		assertRoute("Kiva - Transactions", "/transactions");
		
		// press the back button to get the accounts view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().back();
		assertRoute("Kiva - Accounts", "/accounts");
				
		// press the back button to get the summary view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().back();
		assertRoute("Kiva - Summary", "");
		
		// press the forward button to get the accounts view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().forward();
		assertRoute("Kiva - Accounts", "/accounts");
		
		// press the forward button to get the transactions view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().forward();	
		assertRoute("Kiva - Transactions", "/transactions");
		
		// press the forward button to get the flow view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().forward();
		assertRoute("Kiva - Flow", "/flow");
		
		// press the forward button to get the summary view
		// check that the page title is correct
		// check that the page URL is correct
		driver.navigate().forward();
		assertRoute("Kiva - Summary", "/summary");
		
		// go to the accounts route directly and make sure it changes the tabs/title 
		navigateToRoute("/accounts");
		assertEquals(true, SeleniumUtils.hasClass(driver.findElement(By.id("infWorkspaceTabContentAccounts")), "active"));
		assertEquals(false, SeleniumUtils.hasClass(driver.findElement(By.id("infWorkspaceTabContentSummary")), "active"));
		assertEquals("Kiva - Accounts", driver.getTitle());
		
		// go to a bogus route and make sure the page stays on the same tab 
		navigateToRoute("/banana");
		assertEquals(true, SeleniumUtils.hasClass(driver.findElement(By.id("infWorkspaceTabContentAccounts")), "active"));
		assertEquals("Kiva - Accounts", driver.getTitle());
		
		//check that the root route goes to the summary page
		navigateToRoute("/");
		assertEquals(true, SeleniumUtils.hasClass(driver.findElement(By.id("infWorkspaceTabContentSummary")), "active"));
		assertRoute("Kiva - Summary", "/");

		//check that the empty route goes to the summary page
		navigateToRoute("/accounts");	//go somewhere else first
		navigateToRoute("");			//now go back to the empty route
		assertEquals(true, SeleniumUtils.hasClass(driver.findElement(By.id("infWorkspaceTabContentSummary")), "active"));
		assertRoute("Kiva - Summary", "");
	}
}
