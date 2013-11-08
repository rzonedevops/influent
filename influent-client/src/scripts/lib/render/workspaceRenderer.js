define(['jquery', 'lib/channels', 'lib/render/cardRenderer', 'lib/render/columnRenderer'],
    function($, chan, cardRenderer, columnRenderer) {

        var workspaceRenderer = {};

        //------------------------------------------------------------------------------------------------------------------

        var _renderDefaults = {
            getWindowCenterX : function(childObjects){
                var centerBase =  $('#cards').width()/2-cardRenderer.getRenderDefaults().CARD_WIDTH/2;      // Subtract half of card size,

                if(childObjects.length > 1) {
                    centerBase = Math.max(0, centerBase - (childObjects.length - 1)*_columnDefaults.COLUMN_DISTANCE/2);
                }

                return centerBase;
            }
        };

        //------------------------------------------------------------------------------------------------------------------

        var _columnDefaults = columnRenderer.getRenderDefaults();

        //------------------------------------------------------------------------------------------------------------------

        var processColumns = function(childObjects, parentCanvas){
            for (var i=0; i < childObjects.length; i++){
                var visualInfo = childObjects[i].getVisualInfo();
                var element = columnRenderer.createElement(visualInfo);
                element.css('left', _renderDefaults.getWindowCenterX(childObjects) + _columnDefaults.COLUMN_DISTANCE*i);
                parentCanvas.append(element);
            }
        };

        //------------------------------------------------------------------------------------------------------------------


        workspaceRenderer.createElement = function(visualInfo){
            var canvas = $('#' + visualInfo.xfId);
            if (canvas.length > 0){
                canvas.empty();
            }
            else {
                canvas = $('<div/>');
                canvas.attr('id', visualInfo.xfId);
            }

            processColumns(visualInfo.children, canvas);

            return canvas;
        };

        //------------------------------------------------------------------------------------------------------------------

        workspaceRenderer.getRenderDefaults = function(){
            return _.clone(_renderDefaults);
        };

        return workspaceRenderer;
    }
);