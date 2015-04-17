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

requirejs.config({
	'baseUrl': 'scripts/lib/extern',
	'paths': {
		'lib': '../../lib',
		'modules': '../../modules',
		'templates': '../../templates',
		'views': '../../views',
		
		'rsvp-bundle': 'rsvp',
		'route-recognizer-bundle': 'route-recognizer'
	},
	'bundles': {
		/**
		 * rsvp defines a bunch of different modules which need to be captured here.
		 * rsvp_base is used as the base implementation of rsvp. However, the router libs
		 * are expecting the library to be an es6 compliant lib, so there are extra rsvp
		 * defines later to support this.
		 */
		'rsvp-bundle': [
			'rsvp_base/-internal',
			'rsvp_base/all-settled',
			'rsvp_base/all',
			'rsvp_base/asap',
			'rsvp_base/config',
			'rsvp_base/defer',
			'rsvp_base/enumerator',
			'rsvp_base/events',
			'rsvp_base/filter',
			'rsvp_base/hash-settled',
			'rsvp_base/hash',
			'rsvp_base/instrument',
			'rsvp_base/map',
			'rsvp_base/node',
			'rsvp_base/promise-hash',
			'rsvp_base/promise',
			'rsvp_base/promise/all',
			'rsvp_base/promise/cast',
			'rsvp_base/promise/race',
			'rsvp_base/promise/reject',
			'rsvp_base/promise/resolve',
			'rsvp_base/race',
			'rsvp_base/reject',
			'rsvp_base/resolve',
			'rsvp_base/rethrow',
			'rsvp_base/utils',
			'rsvp_base'
		],

		'route-recognizer-bundle': [
			'route-recognizer/dsl',
			'route-recognizer'
		]
	}
});

/**
 * These defines support the rsvp lib as an es6 compliant lib
 */
define('rsvp', ['rsvp_base'], function(RSVP) { return {'default': RSVP}; });
define('rsvp/promise', ['rsvp_base/promise'], function(RSVP) { return {'default': RSVP.Promise}; });

//Load scripts.
require(['lib/app', 'lib/plugins'], function(app) {

	app.setup();

	// DOM Ready, start app
	$(function() {
		app.start();
	});
});
