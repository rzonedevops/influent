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

define(
	[
		'lib/constants',
		'lib/util/xfUtil',
		'hbs!templates/criteria/fieldSelector'
	],
	function(
		constants,
		xfUtil,
		fieldSelectorTemplate
	) {

		/**
		 * Return the module for access by clients, which exposes a fn for
		 * adding new ordering ui to a canvas and returning an interface to control it.
		 */
		return {

			createInstance : function(canvas) {

				var _UIObjectState = {
					canvas : canvas
				};

				/**
				 * add the ui for the direction editor and return the setter/getter fn
				 */
				function _directionUI(parent) {
					var opt = $('<select class="advancedsearch-direction"></select>')
						.appendTo(parent);

					return {
						add : function(value, label) {
							$('<option></option>')
								.attr('value', value)
								.html(label || value)
								.appendTo(opt);
							return this;
						},
						value : function(text) {
							if (arguments.length !== 0) {
								opt.val(text);
								return this;
							}
							return opt.val();
						},
						change : function(callback) {
							opt.change(function() {
								aperture.log.log({
									type: aperture.log.draperType.USER,
									workflow: aperture.log.draperWorkflow.WF_GETDATA,
									activity: 'select_filter_menu_option',
									description: 'Order direction changed'
								});
								callback();
							});
							return this;
						},
						clear : function() {
							opt.empty();
							return this;
						}
					};
				}

				/**
				 * Creates a field selector
				 */
				function _fieldSelector(descriptors, descriptor) {
					return $(fieldSelectorTemplate(descriptors)).val(descriptor.key);
				}

				/**
				 * Enables the field choices specified.
				 */
				function _removeButton() {

					var removeButton = $('<div class="advancedsearch-remove-button"></div>');
					xfUtil.makeTooltip(removeButton, 'remove this ordering');

					return removeButton;
				}

				return {

					/**
					 * @returns the criteria list.
					 */
					list: function () {
						var ui;
						ui = [];

						_UIObjectState.canvas.find('.advancedsearch-ordering').each(function (i, elem) {
							ui.push($(elem).data('api'));
						});

						return ui;
					},

					/**
					 * Creates a new ordering ui and appends it to the canvas.
					 *
					 * @param descriptors
					 *  the list of descriptors provided by the custom search implementation.
					 *
					 * @param descriptor
					 *  the selected descriptor
					 *
					 * @returns Object
					 *  an interface to the new criteria object.
					 */
					add: function (descriptors, descriptor) {
						var _ui = $('<div></div>')
							.addClass('advancedsearch-ordering')
							.appendTo(_UIObjectState.canvas)
							.append(_removeButton().click(function () {
								api.remove();

								aperture.log.log({
									type: aperture.log.draperType.USER,
									workflow: aperture.log.draperWorkflow.WF_GETDATA,
									activity: 'select_filter_menu_option',
									description: 'ordering removed'
								});
							}));

						var _left = $('<div></div>')
							.addClass('advancedsearch-ordering-left')
							.appendTo(_ui);

						var _privates = {key: descriptor.key};
						var api = null;

						function onFieldSelectorChange() {
							var descriptor = null;
							for (var i = 0; i < descriptors.length && descriptor === null; i++) {
								if (descriptors[i].key === $(this).val()) {
									descriptor = descriptors[i];
								}
							}
							_privates.key = descriptor.key;

							if (!constants.UNIT_TESTS_ENABLED) {
								aperture.log.log({
									type: aperture.log.draperType.USER,
									workflow: aperture.log.draperWorkflow.WF_GETDATA,
									activity: 'select_filter_menu_option',
									description: 'Ordering field changed',
									data: {
										field: descriptor.friendlyText ? descriptor.friendlyText : descriptor.key
									}
								});
							}
						}

						// create the field selector.
						_fieldSelector(descriptors, descriptor)
							.appendTo(_left)
							.change(onFieldSelectorChange);

						_privates.direction = _directionUI(_left);

						_privates.direction.add('ascending');
						_privates.direction.add('descending');

						api = {

							/**
							 * @returns the key of the descriptor being edited.
							 */
							key: function () {
								return _privates.key;
							},

							/**
							 * Gets or sets the direction
							 *
							 * @param [value] the value
							 * @returns the value.
							 */
							direction: function (value) {

								if (arguments.length !== 0) {
									_privates.direction.value(value);
								}

								return _privates.direction.value();
							},

							/**
							 * Removes the ui from the canvas.
							 */
							remove: function () {
								_ui.remove();
							}
						};

						/*
						TODO: Enable relevant api functions for unit tests
						if (constants.UNIT_TESTS_ENABLED) {
							api['getElement'] = function() { return _ui; };
							api['getFieldSelector'] = function() { return fieldSelector; };
							api['onFieldSelectorChange'] = function(paramChoice) { onFieldSelectorChange.call(paramChoice); };
							api['getEditor'] = function() { return _privates.editor; };
						}
						*/

						_ui.data('api', api);

						return api;
					}
				};
			}
		};
	}
);
