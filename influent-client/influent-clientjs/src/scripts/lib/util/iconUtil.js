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
		'modules/infWorkspace',
		'modules/infRest',
		'lib/models/xfClusterBase',
		'lib/models/xfCard',
		'lib/util/xfUtil',
		'lib/util/infTagUtilities',
		'lib/constants'
	],
	function(
		appChannel,
		infWorkspace,
		infRest,
		xfClusterBase,
		xfCard,
		xfUtil,
		tagUtilities,
		constants
	) {

		//--------------------------------------------------------------------------------------------------------------

		var _getClusterIcons = function (cluster, clusterCount) {

			var icons = [];

			var iconClassOrder = aperture.config.get()['influent.config'].iconOrder;
			var iconClassMap = aperture.config.get()['influent.config'].iconMap;
			var iconClass = null;

			for(var i = 0; i < iconClassOrder.length; i++) {
				iconClass = iconClassOrder[i];
				if (iconClassMap[iconClass]) {
					var properties = tagUtilities.getPropertiesByTag(cluster, iconClass);

					for (var pi=0, pn= properties.length; pi<pn; pi++) {
						var property = properties[pi];
						var iconDist = property.range;

						if (iconDist) {
							var iconMap = iconClassMap[iconClass].map;

							if(iconMap != null) {
								var friendlyName = property.friendlyText;
								var newDistIcons = _generateDistIcons(iconClass, property.key, iconMap, iconDist, clusterCount, friendlyName);
								for(var j = 0; j < newDistIcons.length; j++) {
									icons.push(newDistIcons[j]);
								}
							}
							else {
								aperture.log.error('Unable to render icons of type ' + iconClass + '; misconfiguration in config.js.');
							}
						}
					}
				} else {
					aperture.log.error('icon order references a type that doesnt exist');
				}
			}

			return icons;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _generateDistIcons = function (iconClass, propertyKey, iconMap, iconDist, clusterCount, friendlyName) {
			var toReturn = [];
			var count = 0;
			var nextIcon = null;
			var value = null;
			var d = iconDist.distribution;
			var i;
			var freq;

			// safety check
			if ($.isArray(d) === false) {
				return toReturn;
			}

			for (i=0; i< d.length; i++) {
				freq = d[i];

				value = freq.range;
				count = freq.frequency;

				nextIcon = iconMap(propertyKey, value);

				if (nextIcon) {
					toReturn.push(
						{
							type: iconClass,
							imgUrl: nextIcon.url || aperture.palette.icon(nextIcon.icon),
							title: (nextIcon.title || '') + ' ('+ count+ ')',
							score : count / clusterCount,
							friendlyName: friendlyName
						}
					);
				}
			}

			toReturn.sort(function(a, b) {return b['score'] - a['score'];});
			return toReturn;
		};

		//--------------------------------------------------------------------------------------------------------------

		var _getIconForProperty = function(property, attrs) {

			var iconClassMap = aperture.config.get()['influent.config'].iconMap;

			if (!property) {
				return;
			}

			var icon = null;

			aperture.util.forEachUntil(property.tags, function(tag) {
				if (iconClassMap[tag]) {
					var value = property.value;
					if (property.range) {
						var d= property.range.distribution;
						if (!d) {
							d = property.range.values;
							if (d && d.length === 1) {
								value = d[0];
							} else {
								return false; // if more than one value, show none.
							}
						} else if (d.length === 1) {
							value = d[0].range;
						} else { // if more than one value, show none.
							return false;
						}
					}

					var iconObject = iconClassMap[tag].map(property.key, property.value);
					if (iconObject) {
						var url = iconObject.url;
						if (iconObject.icon) {
							var sized = aperture.util.extend(aperture.util.viewOf(iconObject.icon), attrs);

							url = aperture.palette.icon(sized);
						}

						icon = {
							imgUrl : url,
							title : iconObject.title,
							type : tag
						};

						return true;
					}
				}
			});

			return icon;
		};

		//--------------------------------------------------------------------------------------------------------------

		return {
			getClusterIcons: _getClusterIcons,
			getIconForProperty: _getIconForProperty
		};
	}
);

