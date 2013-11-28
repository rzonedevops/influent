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
define(['jquery', 'lib/channels', 'lib/util/xfUtil', 'lib/ui/toolbarOperations', 'lib/ui/xfModalDialog'],
function($, chan, xfUtil, toolbarOp, xfModalDialog) {
    var _toolbarState = {
        canvas : null,
        toolbarDiv : null,
        searchDiv : null,
        leftOp : null,
        rightOp : null,
        leftOpState : null,
        rightOpState : null,
        maxClusterCount : 20 //TODO: Until this is loaded from a config, this value must be kept in sync with the value in fileRenderer.
    };

    var _renderDefaults = {
      TOOLBAR_BTN_HEIGHT : 20
    };

    var _isVisualDescendant = function(visualInfo, uiType){
        var parentObj = visualInfo.spec.parent;
        if (_.isEmpty(parentObj) // Parent has not been set - newly created item.
            || parentObj.getUIType() == 'xfColumn'){
            return false;
        }
        if (parentObj.getUIType() == uiType){
            return true;
        }
        return _isVisualDescendant(parentObj.getVisualInfo(), uiType);
    };

    var _getVisualAncestor = function(visualInfo, uiType){
        var parentObj = visualInfo.spec.parent;
        if (_.isEmpty(parentObj) // Parent has not been set - newly created item.
            || visualInfo.UIType == 'xfColumn'){
            return null;
        }
        if (parentObj.getUIType() == uiType){
            return parentObj;
        }
        return _getVisualAncestor(parentObj.getVisualInfo(), uiType);
    };

    var _resetControls = function(){
        if (_toolbarState.toolbarDiv){
            _toolbarState.toolbarDiv.remove();
        }
        if (_toolbarState.matchDiv){
            _toolbarState.matchDiv.remove();
        }
        if (_toolbarState.searchDiv){
            _toolbarState.searchDiv.remove();
        }
        if (_toolbarState.leftOp && _toolbarState.leftOpState != toolbarOp.WORKING){
            _toolbarState.leftOp.remove();
        }
        if (_toolbarState.rightOp && _toolbarState.rightOpState != toolbarOp.WORKING){
            _toolbarState.rightOp.remove();
        }
    };

    var _getStateElement = function(selector){
        var element = _toolbarState.canvas.children(selector).first();
        return (element.length == 0)?null:element;
    };

    var _createBranchControls = function(visualInfo, cardHeight){
        _toolbarState.leftOp = _getStateElement('.leftOp');
        var leftImg = null;
        var leftOp = null;
        if (_toolbarState.leftOp == null) {
            leftOp = $('<div/>');
            leftImg = $('<img/>');
            leftImg.addClass('toolbarOpImg');
            leftOp.append(leftImg);
            leftOp.addClass('leftOp');
            leftOp.addClass('toolbarOpButton');
            leftOp.addClass('selectedContainer');
            _toolbarState.leftOp = leftOp;
            _toolbarState.canvas.append(leftOp);
            _toolbarState.leftOpState = toolbarOp.BRANCH;
        } else {
            leftOp = _toolbarState.leftOp;
            leftImg = $(_toolbarState.leftOp.children()[0]);
        }

        var requestedLeftOp = visualInfo.spec.leftOperation;
        leftOp.unbind('click');
        if (requestedLeftOp == toolbarOp.BRANCH) {
            leftImg.attr('src','img/expand_btn.png');
            leftOp.click(
                function() {
                    publishBranchRequest(chan.BRANCH_LEFT_EVENT, 'left');
                    return true;
                }
            );
        } else if (requestedLeftOp == toolbarOp.WORKING) {
            leftImg.attr('src','img/ajax-loader.gif');
        }
        leftOp.css('top', (cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/2);
        _toolbarState.leftOpState = requestedLeftOp;

        _toolbarState.rightOp = _getStateElement('.rightOp');
        var rightImg = null;
        var rightOp = null;
        if (_toolbarState.rightOp == null) {
            rightOp = $('<div/>');
            rightImg = $('<img/>');
            rightImg.addClass('toolbarOpImg');
            rightOp.append(rightImg);
            rightOp.addClass('rightOp');
            rightOp.addClass('selectedContainer');
            _toolbarState.rightOp = rightOp;
            _toolbarState.canvas.append(rightOp);
        } else {
            rightOp = _toolbarState.rightOp;
            rightImg = $(_toolbarState.rightOp.children()[0]);
        }

        var requestedRightOp = visualInfo.spec.rightOperation;
        rightOp.unbind('click');
        if (requestedRightOp == toolbarOp.BRANCH) {
            rightImg.attr('src','img/expand_btn.png');
            rightOp.click(
                function() {
                    publishBranchRequest(chan.BRANCH_RIGHT_EVENT, 'right');
                    return true;
                }
            );
        } else if (requestedRightOp == toolbarOp.WORKING ) {
            rightImg.attr('src','img/ajax-loader.gif');
        }
        rightOp.css('top', (cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/2);
        _toolbarState.rightOpState = requestedRightOp;

        var publishBranchRequest = function(branchEvent, direction) {
            aperture.pubsub.publish(
                branchEvent,
                {
                    xfId : visualInfo.xfId,
                    direction : direction
                }
            );
        };
    };

    var _getContainedCardDataIds = function(visualInfo) {
        var containedIds = [];

        if (visualInfo.UIType == 'xfFile') {
            if (visualInfo.clusterUIObject != null) {
                containedIds = containedIds.concat(visualInfo.clusterUIObject.getContainedCardDataIds());
            }
        } else if (visualInfo.UIType == 'xfMutableCluster') {
            for (var i = 0; i < visualInfo.children.length; i++) {
                var child = visualInfo.children[i];
                if (child.getUIType() == 'xfCard' && child.getDataId() != null) {
                    containedIds.push(child.getDataId());
                } else if (child.getUIType() == 'xfMutableCluster') {
                    containedIds = containedIds.concat(child.getContainedCardDataIds());
                }
            }
        } else if (visualInfo.UIType == 'xfCard') {
            containedIds.push(visualInfo.spec.dataId);
        }

        return containedIds;
    };

    var _createToolbar = function(visualInfo, cardWidth){
        // Create toolbar buttons.
        _toolbarState.toolbarDiv = _getStateElement('.cardToolbar');
        var buttonItemList = [];
        // Reference to store the position of the pin button.
        // If it isn't the 2nd  button from the right, we'll
        // need to reposition it.
        var pinIndex = undefined;

        if (_toolbarState.toolbarDiv == null){
            _toolbarState.toolbarDiv = $('<div>');
            _toolbarState.toolbarDiv.addClass('cardToolbar');
            _toolbarState.toolbarDiv.css('position','absolute');

            // extra padding to ensure toolbar and it's target don't allow mouse over events to fall between them
            _toolbarState.toolbarDiv.css('padding-bottom', 3);
            _toolbarState.canvas.append(_toolbarState.toolbarDiv);

            // If this card is a branch result, show
            // the additional controls like the
            // new file folder button, etc.
            if (visualInfo.showToolbar){
                var buttonCount = 1;
                // File button
                var bFileable = visualInfo.toolbarSpec['allowFile'];
                if (bFileable === true) {
                    var fileDiv = $('<div class="new-file-button">');
                    fileDiv.click(function() {
                        // Check if this cluster is within the max. cluster count threshold.
                        // If it isn't, alert the user that this action may take a while.
                        var clusterCount = visualInfo.spec.count;
                        if (clusterCount && clusterCount > _toolbarState.maxClusterCount){
                            xfModalDialog.createInstance({
                                title : 'Add Cluster to File?',
                                contents : 'Adding large clusters to file may take longer than expected. Do you wish to continue?',
                                buttons : {
                                    "Add File" : function() {
                                        aperture.pubsub.publish(chan.CREATE_FILE_REQUEST, {xfId : visualInfo.xfId});
                                    },
                                    "Cancel" : function() {}
                                }
                            });
                        }
                        else {
                            aperture.pubsub.publish(chan.CREATE_FILE_REQUEST, {xfId : visualInfo.xfId});
                        }
                        return false;
                    });

                    var fileImg = $('<img/>');
                    fileImg.attr('src' , 'img/new_file.png');

                    fileDiv.append(fileImg);
                    fileDiv.css('float','left');
                    fileDiv.addClass('cardToolbarItem');
                    buttonItemList.push({
                        id : 'new_file',
                        div : fileDiv
                    });
                    buttonCount++;
                }

                // Search button
                var bSearchable = visualInfo.toolbarSpec['allowSearch'];
                if (bSearchable === true) {
                    var searchDiv = $('<div class="new-search-button">');
                    searchDiv.click(function() {
                        var rootFile = _getVisualAncestor(visualInfo, 'xfFile');
                        if (visualInfo.UIType == 'xfFile' || rootFile != null) {
                            var fileVisualInfo = (rootFile == null) ? visualInfo : rootFile.getVisualInfo();
                            var matchUIObj = fileVisualInfo.matchUIObject;
                            if(matchUIObj == null) {
                                aperture.pubsub.publish(
                                    chan.SHOW_MATCH_REQUEST,
                                    {
                                        xfId : fileVisualInfo.xfId
                                    }
                                );
                            }

                            var containedIds = _getContainedCardDataIds(visualInfo);
                            var targetSearchTerm = '';
                            if (containedIds.length > 0) {
                                targetSearchTerm = 'like:';
                                for (var i = 0; i < containedIds.length; i++) {
                                    targetSearchTerm = targetSearchTerm + containedIds[i] + ',';
                                }
                                targetSearchTerm = targetSearchTerm.slice(0, -1);
                            }

                            aperture.pubsub.publish(
                                chan.APPLY_PATTERN_SEARCH_TERM,
                                {
                                    xfId : fileVisualInfo.xfId,
                                    searchTerm : targetSearchTerm
                                }
                            );
                        } else {
                            aperture.pubsub.publish(chan.CREATE_FILE_REQUEST, {xfId : visualInfo.xfId, showMatchCard : true});
                        }
                        return false;
                    });

                    var searchImg = $('<img/>');
                    searchImg.attr('src' , 'img/search-small.png');

                    searchDiv.append(searchImg);
                    searchDiv.css('float','left');
                    searchDiv.addClass('cardToolbarItem');
                    buttonItemList.push({
                        id : 'search',
                        div : searchDiv
                    });
                    buttonCount++;
                }

                // Focus button
                var bFocusable = visualInfo.toolbarSpec['allowFocus'];
                if ( bFocusable === true) {
                    var focusBtnDiv = $('<div class="highlight-flow-button">');
                    focusBtnDiv.click(function() {
                        aperture.pubsub.publish(chan.FOCUS_CHANGE_REQUEST, {xfId : visualInfo.xfId});
                        return false;
                    });

                    var focusImg = $('<img/>');
                    focusImg.attr('src', 'img/arrows.png');

                    focusBtnDiv.append(focusImg);
                    focusBtnDiv.css('float','left');
                    focusBtnDiv.addClass('cardToolbarItem');
                    buttonItemList.push({
                        id : 'focus',
                        div : focusBtnDiv
                    });
                    buttonCount++;
                }

                // Pin button
                var bPinable = visualInfo.toolbarSpec['allowPin'];
                if ( bPinable === true) {
                    var pinBtnDiv = $('<div class="highlight-flow-button">');
                    pinBtnDiv.click(function() {
                        aperture.pubsub.publish(chan.PIN_TOGGLE, {xfId : visualInfo.xfId});
                        return false;
                    });

                    var pinImg = $('<img/>');
                    pinImg.attr('src', visualInfo.isPinned?'img/empty-button.png':'img/pin-button.png');
                    pinBtnDiv.attr('id', visualInfo.xfId+'_pinBtnDiv');
                    pinBtnDiv.append(pinImg);
                    pinBtnDiv.css('float','left');
                    pinBtnDiv.addClass('cardToolbarItem');
                    buttonItemList.push({
                        id : 'pin',
                        div : pinBtnDiv
                    });
                    pinIndex = buttonCount-1;
                    buttonCount++;
                }
                // xfFile objects require different positioning due to the asymmetrical shape of the file tabs.
                if (visualInfo.UIType == 'xfFile'){
                    _toolbarState.toolbarDiv.css('top', -0.5*_renderDefaults.TOOLBAR_BTN_HEIGHT - 5);
                    _toolbarState.toolbarDiv.css('left', cardWidth - buttonCount*_renderDefaults.TOOLBAR_BTN_HEIGHT - 10);
                }
                else {
                    _toolbarState.toolbarDiv.css('top', -_renderDefaults.TOOLBAR_BTN_HEIGHT - 5);
                    _toolbarState.toolbarDiv.css('right', 0);
                }
            }
            else {
                _toolbarState.toolbarDiv.css('right', 5);
                _toolbarState.toolbarDiv.css('top', 5);
            }

            // Add default Close button
            var bCloseable = visualInfo.toolbarSpec.allowClose;
            if ( bCloseable === true || bCloseable === undefined ) {
                var closeDiv = $('<div>');
                closeDiv.click(
                    function() {
                        if (visualInfo.UIType == 'xfFile'){
                            xfModalDialog.createInstance({
                                title : 'Remove File?',
                                contents : 'Are you sure you want to remove "<b>' + visualInfo.title + '"</b>?',
                                buttons : {
                                    "Remove" : function() {
                                        aperture.pubsub.publish(chan.REMOVE_REQUEST, {xfId : visualInfo.xfId});
                                    },
                                    "Cancel" : function() {}
                                }
                            });
                        }
                        else {
                            aperture.pubsub.publish(chan.REMOVE_REQUEST, {xfId : visualInfo.xfId});
                        }
                        return false;
                    }
                );

                var closeImg = $('<img/>');
                closeImg.attr('src', 'img/close_box.png');
                closeDiv.addClass('cardToolbarItem');
                closeDiv.addClass('remove-button');
                closeDiv.append(closeImg);
                closeDiv.css('top', 5);
                closeDiv.css('float','left');
                buttonItemList.push({
                    id : 'close',
                    div : closeDiv
                });
            }
        }
        // Redmine #6576 - We always want the pin
        // button to be second from the right.
        var sortedButtonList = [];
        if (pinIndex && pinIndex != (buttonItemList.length-2)){
            var pinItem = buttonItemList.splice(pinIndex, 1)[0];
            var lastItem = buttonItemList.splice(buttonItemList.length-1, 1)[0];
            sortedButtonList = sortedButtonList.concat(buttonItemList);
            sortedButtonList.push(pinItem);
            sortedButtonList.push(lastItem);
        }
        else {
            sortedButtonList = buttonItemList;
        }

        for (var i=0; i < sortedButtonList.length; i++){
            _toolbarState.toolbarDiv.append(sortedButtonList[i].div);
        }
    };

    /**
     * Creates custom controls for the search results
     * contained within a xfMatch uiObject.
     * @param visualInfo
     * @param cardHeight
     * @private
     */
    var _createMatchControls = function(visualInfo, cardHeight){
        _toolbarState.matchDiv = _getStateElement('.matchToolbarCtrl');
        if (_toolbarState.matchDiv == null){
            _toolbarState.matchDiv = $('<div/>');
            _toolbarState.matchDiv.addClass('matchToolbar');
            _toolbarState.matchDiv.css('top', (cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/2);
            _toolbarState.canvas.append(_toolbarState.matchDiv);

            // Focus button
            var bFocusable = visualInfo.toolbarSpec['allowFocus'];
            if ( bFocusable === true) {
                var focusBtnDiv = $('<div class="highlight-flow-button">');
                focusBtnDiv.click(function() {
                    aperture.pubsub.publish(chan.FOCUS_CHANGE_REQUEST, {xfId : visualInfo.xfId});
                    return false;
                });

                var focusImg = $('<img/>');
                focusImg.attr('src', 'img/arrows.png');

                focusBtnDiv.append(focusImg);
                focusBtnDiv.css('position', 'relative');
                focusBtnDiv.css('bottom', (cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/2);
                focusBtnDiv.css('float','left');
                focusBtnDiv.css('right',_renderDefaults.TOOLBAR_BTN_HEIGHT/2);
                focusBtnDiv.css('margin-right', 3);
                focusBtnDiv.css('margin-top', 3);
                _toolbarState.matchDiv.append(focusBtnDiv);

            }

            // Add move-to-file button.
            // Find the parent xfFile for this xfMatch.
            var fileObj = _getVisualAncestor(visualInfo, 'xfFile');
            if (fileObj != null){
                var fileDiv = $('<div class="new-file-button">');
                fileDiv.click(function() {
                    return !visualInfo.isSelected;
                });

                var fileImg = $('<img/>');
                fileImg.attr('src' , 'img/file_add.png');

                fileDiv.append(fileImg);
                fileDiv.addClass('matchToolbarItem');
                fileDiv.click(function(){
                    aperture.pubsub.publish(chan.ADD_TO_FILE_REQUEST, {
                    	containerId : fileObj.getXfId(),
                    	cardId: visualInfo.xfId
                    });
                });
                fileDiv.css('position', 'relative');
                fileDiv.css('bottom', (cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/4 + 5);
                fileDiv.css('float','left');
                fileDiv.css('margin-right', 3);
                fileDiv.css('margin-top', 3);
                _toolbarState.matchDiv.append(fileDiv);
            }
            else {
                console.error('There is no parent xfFile associated with the xfMatch: ' + visualInfo.xfId);
            }

            // Add close button.
            var closeDiv = $('<div>');
            closeDiv.click(
                function() {
                    aperture.pubsub.publish(chan.REMOVE_REQUEST, {xfId : visualInfo.xfId});
                    return false;
                }
            );

            var closeImg = $('<img/>');
            closeImg.attr('src', 'img/close_box.png');
            closeDiv.addClass('matchToolbarItem');
            closeDiv.addClass('remove-button');
            closeDiv.append(closeImg);
            closeDiv.css('position', 'relative');
            closeDiv.css('bottom', -(cardHeight - _renderDefaults.TOOLBAR_BTN_HEIGHT)/4 + 5);
            closeDiv.css('float','left');
            closeDiv.css('right',_renderDefaults.TOOLBAR_BTN_HEIGHT + 5);
            closeDiv.css('margin-right', 3);
            closeDiv.css('margin-top', 3);
            _toolbarState.matchDiv.append(closeDiv);
        }
    };

    var _createControls = function(visualInfo, parentCanvas, cardHeight) {
        if (_toolbarState.canvas != null){
            _resetControls();
        }

        var cardWidth = 0;
        if(visualInfo.isHighlighted) {
            cardWidth = parentCanvas.children('.fileBodyHighlighted').width();
        }
        else if(visualInfo.isSelected) {
            cardWidth = parentCanvas.children('.fileBodySelected').width();
        }
        else {
            cardWidth = parentCanvas.children('.fileBody').width();
        }

        // Create the toolbar buttons.
        _toolbarState.canvas = parentCanvas;

        // If this is an xfMatch object, we want to use a
        // custom button layout, otherwise use the default
        // toolbar.
        if (_isVisualDescendant(visualInfo, 'xfMatch')){
            _createMatchControls(visualInfo, cardHeight);
        }
        else {
            _createToolbar(visualInfo, cardWidth);
        }

        // Add expand right/left buttons.
        if (!(_isVisualDescendant(visualInfo, 'xfMatch')
            ||visualInfo.UIType == 'xfFile')){
            _createBranchControls(visualInfo, cardHeight);
        }
    };

    var toolbarRenderer = {
        createControls : _createControls,
        getRenderDefaults : function(){
            return _.clone(_renderDefaults);
        }
    };
    return toolbarRenderer;
});