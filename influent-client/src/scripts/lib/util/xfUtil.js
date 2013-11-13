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
define([],
    function() {

        // Note that the use of ((http|ftp|https)://)* means that this also includes valid hash keys for charts, not just general URIs
        var VALID_URI_REGEX = new RegExp('((http|ftp|https)://)*[a-z0-9\-_]+(\.[a-z0-9\-_]+)+([a-z0-9\-\.,@\?^=%&;:/~\+#]*[a-z0-9\-@\?^=%&;/~\+#])?', 'i');

        //--------------------------------------------------------------------------------------------------------------

        /**
         * Returns true if the visualInfo from a given
         * uiObject is a descendant of the given uiType.
         * @param visualInfo
         * @param uiType
         * @returns {boolean}
         */
        var _isUITypeDescendant = function(uiObject, uiType){
            var parentObj = uiObject.getParent();
            if (_.isEmpty(parentObj) // Parent has not been set - newly created item.
                    || parentObj.getUIType() == 'xfColumn'){
                return false;
            }
            if (parentObj.getUIType() == uiType){
                return true;
            }
            return _isUITypeDescendant(parentObj, uiType);
        };

        /**
         * Returns the first ancestor of the given UIType, if any.
         * @param visualInfo
         * @param uiType
         * @returns {xfUIObject}
         * @private
         */
        var _getUITypeAncestor = function(uiObject, uiType){
            var parentObj = uiObject.getParent();
            if (_.isEmpty(parentObj) // Parent has not been set - newly created item.
                || uiObject.getUIType() == 'xfColumn'){
                return null;
            }
            if (parentObj.getUIType() == uiType){
                return parentObj;
            }
            return _getUITypeAncestor(parentObj, uiType);
        };

        //--------------------------------------------------------------------------------------------------------------

        /**
         * Returns the uiObject in the targetColumn that is positionally
         * closest to the source uiObject.
         * @param positionMap
         * @param sourceObj
         * @param targetParent
         */
        var _getAdjacentObjectInfo = function(positionMap, sourceObj, targetParent){
            var sPos = positionMap[sourceObj.getXfId()];
            // Now iterate through all the children of the target parent
            // and find the closest UI object.
            var children = targetParent.getVisualInfo().children;
            if (children.length == 0){
                var tPos = positionMap[targetParent.getXfId()];
                tPos = tPos == null?{left:0, top:0}:tPos;
                return {
                    uiObject : targetParent,
                    position : tPos,
                    adjacentObj : null
                };
            }
            var minDelta = 1e7;
            var minObj = null;
            var minAdjacentObj = null;
            var minPos = {left:0, top:0};
            for (var i=0; i < children.length; i++){
                var targetObj = children[i];
                // If this is a match card, skip to the next object.
                if (targetObj.getUIType() != 'xfMatch'){
                    var nextTarget = i+1 < children.length ? children[i+1] : null;
                    // If the current target is a file and the next target is a matchcard,
                    // we don't want to separate the two, so we get the next child.
                    if (nextTarget != null && nextTarget.getUIType() == 'xfMatch'){
                        nextTarget = i+2 < children.length ? children[i+2] : null;
                    }
                    var tPos = positionMap[targetObj.getXfId()];

                    if (_isClusterType(targetObj) && targetObj.isExpanded()){
                        var neighbourInfo = _getAdjacentObjectInfo(positionMap, sourceObj, targetObj);
                        // We don't want to insert a uiObject side an expanded cluster hierarchy.
                        // We can only insert above, or below the topmost cluster element.
                        // Determine whether or not the insertion point is closer to the current
                        // target or the next target.
                        if (Math.abs(sPos.top - neighbourInfo.position.top) > Math.abs(sPos.top - tPos.top)){
                            nextTarget = targetObj;
                        }
                        else {
                            // If the insertion point is closer to the next target, update
                            // the position accordingly.
                            tPos = neighbourInfo.position;
                        }
                    }
                    else if (nextTarget == null){
                        if (sPos.top < tPos.top){
                            nextTarget = targetObj;
                        }
                    }
                    var delta = Math.abs(tPos.top-sPos.top);
                    if (delta < minDelta){
                        minDelta = delta;
                        minObj = targetObj;
                        minPos = tPos;
                        minAdjacentObj = nextTarget;
                    }
                }
            }
            return {
                uiObject : minObj,
                position : minPos,
                adjacentObj : minAdjacentObj
            };
        };

        //--------------------------------------------------------------------------------------------------------------

        /**
         * Constructs the corresponding layout request based on type.
         * refInfo and targetInfo are objects with the following members:
         * {
         *      uiObject (XFUIObject - required),
         *      columnObject (XFUIObject - Used by: 'insert'),
         *      columnIndex (Number - Used by: 'insert')
         * }
         *
         * @param type
         * @param workspace
         * @param xfLayoutProvider
         * @param refInfo
         * @param targetInfo
         * @returns {{layoutInfo: {type: *, workspace: *}, layoutProvider: *}}
         * @private
         */
        var _constructLayoutRequest = function(type, workspace, xfLayoutProvider, refInfo, targetInfo){
            var request = {
                layoutInfo : {
                    type : type,
                    workspace : workspace
                },
                layoutProvider : xfLayoutProvider
            };
            if (type == 'insert'){
                request.layoutInfo.refInfo = refInfo;
                request.layoutInfo.targetInfo = targetInfo;
                return request;
            }
            else if (type == 'zoomTo'){
                request.layoutInfo.refInfo = refInfo;
                return request;
            }
            else if (type == 'update'){
                return request;
            }
            // If the request type is unrecognized, report the error.
            console.error('Unrecognized layout request type: ' + type);
            return null;
        };

        //--------------------------------------------------------------------------------------------------------------

        /**
         * Ensures the passed in string is returned as a valid URI
         * by calling encodeURI and logging a warning if something is wrong with it.
         *
         * @param toCheck the URI to check
         * @returns {*} the safe URI
         * @private
         */
        var _getSafeURI = function(toCheck) {
            if(toCheck == null || toCheck.length == 0 || VALID_URI_REGEX.test(toCheck)) {
                return toCheck;
            }
            else {
                console.warn('Cannot parse URI; the URI syntax is invalid: ' + toCheck);
                return encodeURI(toCheck);
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _getUrl = function(uri) {
            var originUrl = document.location.origin + document.location.pathname;
            originUrl = originUrl.substr(0, originUrl.lastIndexOf('/') + 1);

            if (originUrl.charAt(originUrl.length - 1) != '/') {
                originUrl = originUrl + '/';
            }

            return originUrl + uri;
        };

        //--------------------------------------------------------------------------------------------------------------

        var _getChildrenByType = function(target, uiType) {
            var toReturn = [];
            var acceptedTypes = [uiType];

            if (Object.prototype.toString.call( uiType ) === '[object Array]') {
                acceptedTypes = uiType;
            }

            if(acceptedTypes.indexOf(target.getUIType()) != -1) {
                toReturn.push(target);
            }

            if (target.getUIType() === 'xfFile'){
                if (target.getMatchUIObject() != null){
                    toReturn = toReturn.concat(_getChildrenByType(target.getMatchUIObject(), acceptedTypes));
                }
                if (target.getClusterUIObject() != null){
                    toReturn = toReturn.concat(_getChildrenByType(target.getClusterUIObject(), acceptedTypes));
                }
            }
            else {
                var targetChildren = target.getChildren();
                var currChildrenByType;
                for (var i = 0; i < targetChildren.length; i++) {
                    currChildrenByType = _getChildrenByType(targetChildren[i], acceptedTypes);
                    if(currChildrenByType.length > 0) {
                        toReturn.push.apply(toReturn, currChildrenByType);
                    }
                }
            }

            return toReturn;
        };

        //--------------------------------------------------------------------------------------------------------------

        var _getLinkableChildren = function(target) {
            var toReturn = [];

            var isCollapsedCluster = (_isClusterType(target) && !target.isExpanded());

            // If this is an xfCard or a cluster uiObject, use the object itself.
            // Otherwise iterate through its children.
            if (target.getUIType() == 'xfCard' || isCollapsedCluster){
                toReturn.push(target);
            } else {
            	if(target.getUIType() == 'xfFile' && target.hasMatchCard()) {
            		var matchChildren = target.getMatchUIObject().getChildren();
                    for (var i = 0; i < matchChildren.length; i++) {
                        currChildrenByType = _getLinkableChildren(matchChildren[i]);
                        if(currChildrenByType.length > 0) {
                            toReturn.push.apply(toReturn, currChildrenByType);
                        }
                    }
            	}
            }

            if (!isCollapsedCluster){
                var targetChildren = target.getChildren();
                var currChildrenByType;
                for (var i = 0; i < targetChildren.length; i++) {
                    currChildrenByType = _getLinkableChildren(targetChildren[i]);
                    if(currChildrenByType.length > 0) {
                        toReturn.push.apply(toReturn, currChildrenByType);
                    }
                }
            }

            return toReturn;
        };

        //--------------------------------------------------------------------------------------------------------------

        var _stringToBoolean = function(string) {
            switch(string.toLowerCase()) {
                case "false": case "no": case "0": case "" : return false;
                default: return true;
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _clearMouseListeners = function(element, selectedListeners){
            var listeners = _.isEmpty(selectedListeners)?['click', 'dblclick','mouseenter', 'mouseexit', 'mousedown', 'mouseover', 'hover']:selectedListeners;
            for (var i=0; i < listeners.length; i++){
                element.unbind(listeners[i]);
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _getTopLevelEntity = function(uiObj){
            if (uiObj == null ||
                typeof uiObj.getUIType !== 'function' ||
                uiObj.getUIType() == 'xfWorkspace' ||
                uiObj.getUIType() == 'xfColumn' ||
                uiObj.getUIType() == 'xfLink'
            ) {
                return null;
            }

            if (uiObj.getParent().getUIType() == 'xfColumn') {
                return uiObj;
            } else {
                return _getTopLevelEntity(uiObj.getParent());
            }
        };

        //--------------------------------------------------------------------------------------------------------------

        var _isFileCluster = function(uiObject){
          return (uiObject.getUIType() == 'xfMutableCluster');
        };

        //--------------------------------------------------------------------------------------------------------------

        var _isClusterType = function(uiObject) {
            return (uiObject.getUIType() == 'xfImmutableCluster' || uiObject.getUIType() == 'xfMutableCluster');
        };

        //--------------------------------------------------------------------------------------------------------------
        return {
            isUITypeDescendant : _isUITypeDescendant,
            getUITypeAncestor : _getUITypeAncestor,
            getAdjacentObjectInfo : _getAdjacentObjectInfo,
            constructLayoutRequest : _constructLayoutRequest,
            getSafeURI : _getSafeURI,
            getUrl : _getUrl,
            getChildrenByType : _getChildrenByType,
            getLinkableChildren : _getLinkableChildren,
            stringToBoolean : _stringToBoolean,
            clearMouseListeners : _clearMouseListeners,
            getTopLevelEntity : _getTopLevelEntity,
            isFileCluster : _isFileCluster,
            isClusterType : _isClusterType
        };
    }
);