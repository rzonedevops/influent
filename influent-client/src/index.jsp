<!DOCTYPE HTML>
<!--
    Copyright (c) 2013 Oculus Info Inc.
    http://www.oculusinfo.com/

    Released under the MIT License.

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in
    the Software without restriction, including without limitation the rights to
    use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
    of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
-->
<%@ page
    language="java"
    pageEncoding="utf-8"
    contentType="text/html; charset=utf-8"
    import="java.util.Map"
    import="java.util.UUID"
   	session="false"
%>
<html>
    <head>
	    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta charset="UTF-8">
        <link rel="icon" href="img/favicon.ico" type="image/x-icon" />
        <link rel="stylesheet" type="text/css" href="theme/custom-theme/jquery-ui-1.8.13.custom.css"/>
        <link rel="stylesheet" type="text/css" href="theme/fancybox/jquery.fancybox.css"/>
        <link rel="stylesheet" type="text/css" href="theme/transactions/transactions.css">
        <link rel="stylesheet" type="text/css" href="theme/branding.css">
        <link rel="stylesheet" type="text/css" href="rest/parchment.css">
        <link rel="stylesheet" type="text/css" href="theme/app.css" />
        <!--link rel="stylesheet/less" type="text/css" href="theme/app.less" /><script src="scripts/lib/extern/less-1.3.3.min.js" type="text/javascript"></script-->
    </head>
    <body>
        <div id="settings" sessionid="" capture="false"></div>
        <div id="banner">
            <div id="banner-text"></div>
        </div>

        <div id="header">

            <div id = "main-logo"></div>

            <div id = "display">
                <button id = "export-button" class="nocapture">Workspace</button>
                <ul id = "export-options" class="nocapture">
                    <li><a id ="new-chart" href="#"><span class="ui-icon new-chart-icon"></span>Clear Workspace</a></li>
                    <li><a id ="export-capture" href="#"><span class="ui-icon export-capture-icon"></span>Export Image</a></li>
                    <li><a id ="export-notebook" href="#"><span class="ui-icon export-notebook-icon"></span>Export Chart (XML)</a></li>
                </ul>
                <button id = "display-button" class="nocapture">View</button>
                <ul id = "display-options" class="nocapture">
                    <li><a id ="display-entities" href="#"><span class="ui-icon display-entity"></span>Account Holders</a></li>
                    <li><a id ="display-charts" href="#"><span class="ui-icon display-chart"></span>Account Activity</a></li>
                </ul>
            </div>

            <div id = "filter-container">
                <div id = "filter">
                    <span class="title">Transaction Flow:</span>
                    <select id = "interval"></select>
                    <input type="text" id="datepickerfrom" class = "dateinput dateinputfrom" />
                    <label for = "datepickerto" class= "datelabel">to</label>
                    <input type="text" id="datepickerto" class = "dateinput" />
                    <img src="img/flow.png" id="flow-cue" alt="">
                    <button id = "applydates" style="display: none;">Apply</button>
                </div>
            </div>
        </div>

        <div id= "belowheader">
            <div id = "workspace" class = "workspace" >
                <div id = "cards"></div>
                <div id = "sankey"></div>
            </div>

            <div id = "drop-target-canvas" class = "dropTargetCanvas" ></div>

            <div id = "footer" class="nocapture">
                <h3 class="footer-title">Details</h3>
                <div id = "footer-content">
                    <div id = "details">
                        <div id = "details-content"></div>
                    </div>
                    <div id = "transactions">
                        <input id="filterHighlighted" type="checkbox">
                        <div id="filterHighlightedLabel">
                            <span id="filterHighlightedHilite">Highlighted</span> Only
                        </div>
                        <button id= "exportTransactions">Export</button>
                        <table id="transactions-table" class='display dataTable' width='100%'>
                            <thead>
                                <tr class='tHeader'>
                                    <td>#</td><th>Date</th><th>Comment</th><th>Inflowing</th><th>Outflowing</th>
                                </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div id = "advancedDialog" title = "Advanced Match Criteria"></div>

        <!-- common base stuff -->
        <script type="text/javascript" src="scripts/jquery.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/json2.js"></script>

        <!-- For Aperture pub sub -->
        <script type="text/javascript" src="scripts/lib/extern/OpenAjaxUnmanagedHub.js"></script>

        <!-- for Aperture maps -->
        <script type="text/javascript" src="scripts/lib/extern/proj4js.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/OpenLayers-min.js"></script>

        <!-- for Aperture core -->
        <script type="text/javascript" src="scripts/lib/extern/raphael.js"></script>
        <script type="text/javascript" src="aperture/aperture.js"></script>

        <!-- **Aperture application configuration** -->
        <script type="text/javascript" src="rest/config.js"></script>

        <!-- other libs -->
        <script type="text/javascript" src="scripts/lib/extern/underscore.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/jquery.address.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/jquery-ui.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/jquery.blockUI.js"></script>
        <script type="text/javascript" src="scripts/lib/influent-jquery/influent-jquery.js"></script>
        <script type="text/javascript" src="scripts/lib/extern/cookieUtil.js"></script>
        
        <!-- RequireJS -->
        <script type="text/javascript" data-main="scripts/main" src="scripts/lib/extern/require.js"></script>
        
        <script>
            <%!
 				String createSessionId() {
					return UUID.randomUUID().toString();
				}
			%>
        
			function getQueryParam(name) {
				var regex = new RegExp('[\\?&]' + name + '=([^&#]*)'),
        			values = regex.exec(location.search);
			    return values == null ? null : decodeURIComponent(values[1].replace(/\+/g, ' '));
			}
			
        	var cookieId = 
        		aperture.config.get()['aperture.io'].restEndpoint.replace('%host%', 'sessionId');
        	var cookieExpiryMinutes = aperture.config.get()['influent.config']['sessionTimeoutInMinutes'] || 24*60;
			var sessionId = getQueryParam('sessionId');
			var capture = getQueryParam('capture');
			
			// if don't have a session id at all in parameters, check cookie.
			// we are allowing a present param with an empty value be another reset queue here.
        	if (sessionId == null) {
        		sessionId = cookieUtil().readCookie(cookieId);
	        }
	        
        	// if no session is in use or we want a new one, make a new one.
        	if (!sessionId) {
           		sessionId = '<%= createSessionId() %>'
	       		aperture.log.info('Creating new session : ' + sessionId);
	       	}
	        
	        function updateCookie() {
	        	cookieUtil().createCookie(cookieId, sessionId, cookieExpiryMinutes);

				// update every minute so that expiry is from when page is left
	        	window.setTimeout(updateCookie, 60000);
			}
			
	        // update cookie expiry
        	updateCookie();

        	// make this valid
	        capture = capture || false;

			// trace it out for now
			aperture.log.info('cookie: ' + cookieId + ', session: ' + sessionId + ', capture: ' + capture);
			        	
            var settingsDiv = $('#settings');
            settingsDiv.attr('sessionid', sessionId);
            settingsDiv.attr('capture', capture);
                        
        </script>
    </body>
</html>