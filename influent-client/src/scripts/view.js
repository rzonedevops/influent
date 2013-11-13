/**
 * Copyright (c) 2013 Oculus Info Inc.
 * http://www.oculusinfo.com/
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

/**
 * Manages the view for the app.
 */
define(['jquery', 'lib/viewproto', 'lib/channels', 'modules/xfCards', 'modules/xfRenderer', 'modules/xfHeader', 'modules/xfEntityDetails', 'modules/xfAdvancedSearch', 'modules/xfSankey', 'modules/xfTransactionsTable', 'modules/xfDropTargetRenderer'],
    function($, View, chan) {

	// hook up workspace height management, triggered on init and footer expand / collapse
	(function() {
		function onFooterExpand(expand) {
			
			// workspace needs to leave gap for footer, so adjust
			var height = $('#footer').height();
			$('#workspace').css('bottom', height);
	
			// notify
			aperture.pubsub.publish(chan.FOOTER_STATE, {
				expanded : expand
			});
		}
		
		// DOM Ready
		$(function() {
			onFooterExpand(true);
		});
		
		// resize workspace area when footer is done collapsing.
		$( '#footer' ).accordion({
			collapsible: true,
			heightStyle: 'auto',
			change: function (event, ui) {
				onFooterExpand(ui.newContent ? true : false);
			}
	    });
	}());

	return View.extend( 'Influent', {
		
		/**
		 * Constructor function.
		 */
		init : function() {
			
			// call base class constructor.
			View.prototype.init.call(this);
			
			this.inited = false;
		},
		
		/**
		 * Returns a title that summarises the state
		 * 
		 * @returns {String}
		 * 	the title
		 */
		title : function() {
			
			// this.state things should be added behind the name here.
			return aperture.config.get()['influent.config'].title || 'Name Me';
		},
		
		/**
		 * Default state
		 */
		defaults : {
			// state variables here
		},
	
		/**
		 * Set view state
		 */
		view : function(trialState, callback) {
			
			// VALIDATE HERE. can use this.ajax to check server things.
			var validState = trialState;
			
			
			// apply when done.
			this.doView(validState, callback);
		},
		
		/**
		 * Set view state
		 */
		doView : function (validState, callback) {
			
			var banner = aperture.config.get()['influent.config']['banner'];
			if (banner) {
				$('#banner-text').html(banner);
			}
			
			// make sure these modules are started. could do module switches here
            this.modules.start('xfRenderer', {div:'cards', capture:$('#settings').attr('capture')});
            this.modules.start('xfSankey', {div:'sankey', capture:$('#settings').attr('capture')});
            this.modules.start('xfCards', {div:'cards', sessionId:$('#settings').attr('sessionid')});
            this.modules.start('xfHeader', {div:'header'});
            this.modules.start('xfAdvancedSearch', {div:'search'});
            this.modules.start('xfEntityDetails', {div:'popup'});
			this.modules.start('xfTransactionsTable', {div:"transactions-content"});
            this.modules.start('xfDropTargetRenderer', {div:"drop-target-canvas"});
			
			// then call base implementation too
			View.prototype.doView.call(this, validState, callback);


            // Send a notification that all modules have been started.   This will kickoff initialization
			if (!this.inited) {
				this.inited = true;
	            aperture.pubsub.publish(chan.ALL_MODULES_STARTED, {noRender : true});
			}
		}
		
	});
});


