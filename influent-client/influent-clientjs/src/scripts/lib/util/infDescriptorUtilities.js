/**
 * Copyright © 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted™, formerly Oculus Info Inc.
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
define([], function() {

		return {
			getIdDescriptors: function(dataIds, searchParams, key) {
				var descriptors = {};
				var matchDescriptor;
				var matchKey;
				var prop;


				var descriptorProps = searchParams.getPropertiesByTags('ID');
				if (descriptorProps.length > 0) {
					prop = descriptorProps[0];
					matchKey = key ? key : prop.key;
				} else {
					return {};
				}

				for (var i = 0; i < dataIds.length; i++) {
					var typeMappings = [];
					typeMappings.push({
						type: dataIds[i].type,
						memberKey: prop.typeMappings[dataIds[i].type]
					});

					matchDescriptor = {
						key : matchKey,
						range : {
							value: dataIds[i].dataId,
							type: prop.propertyType
						},
						typeMappings : typeMappings
					};
					if (!descriptors.hasOwnProperty(dataIds[i].type)) {
						descriptors[dataIds[i].type] = [];
					}
					descriptors[dataIds[i].type].push(matchDescriptor);

				}

				return descriptors;
			},

			getQueryFromDescriptors : function(descriptors, searchType, searchParams) {
				var query = ''.concat('SEARCHTYPE:', searchType, ' ');
				var isMultitype = searchParams[searchType].getNumTypes() > 1;
				for (var type in descriptors) {
					if (descriptors.hasOwnProperty(type)) {
						for (var i = 0; i < descriptors[type].length; i++) {
							var desc = descriptors[type][i];
							query += desc.key + ':"' + desc.range.value;
							if (isMultitype) {
								query += '" TYPE:' + type + ' ';
							}
						}
					}
				}
				return query.trim();
			},

			/**
			 * We expect query to be in the format:
			 * SEARCHTYPE:searchtype ((key:keyvalue...) type:typevalue)...
			 */
			getDescriptorsFromQuery : function(query, searchParams) {
				var searchTypeRE = /SEARCHTYPE:(entities|links)( |$)/;
				var searchType = searchTypeRE.exec(query);
				if (searchType.length < 2) {
					aperture.log.error('getDescriptorsFromQuery: no search type specified');
					return {};
				}
				searchType = searchType[1];
				query = query.replace(searchTypeRE, '');

				var typeRE = /TYPE:[^:\s]+\s?/g;
				var typeTerms = query.match(typeRE);

				if (!typeTerms && searchParams[searchType].getNumTypes() === 1) {
					typeTerms = ['TYPE:' + Object.keys(searchParams[searchType].getTypeMap())[0]];
				}

				var descriptorSplit = query.split(typeRE).filter(function(elem) {return elem.length !== 0;});

				if (typeTerms.length !== descriptorSplit.length) {
					aperture.log.error('getDescriptorsFromQuery: has unmatched descriptors/typeMapping');
					return {};
				}

				var descriptorProps = searchParams[searchType].getPropertiesByTags('ID');
				var prop;
				if (descriptorProps.length > 0) {
					prop = descriptorProps[0];
				} else {
					return;
				}

				var matchDescriptorMap = {};
				for (var i = 0; i < descriptorSplit.length; i++) {
					var typeMappings = [];
					var type = typeTerms[i].trim().replace(/TYPE:/, '');
					typeMappings.push({
						type : type,
						memberKey : prop.typeMappings[type]
					});

					var terms = descriptorSplit[i].trim().split(' ');
					var matchDescriptors = [];
					for (var j = 0; j < terms.length; j++) {
						var reg = /([^:\s]+):(\"([^\"]*)\"|[^:]*)( |$)/gi;
						var res = reg.exec(terms[j]);
						var key = res[1];
						var value =  res[3] !== undefined ? res[3] : res[2];
						matchDescriptors.push({
							key : key,
							range : {
								value : value,
								type : prop.propertyType
							},
							typeMappings : typeMappings
						});
					}
					if (matchDescriptorMap.hasOwnProperty(type)) {
						matchDescriptorMap[type] = matchDescriptorMap[type].concat(matchDescriptors);
					} else {
						matchDescriptorMap[type] = matchDescriptors;
					}

				}

				return {
					searchType : searchType,
					matchDescriptorMap : matchDescriptorMap
				};
			}
		};
	}
);
