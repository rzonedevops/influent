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
package influent.server.dataaccess;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import influent.idl.*;
import influent.idlhelper.DataPropertyDescriptorHelper;
import influent.idlhelper.PropertyMatchDescriptorHelper;
import influent.server.sql.*;
import influent.server.utilities.SQLConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Helper class to help create a SQL statement from a list of {@link FL_PropertyMatchDescriptor}s.
 * It's expected that this class will be extended in order to supply any special fields.
 *
 * @author cregnier
 *
 */
public class SearchSQLHelper {


	private static final Logger s_logger = LoggerFactory.getLogger(SearchSQLHelper.class);

	private static final int ID_BATCH_SIZE = 30;


	//----------------------------------------------------------------------

	protected final SQLBuilder _sqlBuilder;
	private final SQLConnectionPool _connectionPool;
	private final DataNamespaceHandler _namespaceHandler;
	private final FL_PropertyDescriptors _propertyDescriptors;
	private boolean _isLink;
	//----------------------------------------------------------------------



	public SearchSQLHelper(
			SQLBuilder sqlBuilder,
			SQLConnectionPool connectionPool,
			DataNamespaceHandler namespaceHandler,
			FL_PropertyDescriptors propertyDescriptors
	) {
		_sqlBuilder = sqlBuilder;
		_connectionPool = connectionPool;
		_namespaceHandler = namespaceHandler;
		_propertyDescriptors = propertyDescriptors;
	}

	public SearchSQLHelper isLinkHelper(boolean isLinkHelper) {
		_isLink= isLinkHelper;
		
		return this;
	}
	public boolean isLinkHelper() {
		return _isLink;
	}

	protected Logger getLogger() {
		return s_logger;
	}


	protected SQLBuilder getSQLBuilder() {
		return _sqlBuilder;
	}

	private String _mapKey(String key, String type) {
		return DataPropertyDescriptorHelper.mapKey(key, _propertyDescriptors.getProperties(), type);
	}

	private List<FL_OrderBy> _mapOrderBy(List<FL_OrderBy> orderBy, String type) {
		return DataPropertyDescriptorHelper.mapOrderBy(orderBy, _propertyDescriptors.getProperties(), type);
	}


