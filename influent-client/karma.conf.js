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

module.exports = function(config) {
	config.set({

		// NOTE: uncomment the // COVERAGE lines to instrument for coverage.
		//       comment them back out if you want to debug source code on the command line.
		plugins: [
			'karma-chrome-launcher',
//			'karma-coverage', // COVERAGE
			'karma-firefox-launcher',
			'karma-html2js-preprocessor',
			'karma-ie-launcher',
			'karma-jasmine',
			'karma-jasmine-jquery',
			'karma-junit-reporter',
			'karma-phantomjs-launcher',
			'karma-requirejs'
		],

		// base path that will be used to resolve all patterns (eg. files, exclude)
		basePath: '',

		// frameworks to use
		// available frameworks: https://npmjs.org/browse/keyword/karma-adapter
		frameworks: ['jasmine-jquery', 'jasmine'],

		// list of files / patterns to load in the browser
		files: [
			// jQuery imports
			'influent-clientjs/src/scripts/lib/extern/jquery.js',
			'influent-clientjs/src/scripts/lib/extern/jquery-ui.js',
			'influent-clientjs/src/scripts/lib/influent-jquery/influent-jquery.js',

			// bootstrap imports
			'influent-clientjs/filtered/bootstrap/bootstrap.js',
			'influent-clientjs/filtered/bootstrap/bootstrap-select.js',
			'influent-clientjs/filtered/bootstrap/bootstrap-datepicker.js',

			// aperture import
			'influent-clientjs/src/aperture/aperture.js',

			// Draper logging
			'influent-clientjs/src/scripts/lib/log/DraperAppender.js',

			// always add require after global imports
			'node_modules/requirejs/require.js',
			'node_modules/karma-requirejs/lib/adapter.js',

			// production code
			{pattern: 'influent-clientjs/src/scripts/**/*.js', included: false},
			{pattern: 'influent-clientjs/src/scripts/templates/**/*.hbs', included: false},
			{pattern: 'influent-clientjs/src/scripts/templates/helpers/*.js', included: false},

			// test code
			'influent-clientjs/test/test-config.js',
			'influent-clientjs/test/test-main.js',
			'influent-clientjs/test/test-specs/html/*.html',
			{pattern: 'influent-clientjs/test/test-specs/**/*.js', included: false},
			{pattern: 'influent-clientjs/test/extensions/*.js', included: false},
			{pattern: 'influent-clientjs/test/extensions/templates/**/*.hbs', included: false}
		],

		// list of files to exclude
		exclude: [
			'influent-clientjs/src/scripts/main.js'
		],

		// preprocess matching files before serving them to the browser
		// available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
		preprocessors: {
			'**/*.html': ['html2js']//,
//			'influent-clientjs/src/scripts/**/*.js': ['coverage'] // COVERAGE
		},

		// test results reporter to use
		// possible values: 'dots', 'progress'
		// available reporters: https://npmjs.org/browse/keyword/karma-reporter
		reporters: [
			'progress',
			'junit'//,
//			'coverage' // COVERAGE
		],

		// COVERAGE
//		coverageReporter: {
//			type : 'html',
//			dir : 'coverage/'
//		},

		junitReporter: {
			outputFile: 'target/test-reports/karma-results.xml'
		},

		// web server port
		port: 9876,

		// enable / disable colors in the output (reporters and logs)
		colors: true,

		// increase browser timeout length (default 10s).   PhantomJS can take a while to initialize
		browserNoActivityTimeout: 100000,

		// level of logging
		// possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
		logLevel: config.LOG_INFO,

		// enable / disable watching file and executing tests whenever any file changes
		autoWatch: true,

		// start these browsers
		// available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
		browsers: [
			'PhantomJS'//,
			//'Chrome' //,
			//'Firefox' //,
			//'IE'
		],

		// Continuous Integration mode
		// if true, Karma captures browsers, runs the tests and exits
		singleRun: true
	});
};
