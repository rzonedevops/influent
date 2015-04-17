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

var allTestFiles = [];
var TEST_REGEXP = /(spec|test)\.js$/i;

var pathToModule = function(path) {
	return path.replace(/^\/base\//, '').replace(/\.js$/, '');
};

Object.keys(window.__karma__.files).forEach(function(file) {
	if (TEST_REGEXP.test(file)) {
		// Normalize paths to RequireJS module names.
		allTestFiles.push(pathToModule(file));
	}
});

require.config({
	// Karma serves files under /base, which is the basePath from your config file
	baseUrl: '/base',
	paths: {
		cookieUtil: 'influent-clientjs/src/scripts/lib/extern/cookieUtil',
		handlebars: 'influent-clientjs/src/scripts/lib/extern/handlebars',
		hbs: 'influent-clientjs/src/scripts/lib/extern/hbs',
		img: 'influent-clientjs/src/img',
		jquery: 'influent-clientjs/src/scripts/lib/extern/jquery',
		lib: 'influent-clientjs/src/scripts/lib',
		modules: 'influent-clientjs/src/scripts/modules',
		moment: 'influent-clientjs/src/scripts/lib/extern/moment',
		templates: 'influent-clientjs/src/scripts/templates',
		underscore: 'influent-clientjs/src/scripts/lib/extern/underscore',
		views: 'influent-clientjs/src/scripts/views',
		'route-recognizer-bundle': 'route-recognizer',
		'rsvp-bundle': 'rsvp',

		//for testing
		'../../extensions': 'influent-clientjs/test/extensions/extensions',
		'extensions/templates/accountsView/searchDetailsOverlay': 'influent-clientjs/test/extensions/templates/accountsView/searchDetailsOverlay'
	},
	bundles: {
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
	},
	
	// dynamically load all test files
	deps: allTestFiles,
	
	// we have to kickoff jasmine, as it is asynchronous
	callback: window.__karma__.start
});