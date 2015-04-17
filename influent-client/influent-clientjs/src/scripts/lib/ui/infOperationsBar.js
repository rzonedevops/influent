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
		'lib/communication/applicationChannels',
		'hbs!templates/operationsBar/infOperationsBarButton'
	],
	function(
		appChannel,
		infOperationsBarButtonTemplate
	) {
		var operationsBarModule = (function() {
			var operationsBarInterface = {};

			operationsBarInterface.create = function(canvas) {

				var _UIObjectState = {
					canvas : null
				};

				if (canvas == null) {
					_UIObjectState.canvas = $('<div class="infOperationsBar"/>');
				} else {
					_UIObjectState.canvas = canvas;
				}

				// -----------------------------------------------------------------------------------------------------
				//	PUBLIC INTERFACE
				//--------------------------------------------------------------------------------------------------
				var operationsBarObject = { _buttons: [] };

				operationsBarObject.getElement = function() {
					return _UIObjectState.canvas;
				};

				//------------------------------------------------------------------------------------------------------

				operationsBarObject.buttons = function(buttons) {
					if (arguments.length === 0) {
						return this._buttons;
					}
					
					this._buttons = buttons;
					
					$.each(buttons, function(idx, button) {
						var buttonSpec = {
							title : button.title,
							iconClass : button.icon,
							annotationIconClass : button.annotationIcon
						};
						if (button.switchesTo) {
							buttonSpec['switchesTo'] = button.switchesTo;
						}
						if (button.requiresSelection) {
							buttonSpec['requiresSelection'] = button.requiresSelection;
						}
						_UIObjectState.canvas.append(infOperationsBarButtonTemplate(buttonSpec));

						var buttonElement = _UIObjectState.canvas.children().last();
						if (button.callback) {
							buttonElement.click(function() {
								button.callback();
							});
						}

						if (!button.callback || button.requiresSelection) {
							buttonElement.addClass('disabled');
						}
					});
				};

				//------------------------------------------------------------------------------------------------------

				operationsBarObject.enableButtons = function(enable) {
					var $buttons = _UIObjectState.canvas.find('.btn');
					$buttons.each(function(idx, button) {
						var $button = $(button);
						if ( $button.hasClass('requiresSelection')) {
							if (enable && $button.hasClass('disabled')) {
								$button.removeClass('disabled');
							} else if (!enable && !$button.hasClass('disabled')) {
								$button.addClass('disabled');
							}
						}
					});
				};

				//------------------------------------------------------------------------------------------------------

				return operationsBarObject;
			};

			return operationsBarInterface;
		})();

		return operationsBarModule;
	}
);
