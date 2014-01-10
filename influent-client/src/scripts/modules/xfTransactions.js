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
define(
    ['jquery', 'lib/module', 'lib/channels', 'lib/constants'],
    function($, modules, chan, constants) {
	
        var transactionsConstructor = function(sandbox) {

            var _transactionsState = {
                subscriberTokens : null,
                userSelectedTab: 'table',
                autoSelecting: false
            };

            //----------------------------------------------------------------------------------------------------------

            var _onUpdate = function(channel, data) {

                var transDiv = $('#transactions');
                var tableIndex = $("#tableTab").index() - 1;
                var chartIndex = $("#chartTab").index() - 1;

                if (data) {
                    if (data.uiType == constants.MODULE_NAMES.ENTITY) {

                        if (_.size(transDiv.tabs('option', 'disabled')) > 0 &&
                            _.contains(transDiv.tabs('option', 'disabled'), tableIndex)
                        ) {
                            transDiv.tabs(
                                'enable',
                                tableIndex
                            );
                        }

                        if (_transactionsState.userSelectedTab == 'table' &&
                            transDiv.tabs('option', 'selected') == chartIndex
                        ) {
                            _transactionsState.autoSelecting = true;
                            transDiv.tabs(
                                'select',
                                tableIndex
                            );
                        }
                    } else {
                        if (_transactionsState.userSelectedTab == 'table' &&
                            transDiv.tabs('option', 'selected') == tableIndex
                        ) {
                            _transactionsState.autoSelecting = true;
                            transDiv.tabs(
                                'select',
                                chartIndex
                            );
                        }
                        transDiv.tabs(
                            'disable',
                            tableIndex
                        );
                    }
                }
            };

            //----------------------------------------------------------------------------------------------------------

            var initialize = function() {
                $(function() {
                    $("#transactions").tabs(
                        {
                            select: function(event, ui) {

                                if (ui.panel.id == 'chartTab') {
                                    aperture.pubsub.publish(chan.REQUEST_CURRENT_STATE);
                                }

                                if (_transactionsState.autoSelecting) {
                                    _transactionsState.autoSelecting = false;
                                } else {
                                    _transactionsState.userSelectedTab = (ui.panel.id == 'chartTab') ?
                                        _transactionsState.userSelectedTab = 'chart' :
                                        _transactionsState.userSelectedTab = 'table';
                                }
                            }
                        }
                    );
                });
            };

            //----------------------------------------------------------------------------------------------------------

            var _initializeModule = function() {
                aperture.log.debug('_initializeModule xfTransactions');
                initialize();
            };

            //----------------------------------------------------------------------------------------------------------

            var _start = function() {
                var subTokens = {};

                subTokens[chan.SELECTION_CHANGE_EVENT] = aperture.pubsub.subscribe(chan.SELECTION_CHANGE_EVENT, _onUpdate);
                subTokens[chan.ALL_MODULES_STARTED] = aperture.pubsub.subscribe(chan.ALL_MODULES_STARTED, _initializeModule);

                _transactionsState.subscriberTokens = subTokens;
            };

            //--------------------------------------------------------------------------------------------------------------

            var _end = function() {
                for (var token in _transactionsState.subscriberTokens) {
                    if (_transactionsState.subscriberTokens.hasOwnProperty(token)) {
                        aperture.pubsub.unsubscribe(_transactionsState.subscriberTokens[token]);
                    }
                }
            };

            //--------------------------------------------------------------------------------------------------------------

            return {
                start : _start,
                end : _end
            };
        };

        modules.register('xfTransactions', transactionsConstructor);
    }
);