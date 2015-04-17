---
section: Docs
subsection: User Guide
topic: Understanding Influent Concepts
permalink: docs/user-guide/concepts/
layout: submenu
---

Understanding Influent Concepts
===============================

This topic describes how the individual entities and transactions in your source data are represented and persisted across the different tabs in Influent. <div class="git">The concepts in this topic include:

	<ul>
		<li><a href="#entities">Entities</a>
		<ul>
			<li><a href="#accounts">Accounts</a>
			<li><a href="#clusters">Clusters</a>
			<ul>
				<li><a href="#cluster-grouping">How Accounts Are Grouped into Clusters</a>
			</ul>
			<li><a href="#account-owners">Account Owners</a>
			<li><a href="#cluster-summaries">Cluster Summaries</a>
		</ul>
		<li><a href="#transactions">Transactions</a>
	</ul>
</div>

For more information on the individual tabs that make up the Influent application, see the [Interface](../interface/) topic.

## <a name="entities"></a>Entities ##

Influent supports four types of entities, two core types and two optional types, to represent the actors from your transaction flow data:

- Core entities:
	- [Accounts](#accounts) represent individual entities.
	- [Clusters](#clusters) are dynamically generated hierarchical groups of similar accounts that can be expanded or collapsed.
- Optional entities:
	- [Account Owners](#account-owners) are large groups of accounts owned by the same entity that, like clusters, can be expanded or collapsed.
	- [Cluster Summaries](#cluster-summaries) represent very large groups of pre-aggregated accounts (generally > 1,000) that, unlike clusters and account owners, cannot be expanded.

**NOTE**: It is up to each deployment to determine -- based on available system resources and the extent of the source dataset -- when to include the optional entity types and how to group together individual accounts.

### <a name="accounts"></a>Accounts ###

Accounts represent individual actors in your transaction flow data. Influent can differentiate between accounts of various types. In the Kiva data example, there are three account types:

- **Lenders**, who donate money to partners to fulfill a requested loan, then receive money from the partner once the borrower begins repaying the loan.
- **Partners**, who receive money donated by lenders, distribute it to the borrower, manage repayments from the borrower and redistribute them back to the lenders.
- **Borrowers**, who request loans, receive money from partners, then make repayments back to the partner based on the terms of the loan.

Each account is associated with the following sets of attributes:

- **Account details**, which can include identifying information such as a name, location, description or photograph
- **Transaction details**, which includes the identity of the other accounts the transactions were carried out with, the date on which they occurred and their value.

You can interact with accounts on the following tabs in Influent:

- [Accounts](../interface/#accounts), where you can search for accounts based on identifying attributes
- [Flow](../interface/#flow), where you visualize and investigate the transactions in which accounts are involved

### <a name="clusters"></a>Clusters ###

To organize and simplify the Flow workspace, Influent often dynamically groups similar [accounts](#accounts) into clusters. Clusters act like accounts in the workspace, but can be expanded and collapsed to show or hide their child entities. 

Collapsed clusters display aggregate summary information about the attributes and transactions of their child entities. In addition to accounts, clusters can also contain other clusters. The nesting of clusters is generated according to configurable hierarchical groups.

Dynamically generated clusters appear only on the [Flow](../interface/#flow) tab in Influent.

#### <a name="cluster-grouping"></a>How Accounts Are Grouped into Clusters ####

The Influent hierarchical clustering algorithm can be configured to cluster accounts based on any entity attribute. In the Kiva example, accounts are clustered together based on the following hierarchical groups:

1. **Categorical Group on Account Type**: Accounts are grouped based on type. In the Kiva example, there are three types: lenders, partners and borrowers.
2. **Geographical Group on Location**: Accounts may be further grouped by geography. Geographical clusters are also hierarchical: first continent, then region (e.g., Central or Eastern Africa), country and finally similar latitude/longitude (if available).
3. **Label Group on Account Name**: Further groupings are based on names. By default, similar names are grouped alphabetically, then using fuzzy string matching. This process:
	- Removes special characters (e.g., leading/trailing non-printing characters and punctuation)
	- Converts all characters to lowercase
	- Changes accented characters to ASCII representations
	- Sorts unique, whitespace-separated parts of the name

	For example, "Héctor" and "hector " are treated as the same name and grouped together, as are "John Doe" and "Doe John".

By default, each cluster has a maximum of 6 child entities (accounts and/or clusters). This value can be configured for each Influent deployment.

### <a name="account-owners"></a>Account Owners ###

An account owner represents a large group of accounts owned by the same entity. These entities, which like clusters can be expanded or collapsed, are used to simplify the Influent workspace and search results and to optimize application performance.

Collapsed account owners display aggregate summary information about the account attributes and transaction data of their child entities.

In the Kiva application, each loan is associated with a unique partner (broker) account. Ownership of these hundreds of thousands of accounts are split among several hundred account owner entities.

You can interact with account owners on the following tabs in Influent:

- [Accounts](../interface/#accounts), where you can search for account owners based on identifying attributes
- [Flow](../interface/#flow), where you can visualize and investigate the transactions in which account owners are involved.

**NOTE**: For account owner entities to appear in your Influent deployment, your administrator must create records for them and insert them into your Transaction database. 

### <a name="cluster-summaries"></a>Cluster Summaries ###

A cluster summary is an entity that represents a pre-aggregation of a large group of related accounts. These entities, which unlike clusters and account owners cannot be expanded or collapsed, are used to simplify the Influent workspace and optimize application performance. 

Cluster summaries display aggregate summary information about the account attributes and transaction data of their child entities.

In the Kiva application, cluster summaries are used to pre-aggregate two types of account groups:

- Large groups of accounts (>1,000) that branch off a single entity
- Particularly large account owners

You can interact with cluster summaries only on the [Flow](../interface/#flow) tab.

**NOTE**: For cluster summary entities to appear in your Influent deployment, your administrator must create records for them and insert them into your Transaction database.

## <a name="transactions"></a>Transactions ##

A transaction is a timestamped record of a link or event between two entities. Each transaction in Influent contains information about the value of the link (e.g., the monetary amount), the directionality (who sent the transaction and who received it) and the date and time on which it occurred.

Transactions appear on the following tabs in Influent:

- [Transactions](../interface/#transactions), where you can search for transactions based on the participating entities, date or value
- [Flow](../interface/#flow), where you can visualize and investigate the transactions between entities

## Next Steps ##

For a detailed description of the Influent user interface components, see the [Navigating the Interface](../interface) topic.