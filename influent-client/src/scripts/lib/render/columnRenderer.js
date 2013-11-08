define(['jquery', 'lib/channels', 'lib/util/xfUtil', 'lib/render/cardRenderer', 'lib/render/fileRenderer',
    'lib/render/clusterRenderer', 'lib/ui/xfModalDialog'],
    function($, chan, xfUtil, cardRenderer, fileRenderer, clusterRenderer, xfModalDialog) {
	var columnRenderer = {};

    var _cardDefaults = cardRenderer.getRenderDefaults();
    var _renderDefaults = {
        COLUMN_DISTANCE : 300 // 300px between columns
    };
    var _processChildren = function(childObjects, parentCanvas){
        for (var i=0; i < childObjects.length; i++){
            var visualInfo = childObjects[i].getVisualInfo();
            var element = undefined;
            switch(childObjects[i].getUIType()){
                case 'xfFile' : {
                    element = fileRenderer.createElement(visualInfo);
                    break;
                }
                case 'xfImmutableCluster' :
                case 'xfMutableCluster' : {
                    element = clusterRenderer.createElement(visualInfo);
                    element.css('left', _cardDefaults.CARD_LEFT);
                    break;
                }
                case 'xfCard' : {
                    element = cardRenderer.createElement(visualInfo);
                    element.css('left', _cardDefaults.CARD_LEFT);
                    break;
                }
                default : {
                    console.error('Attempted to add an unsupported UIObject type to column: ' + childObjects[i].getUIType());
                }
            }
            if (element){
                parentCanvas.append(element);
            }
        }
    };

	columnRenderer.createElement = function(visualInfo){
		var canvas = $('#' + visualInfo.xfId),
			container;
		
		// exists? empty it
        if (canvas.length > 0) {
            container = $('.columnContainer', canvas);
            container.empty();
            // Remove all listeners.
            xfUtil.clearMouseListeners(canvas, ['click', 'hover']);
        // else construct it
        } else {
            canvas = $('<div class="column"/>');
            canvas.attr('id', visualInfo.xfId);
            
            // create the column header
    		var header = $('<div class="columnHeader"/>').appendTo(canvas).css('display', 'none');
    		
    		// create the content container
    		container = $('<div class="columnContainer"/>').appendTo(canvas);
    		
            var fileDiv = $('<div class="new-file-button">').appendTo(header);
            fileDiv.click(function() {
                aperture.pubsub.publish(chan.CREATE_FILE_REQUEST, {xfId : visualInfo.xfId, isColumn: true});
                return false;
            });

            var fileImg = $('<img/>');
            fileImg.attr('src' , xfUtil.getUrl('img/new_file.png'));

            fileDiv.append(fileImg);
            fileDiv.css('float', 'right');
            fileDiv.addClass('cardToolbarItem');

            var cleanColumnDiv = $('<div class="clean-column-button">').appendTo(header);
            cleanColumnDiv.click(function() {
                xfModalDialog.createInstance({
                    title: "Clear Column?",
                    contents: "Clear all unfiled and unpinned cards? Everything but pinned cards, file folders and match results will be removed.",
                    buttons: {
                        "Clear" : function() {
                            aperture.pubsub.publish(chan.CLEAN_COLUMN_REQUEST, {xfId : visualInfo.xfId});
                        },
                        Cancel : function() {}
                    }
                });
                return false;
            });

            var cleanColumnImg = $('<img/>');
            
            // icon courtesy of http://p.yusukekamiyamane.com 
            cleanColumnImg.attr('src' , xfUtil.getUrl('img/clean_column.png'));

            cleanColumnDiv.append(cleanColumnImg);
            cleanColumnDiv.css('float', 'right');
            cleanColumnDiv.addClass('cardToolbarItem');

            function showHeader() {
            	if ($('.columnHeader', canvas).css('display') === 'none') {
            		
            		// hide all, just in case we didn't get a leave event
                	$('.columnHeader').css('display', 'none');
                	
                	// show just me
                	$('.columnHeader', canvas).css('display', '');
            	}
            }
            
            // show when hovering or when clicked.
            canvas.click(showHeader);
            canvas.mousemove(showHeader);
            canvas.mouseleave(function() {
            	$('.columnHeader', canvas).css('display', 'none');
            });
        }
        
        _processChildren(visualInfo.children, container);
        
        return canvas;
	};
    columnRenderer.getRenderDefaults = function(){
        return _.clone(_renderDefaults);
    };
	return columnRenderer;
});