	/**
	 * Builds a {@link SQLFilter} for the given term that's treated as the given special field.
	 * @param term
	 * @param type
	 * @return
	 */
	public SQLFilter buildSpecialTermFilter(FL_PropertyMatchDescriptor term, String type) {
		SQLFilter filter = null;

		// Only handle ENTITY terms. ENTITY and LINKED pairs are processed in _buildSpecialIdPairs
		if (term.getKey().equals(FL_RequiredPropertyKey.ENTITY.name())) {
			FL_PropertyMatchDescriptor fromTerm = FL_PropertyMatchDescriptor.newBuilder(term)
				.setKey( FL_RequiredPropertyKey.FROM.name())
				.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.FROM.name(), type)).build()))
				.build();
			FL_PropertyMatchDescriptor toTerm = FL_PropertyMatchDescriptor.newBuilder(term)
				.setKey( FL_RequiredPropertyKey.TO.name())
				.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.TO.name(), type)).build()))
				.build();

			filter = _sqlBuilder.or(_buildFilterColumn(fromTerm, type), _buildFilterColumn(toTerm, type));
		} else if (term.getKey().equals(FL_RequiredPropertyKey.LINKED.name())) {
			FL_PropertyMatchDescriptor transIdTerm = FL_PropertyMatchDescriptor.newBuilder(term)
					.setKey(FL_RequiredPropertyKey.LINKED.name())
					.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(FL_RequiredPropertyKey.LINKED.name()).build()))
					.build();
			filter = _sqlBuilder.group(_buildFilterColumn(transIdTerm, type));
		}

		return filter;
	}

	/**
	 * Determines if the given field is one of the IDs for the current data set.
	 * @param field
	 * @return
	 */
	public boolean isIdField(String field) {
		if (_isLink) {
			return  field.equals(FL_RequiredPropertyKey.FROM.name()) ||
					field.equals(FL_RequiredPropertyKey.TO.name()) ||
					field.equals(FL_RequiredPropertyKey.ENTITY.name()) ||
					field.equals(FL_RequiredPropertyKey.LINKED.name());
		}
		
		return field.equals(FL_RequiredPropertyKey.ENTITY.name());
	}

	SQLFilterColumn _buildFilterColumn(FL_PropertyMatchDescriptor term, String type) {

		String fieldName = _sqlBuilder.escape(PropertyMatchDescriptorHelper.getFieldname(term, type));
		
		SQLFilterColumn filter = _sqlBuilder.filter();
		filter.column(fieldName);

		Object range = term.getRange();
		if (range instanceof FL_SingletonRange) {
			FL_SingletonRange srange = (FL_SingletonRange)range;
			
			String val = formatColumnValue(srange.getType(), srange.getValue().toString());

			boolean hasWildcard = val.matches(".*[\\*\\?].*");

			if (hasWildcard) {
				//use a LIKE query
				val = val.replace('*', '%');
				val = val.replace('?', '_');
				filter.like(val);

				if (!term.getInclude())
					filter.not();
			}
			else {
				//use an EQ query
				if (term.getInclude()) {
					filter.eq(val);
				}
				else {
					filter.notEq(val);
				}
			}
		}
		else if (range instanceof FL_ListRange) {
			FL_ListRange lrange = (FL_ListRange)range;
			List<Object> values = ((FL_ListRange)range).getValues();
			List<Object> values2 = new ArrayList<Object>();

			for (Object obj : values) {
				values2.add(formatColumnValue(lrange.getType(), (String)obj));
			}

			filter.in(values2);

			if (!term.getInclude())
				filter.not();
		}
		else if (range instanceof FL_BoundedRange) {
			FL_BoundedRange brange = (FL_BoundedRange)range;
			String start = formatColumnValue(brange.getType(), brange.getStart().toString());
			String end = formatColumnValue(brange.getType(), brange.getEnd().toString());
			if (start.contains("*")) {
				filter.lessThanEq(end);
			} else if (end.contains("*")) {
				filter.greaterThanEq(start);
			} else {
				filter.between(start, end);
			}

			if (!term.getInclude())
				filter.not();
		}
		else {
			throw new UnsupportedOperationException("Trying to create a filter for term " + term.getKey() + " of an unsupported range type " + range.getClass());
		}

		return filter;
	}


	/**
	 * General method to build a {@link SQLFilter} from a {@link FL_PropertyMatchDescriptor}.
	 * This can handle {@link FL_RequiredPropertyKey}s and ID related fields along with normal fields.
	 * @param term
	 * @return
	 */
	public SQLFilter buildTermFilter(FL_PropertyMatchDescriptor term, String type) {

		if (isSpecialDescriptor(term)) {
			//got a special field so handle that separately
			return buildSpecialTermFilter(term, type);
		}
		else {
			//handle normal fields in a general way
			SQLFilterColumn filter = _buildFilterColumn(term, type);

			return filter;
		}
	}

	/**
	 * Builds all the filters for each term in the list of terms
	 */
	public SQLFilter buildListOfFilters(Iterable<FL_PropertyMatchDescriptor> terms, String type, SQLFilterGroup group) {

		boolean hasSpecialIdPairs = _buildSpecialIdPairs(terms, type, group);

		for (FL_PropertyMatchDescriptor term : terms) {

			// Skip special id terms if we've already handled them elsewhere
			if (hasSpecialIdPairs &&
				(term.getKey().equals(FL_RequiredPropertyKey.ENTITY.name()) ||
				term.getKey().equals(FL_RequiredPropertyKey.LINKED.name()))) {
				continue;
			}

			group.addFilter(buildTermFilter(term, type));
		}
		return group;
	}
	
	/**
	 * Returns true if the descriptor has a special {@link FL_RequiredPropertyKey}
	 * @param property
	 * @return
	 */
	protected boolean isSpecialDescriptor(FL_PropertyMatchDescriptor property) {
		return isSpecialDescriptor(property.getKey());
	}
	protected boolean isSpecialDescriptor(String key) {
		return _isLink && (
			key.equals(FL_RequiredPropertyKey.ENTITY.name()) ||
			key.equals(FL_RequiredPropertyKey.LINKED.name())
		);
	}

	/**
	 * Top level method that will pull out all optional and required terms
	 * separately so they can be turned into separate AND/OR filters.
	 * 
	 * @param terms
	 * @return
	 */
	public SQLFilter buildTermsFilter(List<FL_PropertyMatchDescriptor> terms, String type) {
		//split the list into two, one list with constraints == OPTIONAL_EQUALS for OR clauses, and all others which will be AND'ed
		Iterable<FL_PropertyMatchDescriptor> orTerms = Iterables.filter(terms, new Predicate<FL_PropertyMatchDescriptor>() {
			@Override
			public boolean apply(FL_PropertyMatchDescriptor input) {
				return FL_Constraint.OPTIONAL_EQUALS.equals(input.getConstraint()) || FL_Constraint.FUZZY_PARTIAL_OPTIONAL.equals(input.getConstraint());
			}
		});
		Iterable<FL_PropertyMatchDescriptor> andTerms = Iterables.filter(terms, new Predicate<FL_PropertyMatchDescriptor>() {
			@Override
			public boolean apply(FL_PropertyMatchDescriptor input) {
				return FL_Constraint.REQUIRED_EQUALS.equals(input.getConstraint()) || FL_Constraint.FUZZY_REQUIRED.equals(input.getConstraint());
			}
		});

		SQLFilterGroup filter = _sqlBuilder.and();
		filter.addFilter(buildListOfFilters(orTerms, type, _sqlBuilder.or()));
		filter.addFilter(buildListOfFilters(andTerms, type, _sqlBuilder.and()));
		return filter;
	}
	
	/***
	 * Fetch the specified columns per matching top 1000 rows in database using
	 * filter term criteria.
	 *
	 * @param table
	 * @param columns
	 * @param terms
	 * @param type
	 * @return
	 */
	public List<Map<String, Object>> fetchColumnsForTerms(
			String table,
			List<String> columns,
			List<FL_PropertyMatchDescriptor> terms,
			String type) {

		return fetchColumnsForTerms(table, columns, terms, type, null);
	}

	/***
	 * Fetch the specified columns per matching top 1000 rows in database using
	 * filter term criteria.
	 *
	 * @param table
	 * @param columns
	 * @param terms
	 * @param type
	 * @param orderBy
	 * @return
	 */
	public List<Map<String, Object>> fetchColumnsForTerms(
			String table,
			List<String> columns,
			List<FL_PropertyMatchDescriptor> terms,
			String type,
			List<FL_OrderBy> orderBy
	) {

		Connection connection = null;
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		if (terms.size() == 0) {
			return results;
		}

		try {
			List<List<FL_PropertyMatchDescriptor>> groupedFilterTerms = _groupFilterTerms(terms);

			SQLSelect query = _sqlBuilder.select();

			//uses top 1000 to be quicker, but doesn't allow proper pagination
			query.top(1000);

			for (String column : columns) {
				query.column(_sqlBuilder.escape(column));
			}

			// Create top level query
			SQLFrom fromClause = _sqlBuilder.from();
			SQLSelect fromQuery = _createQuery(type, table, columns, groupedFilterTerms);
			fromClause.fromQuery(fromQuery).as("f");
			query.from(fromClause);

			// set the sort order if specified
			if (orderBy != null) {
				orderBy = _mapOrderBy(orderBy, type);
				
				for (FL_OrderBy ob : orderBy) {

					if (ob.getPropertyKey().equals(FL_ReservedPropertyKey.MATCH.name())) {
						continue;
					}

					query.orderBy(_sqlBuilder.escape(ob.getPropertyKey()), ob.getAscending());
				}
			}
			
			// Build the sql string
			String sql = query.build();

			connection = _connectionPool.getConnection();
			Statement stmt = connection.createStatement();

			s_logger.trace("Executing sql statement: " + sql);

			if (stmt.execute(sql)) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {

					// Map the results against the requested columns
					Map<String, Object> resultMap = new HashMap<String, Object>();
					for (String column : columns) {
						String columnName = _sqlBuilder.unescape(column);
						Object obj = rs.getObject(columnName);

						resultMap.put(column, obj);
					}
					results.add(resultMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				s_logger.trace("Error closing connection ", e);
			}
		}

		return results;
	}
	
	// Create a sub query around batched terms
	private SQLSelect _createSubQuery(String type, String table, List<String> columns, List<FL_PropertyMatchDescriptor> terms) {
		SQLSelect groupQuery = _sqlBuilder.select();
		
		for (String column : columns) {
			groupQuery.column(_sqlBuilder.escape(column));
		}
		
		groupQuery.from(table, "f")
				.where(buildTermsFilter(terms, type));
		
		return groupQuery;
	}

	// Create a top level query containing subqueries
	private SQLSelect _createQuery(String type, String table, List<String> columns, List<List<FL_PropertyMatchDescriptor>> groupedTerms) {
		// create a search query by type
		SQLSelect typeQuery = _sqlBuilder.select();
		
		for (String column : columns) {
			typeQuery.column(_sqlBuilder.escape(column));
		}
		
		// construct a sub-query to select results from - this will be a union of grouped term queries
		SQLFrom fromClause = _sqlBuilder.from();
		SQLSelect fromQuery = _createSubQuery(type, table, columns, groupedTerms.get(0));
		fromClause.fromQuery(fromQuery).as("f");
		
		// union any other group term queries together
		for (int i=1; i < groupedTerms.size(); i++) {
			SQLSelect query = _createSubQuery(type, table, columns, groupedTerms.get(i));
			fromQuery.union(query);
			fromQuery = query;
		}
		
		typeQuery.from(fromClause);
		
		return typeQuery;
	}

	private boolean _buildSpecialIdPairs(Iterable<FL_PropertyMatchDescriptor> terms, String type, SQLFilterGroup group) {
		FL_PropertyMatchDescriptor entityIDTerm = null;
		FL_PropertyMatchDescriptor linkedIDTerm = null;

		boolean hasMultipleEntityIDs = false;
		boolean hasMultipleLinkedIDs = false;

		// Check for ENTITY and LINKED pairs
		for (FL_PropertyMatchDescriptor term : terms) {
			if (term.getKey().equals(FL_RequiredPropertyKey.ENTITY.name())) {
				if (entityIDTerm != null) {
					hasMultipleEntityIDs = true;
				} else {
					entityIDTerm = term;
				}
			} else if (term.getKey().equals(FL_RequiredPropertyKey.LINKED.name())) {
				if (linkedIDTerm != null) {
					hasMultipleLinkedIDs = true;
				} else {
					linkedIDTerm = term;
				}
			}
		}

		// TODO: Handle cases where multiple ENTITY or LINKED terms have been specified. Currently throws an error
		if (hasMultipleEntityIDs && hasMultipleLinkedIDs) {
			s_logger.error("Multiple ENTITY or LINKED terms specified");
			return false;
		}

		// If both are defined, build a special group term such that ENTITY:A,B LINKED:C,D
		// Becomes filter (_from IN(A,B) AND _to IN(C,D)) OR (_from IN(C,D) AND _to IN(A,B))
		if (entityIDTerm != null && linkedIDTerm != null) {
			FL_PropertyMatchDescriptor fromEntityIDTerm = FL_PropertyMatchDescriptor.newBuilder(entityIDTerm)
					.setKey(FL_RequiredPropertyKey.FROM.name())
					.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.FROM.name(), type)).build()))
					.build();
			FL_PropertyMatchDescriptor toLinkedIDTerm = FL_PropertyMatchDescriptor.newBuilder(linkedIDTerm)
					.setKey(FL_RequiredPropertyKey.TO.name())
					.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.TO.name(), type)).build()))
					.build();

			FL_PropertyMatchDescriptor fromLinkedIDTerm = FL_PropertyMatchDescriptor.newBuilder(linkedIDTerm)
					.setKey(FL_RequiredPropertyKey.FROM.name())
					.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.FROM.name(), type)).build()))
					.build();
			FL_PropertyMatchDescriptor toEntityIDTerm = FL_PropertyMatchDescriptor.newBuilder(entityIDTerm)
					.setKey(FL_RequiredPropertyKey.TO.name())
					.setTypeMappings(Collections.singletonList(FL_TypeMapping.newBuilder().setType(type).setMemberKey(_mapKey(FL_RequiredPropertyKey.TO.name(), type)).build()))
					.build();


			SQLFilter entityIDFilter = _sqlBuilder.and(_buildFilterColumn(fromEntityIDTerm, type), _buildFilterColumn(toLinkedIDTerm, type));
			SQLFilter linkedIDFilter = _sqlBuilder.and(_buildFilterColumn(fromLinkedIDTerm, type), _buildFilterColumn(toEntityIDTerm, type));
			SQLFilter both = _sqlBuilder.or(entityIDFilter, linkedIDFilter);

			group.addFilter(both);

			return true;
		}

		return false;
	}

	// Group filter terms into batches for faster execution
	private List<List<FL_PropertyMatchDescriptor>> _groupFilterTerms(List<FL_PropertyMatchDescriptor> filterTerms) {
		List<List<FL_PropertyMatchDescriptor>> groupedFilterTerms = new LinkedList<List<FL_PropertyMatchDescriptor>>();

		List<FL_PropertyMatchDescriptor> generalFilterTerms = new LinkedList<FL_PropertyMatchDescriptor>();

		// Map of id term lists by id term type
		Map<String,List<FL_PropertyMatchDescriptor>> idFilterTermsMap = new HashMap<String, List<FL_PropertyMatchDescriptor>>();

		// separate out id filters and general term filters
		for (FL_PropertyMatchDescriptor filterTerm : filterTerms) {
			String key = filterTerm.getKey();
			if (isIdField(key)) {
				List<FL_PropertyMatchDescriptor> pmd = idFilterTermsMap.get(key);
				if (pmd == null) {
					pmd = new ArrayList<FL_PropertyMatchDescriptor>();
				}
				pmd.add(filterTerm);
				idFilterTermsMap.put(key, pmd);
			} else {
				generalFilterTerms.add(filterTerm);
			}
		}

		// Find the largest id-list contributor to the query. This will be the one that gets batched.
		// All other terms (including id terms) will be grouped in their entirety with this term.
		String largestIdField = null;
		int largestIdFieldCount = 0;
		for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : idFilterTermsMap.entrySet()) {
			int rangeTotal = 0;
			for (int i=0; i < entry.getValue().size(); i++) {
				// collect up all the filter ids for processing into batches
				FL_PropertyMatchDescriptor pmd = entry.getValue().get(i);
				Object range = pmd.getRange();
				if (range instanceof FL_SingletonRange) {
					rangeTotal += 1;
				} else if (range instanceof FL_ListRange) {
					rangeTotal += ((FL_ListRange) range).getValues().size();
				}
			}

			if (largestIdField == null || rangeTotal > largestIdFieldCount) {
				largestIdFieldCount = rangeTotal;
				largestIdField = entry.getKey();
			}
		}

		// create the initial batch for the term
		List<FL_PropertyMatchDescriptor> batch = new LinkedList<FL_PropertyMatchDescriptor>();
		List<Object> batchFilterIds = new ArrayList<Object>();

		// Add general terms
		batch.addAll(generalFilterTerms);
		// Add unbatched id terms
		for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : idFilterTermsMap.entrySet()) {
			if (!entry.getKey().equals(largestIdField)) {
				batch.addAll(entry.getValue());
			}
		}
		groupedFilterTerms.add(batch);

		if (largestIdField != null) {
			List<FL_PropertyMatchDescriptor> idFilterTerms = idFilterTermsMap.get(largestIdField);//entry.getValue();
			for (int i = 0; i < idFilterTerms.size(); i++) {
				// collect up all the filter ids for processing into batches
				FL_PropertyMatchDescriptor pmd = idFilterTerms.get(i);
				Object range = pmd.getRange();
				List<Object> filterIds = null;
				if (range instanceof FL_SingletonRange) {
					filterIds = Collections.singletonList(((FL_SingletonRange) range).getValue());
				} else if (range instanceof FL_ListRange) {
					filterIds = ((FL_ListRange) range).getValues();
				}

				if (filterIds != null) {
					for (int j = 0; j < filterIds.size(); j++) {
						batchFilterIds.add(filterIds.get(j));

						boolean lastItem = (i == idFilterTerms.size() - 1) && (j == filterIds.size() - 1);

						if (batchFilterIds.size() >= ID_BATCH_SIZE || lastItem) {
							// we have filled up the batch - create a property match descriptor for this batch of filter ids
							FL_PropertyMatchDescriptor batchedPmd = FL_PropertyMatchDescriptor.newBuilder(pmd)
									.setRange(FL_ListRange.newBuilder().setType(FL_PropertyType.STRING).setValues(batchFilterIds).build())
									.build();
							batch.add(batchedPmd);

							if (!lastItem) {
								// create a new batch
								batch = new LinkedList<FL_PropertyMatchDescriptor>();
								batchFilterIds = new ArrayList<Object>();

								// Add general terms
								batch.addAll(generalFilterTerms);
								// Add unbatched id terms
								for (Map.Entry<String, List<FL_PropertyMatchDescriptor>> entry : idFilterTermsMap.entrySet()) {
									if (!entry.getKey().equals(largestIdField)) {
										batch.addAll(entry.getValue());
									}
								}
								groupedFilterTerms.add(batch);
							}
						}
					}
				}
			}
		}

		if (groupedFilterTerms.isEmpty()) groupedFilterTerms.add(generalFilterTerms);

		return groupedFilterTerms;
	}
	
	private String formatColumnValue(FL_PropertyType type, String value) {
		if (type.equals(FL_PropertyType.DOUBLE) || type.equals(FL_PropertyType.FLOAT) || type.equals(FL_PropertyType.INTEGER) || type.equals(FL_PropertyType.LONG)) {
			return value;
		} else {
			return String.format("\'%s\'", value);
		}
	}
}
