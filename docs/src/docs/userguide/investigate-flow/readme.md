---
section: Docs
subsection: User Guide
topic: Investigating Transaction Flow
permalink: docs/user-guide/investigate-flow/
layout: submenu
---

Investigating Transaction Flow
==============================

This topic describes how to investigate accounts of interest using the Flow tab, Influent's workspace for interacting with transaction flow history.

<div class="git">
	<ul>
		<li><a href="#add-accounts">Adding Accounts to the Flow Tab</a>
		<li><a href="#transaction-flow-period">Setting the Transaction Flow Period</a>
		<li><a href="#workspace">Managing the Workspace</a>
		<li><a href="#entity-representations">Understanding Entity Representations</a>
		<li><a href="#investigating-entities">Investigating Entities</a>
		<li><a href="#branching">Branching Transaction Flow</a>
		<li><a href="#managing-entities">Managing Entities</a>
	</ul>
</div>

## <a name="add-accounts"></a> Adding Accounts to the Flow Tab ##

You can add accounts to the workspace by searching for entities or transactions on the Accounts or Transactions tabs and viewing the applicable results on the Flow tab.

<h6 class="procedure">To view entities from the Accounts tab in the Flow workspace</h6>

1. Switch to the Accounts tab.
2. Search for accounts using known attributes and review the results.
3. Select the checkboxes for the results of interest and click the **View selected accounts in Flow View** button.<img src="../../../img/screenshots/view-flow.png" class="screenshot" alt="View Selected Accounts in Flow View" />
4. Any accounts you selected are added to a new file in the Flow workspace and automatically clustered together.<img src="../../../img/screenshots/new-account-flow.png" class="screenshot" alt="New Accounts in the Flow workspace" />
5. If you have other files or accounts in the workspace that you want to remove, click the **Clear everything but this** button.
6. Double click the *(empty title)* section of the file to enter a descriptive name for the accounts it contains.

<h6 class="procedure">To view entities from the Transactions tab in the Flow workspace</h6>

1. Switch to the Transactions tab.
2. Search for transactions using known details and review the results.
3. Select the checkboxes for the results of interest and click the **View selected accounts in Flow View** button.<img src="../../../img/screenshots/view-flow-from-trans.png" class="screenshot" alt="View Selected Accounts in Flow View" />
4. The accounts that participated in the transactions you selected are added to new files in the Flow workspace. The **From** entities are added to one file on the left side of the workspace, while the **To** entities are added to a file in the next column to the right. If you selected more than one transaction, the participating **From** entities are automatically clustered together as are the **To** entities.<img src="../../../img/screenshots/new-accounts-flow.png" class="screenshot" alt="New Accounts in the Flow workspace" />
5. If you have other files or accounts in the workspace that you want to remove, click the **Clear everything but these** button.
6. Double click the *(empty title)* section of the new files to enter a descriptive names for the accounts they contains.

## <a name="transaction-flow-period"></a> Setting the Transaction Flow Period ##

The first step in investigating an account is to choose a range of dates over which to view its transactions. The Transaction Flow period controls in the Flow tab header allow you to configure this range.

<img src="../../../img/screenshots/header-transaction-flow.png" class="screenshot" alt="Transaction Flow Period" />

Setting the range requires two steps:

1. Choose the start and end dates you want to view.
2. Choose one of predetermined time intervals (e.g., two weeks) that overlaps with the date your selected.

Influent supports the following predetermined intervals. 

<div class="props">
	<table class="summaryTable">
		<thead>
			<tr>
				<th scope="col">Interval</th>
				<th scope="col">Description</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="value">2&nbsp;weeks</td>
				<td class="description">14-day period beginning or ending on any day</td>
			</tr>
			
			<tr>
				<td class="value">16&nbsp;weeks</td>
				<td class="description">16 calendar weeks running from Sunday to Saturday.</td>
			</tr>
			
			<tr>
				<td class="value">1&nbsp;year</td>
				<td class="description">12-month period beginning or ending on the first of any month</td>
			</tr>
			
			<tr>
				<td class="value">16&nbsp;months</td>
				<td class="description">16-month period beginning or ending on the first of any month</td>
			</tr>
			
			<tr>
				<td class="value">4&nbsp;years</td>
				<td class="description">4-year period beginning or ending on the first day of any quarter (Jan 1, Apr 1, Jul 1 or Oct 1)</td>
			</tr>
			
			<tr>
				<td class="value">16&nbsp;years</td>
				<td class="description">16 calendar years running Jan 1 to the following Jan 1</td>
			</tr>
			
		</tbody>
	</table>
</div>

**NOTE**: Due to how Influent builds transaction flow tables from your source data, the predetermined periods cannot be customized for new deployments of the application.

<h6 class="procedure">To choose an appropriate Transaction Flow Period</h6>

Because the Flow workspace allows you to discover previously unknown entities, at first you may not know the best period of time over which to investigate them. To quickly discover the full range of time over which an account is active:

1. [Select](#selected) the account in the workspace to view the [Details pane](../interface/#details-pane).
2. In the Account Details section, click the **View accounts** button to open the Accounts tab.
3. On the Accounts tab, click the **[more]** link to show a complete transaction history chart for the selected account.
4. Mouse over the earliest and most recent bars on the chart to view the dates corresponding to its full range of activities. Alternatively, you can select a shorter span to investigate activity patterns of interest.<img src="../../../img/screenshots/trans-flow-get-date.png" class="screenshot" alt="Find Earliest Activity" />
5. Return to the Flow tab and enter the full date range you want to investigate.
6. Choose one of the preconfigured time windows suggested by Influent.

## <a name="workspace"></a> Managing the Workspace ##

Before beginning your investigation, it is helpful to understand how the entities in your data are represented in the Flow workspace, which is your desktop for viewing, investigating and assessing transaction flow and accounts. 

The workspace is separated into [columns](#columns), each of which can contain any number of [files](#files) in which you organize accounts for investigation and unfiled accounts that branch off of accounts of interest.

### <a name="columns"></a> Columns ###

A column in the Flow workspace is an structure that emphasizes the left-to-right flow of transactions. Accounts in columns on the left send transactions to accounts on the right.

As shown in the following figure, columns can contain [files](#files) or unfiled [branched](#branching) accounts. As new accounts are added to the workspace through branching, they are either appended to the bottom of an existing column or placed in a new column.

<img src="../../../img/screenshots/columns.png" class="screenshot" alt="Columns" />

#### Managing Columns ####

<h6 class="procedure">To sort entities in a column</h6>

- Mouse over the workspace above the column, click the **Sort Column** ![Sort Column](../../../img/screenshots/buttons/sort-column.png) button and select a sorting method: *Incoming Flow*, *Outgoing Flow* or *Both*.

<p class="procedure-text">Entities are listed in descending order of the selected sorting method. Column sorting is useful when you:</p>

- Branch off of more than one account or cluster in a column.
- Expand a cluster to reveal child accounts or clusters, then branch off of them. 

<p class="procedure-text">Regardless of the chosen sorting method, files and the accounts they contain always remain at the top of a column.</p>

<h6 class="procedure">To clear unfiled entities from a column</h6>

- Mouse over the workspace above the column and click the **Clear Column** ![Clear Column](../../../img/screenshots/buttons/clear-column.png) button.

<p class="procedure-text">All unfiled content (branched accounts) is removed from the column.</p>

### <a name="files"></a> Files ###

Files store and organize accounts you want to investigate.

<img src="../../../img/screenshots/file.png" class="screenshot" alt="Empty File" />

When you add multiple accounts to the same file, they are automatically clustered together to provide an aggregate view of all the accounts. You can expand or collapse the new cluster at any time to show or hide its child entities.

Files are also used to save accounts for [exporting](../import-export/). When exporting your Flow workspace to an XML file, only the accounts and clusters stored in files are saved; unfiled accounts are not included.

#### Creating New Files ####

<h6 class="procedure"> To add a new file to the top of a column</h6>

- Mouse over the workspace above the column and click the **Add New File** ![Add New File](../../../img/screenshots/buttons/move-to-new-file.png) button.

<h6 class="procedure">To create a new file containing an unfiled account already in the workspace</h6>

- Mouse over the account and click the **Move to File** ![Move to File](../../../img/screenshots/buttons/move-to-new-file.png) button. 

<p class="procedure-text"><strong>NOTE</strong>: The new file inherits the name of the selected account.</p>

<h6 class="procedure">To add search results from the Accounts tab to a new file</h6>

1. Select the checkboxes for the desired results.
2. Click the **View Selected Accounts in Flow View** button.<img src="../../../img/screenshots/view-flow.png" class="screenshot" alt="View Selected Accounts in Flow View" />
3. To remove all other items in the workspace, click the **Clear everything but this** button.

#### Adding Accounts to Existing Files ####

<h6 class="procedure">To add accounts to an existing file</h6>

- Click and drag the desired account in the workspace into the file.

<h6 class="procedure">To search for accounts to add to a file</h6>

- Mouse over the file and click the **Search for Accounts to Add** ![Search for Accounts to Add](../../../img/screenshots/buttons/search-for-similar-accounts.png) button to switch to the Accounts tab.

#### Managing Files ####

<h6 class="procedure">To rename a file</h6>

- Double click the file name, type the new name and press ENTER.<img src="../../../img/screenshots/file-rename.png" class="screenshot" alt="Rename File" />

<h6 class="procedure">To remove a file</h6>

- Mouse over the file and click the **Remove** ![Remove](../../../img/screenshots/buttons/remove.png) button.

<p class="procedure-text">Deleting a file also deletes all of the entities inside of it.</p>

## <a name="entity-representations"></a> Understanding Entity Representations ##

Understanding the representations of [entities](../concepts/#entities) in the Flow workspace is a key component of spotting unusual behavior. Each entity is represented by a card with a group of icons and a chart that summarize its attributes and activities. 

The different entity types have their own unique representations:

- Individual accounts are represented by cards
- Clusters and account owners are represented by a stack of cards held together by a paper clip
- Cluster summaries are represented by a stack of cards with no paper clip

<table cellpadding="10" align="center">
	<tr>
		<td><img src="../../../img/screenshots/card-example.png" class="table" alt="Account Card" /></td>
		<td><img src="../../../img/screenshots/cluster-example.png" class="table" alt="Cluster Stack" /></td>
	</tr>
	<tr>
		<td class="caption">Account</td>
		<td class="caption">Cluster</td>
	</tr>
	<tr>
		<td><img src="../../../img/screenshots/account-owner.png" class="table" alt="Account Owner Stack" /></td>
		<td><img src="../../../img/screenshots/cluster-summary-example.png" class="table" alt="Cluster Summary Stack" />
	</tr>
	<tr>
		<td class="caption">Account Owner</td>
		<td class="caption">Cluster Summary</td>
	</tr>
</table>

 As described in the following sections, the face of each card or stack indicates:

- The [certainty](#certainty) of its transaction history
- Its [focus](#focus) within the workspace
- The [attributes](#summary-attributes) of the entities it represents
- Its [transaction history](#transaction-history) over the specified transaction flow period

### <a name="certainty"></a> Certainty ###

A card's background indicates the level of certainty about the transactions in which it is involved: 

- **White Background**: The dataset for the account represented by the card is complete, and there is no uncertainty about its transactions.
- **Parchment Background**: The dataset for the account represented by the card is incomplete. Uncertainty exists about the exact transactions the account has made.<p class="list-paragraph">In the Kiva data, parchment backgrounds are displayed for lender accounts, as the total amount of each loan to the borrower is known, but not the exact amount from each lender.</p>
- **Mixed Background**: Only appears on clusters with a mix of entities with varying levels of certainty. Identified by the lack of "dirtiness" that appears on the uncertainty parchment background.

<table cellpadding="10" align="center">
	<tr>
		<td><img src="../../../img/screenshots/certainty.png" class="table" alt="Certainty" /></td>
		<td><img src="../../../img/screenshots/uncertainty.png" class="table" alt="Uncertainty" /></td>
		<td><img src="../../../img/screenshots/uncertainty-cluster.png" class="table" alt="Cluster Uncertainty" /></td>
	</tr>
	<tr>
		<td class="caption">Certainty</td>
		<td class="caption">Uncertainty</td>
		<td class="caption">Cluster Uncertainty</td>
	</tr>
</table>

### <a name="focus"></a> Workspace Focus: Highlighted vs. Selected ###

The border of a card or stack indicates whether the workspace is currently configured to:

- Display expanded account and transaction attributes of the [selected](#selected) account
- Call out transactions involving the [highlighted](#highlighted) account 

The terms "highlighted" and "selected" are used throughout this help and should not be confused.

<table cellpadding="10" align="center">
	<tr>
		<td><img src="../../../img/screenshots/no-focus.png" class="table" alt="No Focus" /></td>
		<td><img src="../../../img/screenshots/highlighted.png" class="table" alt="Highlighted" /></td>
		<td><img src="../../../img/screenshots/selected.png" class="table" alt="Selected" /></td>
	</tr>
	<tr>
		<td class="caption">No Focus</td>
		<td class="caption">Highlighted</td>
		<td class="caption">Selected</td>
	</tr>
</table>

#### <a name="highlighted"></a> Highlighted ####

A highlighted account is the current focal point of the Flow workspace. The highlighted card has an orange border. The following transaction flow details associated with all other cards in the workspace are drawn in relation to the highlighted card:

- Account Activity Charts are scaled in relation to the proportions of the highlighted account.
- Transactional data (all charts and flow lines) is represented in grayscale, except for portions of those transactions made directly with the highlighted account, which are represented in color.

<img src="../../../img/screenshots/highlight-example.png" class="screenshot" alt="Highlighted Account" />

<h6 class="procedure">To change the highlighted card in the workspace</h6>

- Mouse over the desired card and click its **Highlight Flow** ![Highlight Flow button](../../../img/screenshots/buttons/highlight.png) button.

#### <a name="selected"></a> Selected ####

A selected account or cluster is one for which the [Details Pane](../interface/#details-pane) is currently displayed. The selected card has a blue border.

<img src="../../../img/screenshots/selected-example.png" class="screenshot" alt="Selected Account" />

<h6 class="procedure">To change the selected card</h6>

- Click a new card in the workspace.

### <a name="summary-attributes"></a> Summary Attributes ###

The text and icons on a card or stack indicate key account and transaction attributes that describe the account or cluster. Hover over an icon to view a tooltip explaining what it represents.

For clusters, account owners and cluster summaries, a bar under each icon indicates the percentage of accounts in the group with the corresponding attribute. One icon type may appear multiple times on the same stack if the group contains accounts with different values for a single attribute (e.g., there may be two location icons if 75% of the accounts are in Spain and 25% are in France).

**NOTE**: Many of the icons below are specific to the Kiva dataset. Each implementation can be modified to show unique icons for any attribute in the source dataset.

<div class="props">
	<table class="summaryTable">
		<thead>
			<tr>
				<th scope="col">Category</th>
				<th scope="col">Description</th>
				<th scope="col">Icon</th>
				<th scope="col">Name</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="description">Name</td>
				<td class="description">Name of the account or cluster. Cluster names are chosen from among their child entities.
				</td>
				<td class="description"></td>
				<td class="value"></td>
			</tr>
			<tr>
				<td class="description" rowspan="3">Account&nbsp;Type</td>
				<td class="description" rowspan="3">Indicates the type of account.</td>
				<td class="description"><img src="../../../img/screenshots/icons/lender.png" alt="Lender icon" /></td>
				<td class="value">Lender</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/borrower.png" alt="Loan icon" /></td>
				<td class="value">Loan</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/partner.png" alt="Partner icon" /></td>
				<td class="value">Partner</td>
			</tr>
			<tr>
				<td class="description" rowspan="6">Location</td>
				<td class="description" rowspan="6">Indicates the continent on which the account is located. A three-letter ISO country code below each icon indicates the country in which the account is located.</td>
				<td class="description"><img src="../../../img/screenshots/icons/location-af.png" alt="Africa icon" /></td>
				<td class="value">Africa</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/location-as.png" alt="Asia icon" /></td>
				<td class="value">Asia</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/location-eu.png" alt="Europe icon" /></td>
				<td class="value">Europe</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/location-na.png" alt="North America icon" /></td>
				<td class="value">North&nbsp;America</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/location-oc.png" alt="Oceania icon" /></td>
				<td class="value">Oceania</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/location-sa.png" alt="South America icon" /></td>
				<td class="value">South&nbsp;America</td>
			</tr>
			<tr>
				<td class="description" rowspan="2">Status</td>
				<td class="description" rowspan="2">Indicates the status of the account. For the Kiva dataset, this attribute flags loan accounts that have defaulted on their repayments and partner accounts that have been closed.</td>
				<td class="description"><img src="../../../img/screenshots/icons/status.png" alt="Closed icon" /></td>
				<td class="value">Closed</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/defaulted.png" alt="Defaulted icon" /></td>
				<td class="value">Defaulted</td>
			</tr>
			<tr>
				<td class="description" rowspan="2">Warning</td>
				<td class="description" rowspan="2">Flags accounts with attributes that require special attention. For the Kiva dataset, this icon flags accounts with high delinquency or default rates.</td>
				<td class="description"><img src="../../../img/screenshots/icons/warning.png" alt="High Default Rate icon" /></td>
				<td class="value">High&nbsp;Default&nbsp;Rate</td>
			</tr>
			<tr>
				<td class="description"><img src="../../../img/screenshots/icons/warning.png" alt="High Delinquency Rate icon" /></td>
				<td class="value">High&nbsp;Delinquency&nbsp;Rate</td>
			</tr>
			<tr>
				<td class="description">Display&nbsp;Counter</td>
				<td class="description">Indicates the number of times the account appears in the workspace. Not displayed for clusters or account owners.</td>
				<td class="description"><img src="../../../img/screenshots/icons/display-counter.png" alt="Display Counter" /></td>
				<td class="value"></td>
			</tr>
			<tr>
				<td class="description">Cluster&nbsp;Member Count</td>
				<td class="description">Number of individual accounts that make up the cluster. Not displayed for accounts.</td>
				<td class="description"><img src="../../../img/screenshots/icons/member-count.png" alt="Cluster Member Count" /></td>
				<td class="value"></td>
			</tr>
		</tbody>
	</table>
</div>

### <a name="transaction-history"></a> Transaction History Chart ###

The Transaction History chart on each card provides an aggregated view of the account's activity over the selected Transaction Flow Period (represented by the x-axis). Transactions flowing into the account are shown above the x-axis, while transactions flowing out are below.

<img src="../../../img/screenshots/icons/card-transaction-history.png" class="screenshot" alt="Transaction History Chart" />

Depending on the selected Transaction Flow Period, each bar in the chart represents an aggregation of all transactions that occurred over a different timescale:

<div class="props">
	<table class="summaryTable">
		<thead>
			<tr>
				<th scope="col">Interval</th>
				<th scope="col">Each Bar Represents</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="value">2 weeks</td>
				<td class="description">1 day</td>
			</tr>
			<tr>
				<td class="value">16 weeks</td>
				<td class="description">1 week</td>
			</tr>
			<tr>
				<td class="value">1 year</td>
				<td class="description">1 month</td>
			</tr>
			<tr>
				<td class="value">16 months</td>
				<td class="description">1 month</td>
			</tr>
			<tr>
				<td class="value">4 years</td>
				<td class="description">1 quarter of a year</td>
			</tr>
			<tr>
				<td class="value">16 years</td>
				<td class="description">1 year</td>
			</tr>
		</tbody>
	</table>
</div>

The values of any transactions with the [highlighted](#highlighted) account are called out in orange. All other transactions are represented in grayscale.

#### Scale ####

For new workspaces, Transaction History charts are scaled relative to a default value that is configurable for each deployment. In the Kiva application, for example, this value is set as $100. Once you [highlight](#highlighted) an account in the workspace, the default value is overridden and all transaction charts are scaled relative to the greatest value in the transaction history of the highlighted account.

In either case, aggregated transaction values that are larger than the scale are shown as clipped bars with a black line above or below them to indicate they are not to scale.

<img src="../../../img/screenshots/clipped-history.png" class="screenshot" alt="Clipped Transaction History Chart" />

#### Expanded View ####

An expanded version of each account's transaction history chart is accessible in the [Details pane](../interface/#details-pane). The scale of this chart is always relative to the account's own transaction history.

## <a name="investigating-entities"></a> Investigating Entities ##

Once you have identified accounts that you want to investigate, you can use the Flow workspace to interactively expand their transaction flow to understand behavioural patterns.

### Reviewing Account Attributes ###

<h6 class="procedure">To review the key attributes of an account</h6>

1. Review the [summary icons](#summary-attributes) on the account card or cluster stack.
2. Mouse over any icon to view a tooltip describing the attribute it represents.

<h6 class="procedure">To review a complete list of account attributes</h6>

1. Click the account to open the [Details pane](../interface/#details-pane).
2. Scroll through the left panel to review the Account Details (for accounts) or Cluster Member Summary (for clusters).

<p class="procedure-text"><strong>NOTE</strong>: The border of the selected card or stack in the Flow workspace is now blue.</p>

### Reviewing Transaction History ###

<h6 class="procedure">To visualize the transaction history of an account</h6>

1. Review the [Transaction History chart](#transaction-history) on the card or stack, which provides a quick view of the account's history over the specified Transaction Flow period.
2. For an expanded view of the same chart, click the account to open the Details pane.
3. On the right panel, select the Transaction Chart tab to view an expanded and labeled version of the Transaction History chart.
4. Mouse over any bar in the chart for an overview of the date ranges it covers and the total inflowing or outgoing value it represents.

<h6 class="procedure">To review a list of the transactions an account has made</h6>

1. Click the account to open the [Details pane](../interface/#details-pane).
2. On the right panel, select the Transaction Table tab to view a paginated list of the transactions in which the account was involved. Transactions that involve the account that is currently [highlighted](#highlighted) are listed in orange. 

<p class="procedure-text"><strong>NOTE</strong>: The Transaction Table tab is not available for clusters.</p>

## <a name="branching"></a> Branching Transaction Flow ##

To help you investigate the flow of transactions, Influent allows you to interactively branch out the entities with which an account has participated in an a transaction. This allows you to create visualizations of your data that contain only the entities relevant to a specific line of investigation.

The directionality of branching in the Flow workspace is semantically meaningful. Entities on the left side of the workspace send transactions to entities on the right. This allows you to follow the money or communication trail between entities.

<img src="../../../img/screenshots/branch-directionality.png" class="screenshot" alt="Directionality of Branching" />

Depending on your dataset, you may see instances where a single entity appears both in the left and right branches of another entity. In the Kiva example, this indicates the nature of the microloan data. A partner loans money to a borrower, and then receives an equivalent amount back at a later date. Thus, the partner appears on both sides of the borrower. Understanding the expected transactional patterns in your data source is the key to using Influent to find accounts of interest.

### Branching off of an Entity ###

<h6 class="procedure">To branch off an entity</h6>

1. Mouse over the account or cluster to reveal its **Branch** buttons.
2. Review the number under each **Branch** button to see the total source and destination entities to which the account or cluster is connected. Any connected entities already shown in the workspace will also be indicated.
3. Click the left **Branch** button to view transactions flowing into the account.<img src="../../../img/screenshots/branch.png" class="screenshot" alt="Branch Left" />
4. Click the right **Branch** button to view the transactions flowing out of the account.

#### Limitations ####

- Influent displays warnings when you try to branch off an account with connections to more than 1,000 accounts on the incoming or outgoing side, as the process of computing clusters and refreshing the workspace for a large group of accounts can be time consuming. For the same reason, Influent will not allow branching off of accounts with more than 10,000 incoming or outgoing connections. Both of these limits can be configured for each Influent deployment.
- You cannot create branches directly from accounts in your [activity search](../activity-search) results. You can only branch from accounts saved in files or from accounts branched off of accounts saved in files.

### <a name="transaction-flow-lines"></a> Understanding Transaction Flow Lines ###

Lines between accounts and clusters in the Flow workspace indicate the value of the transaction between the entities. The thicker the lines, the greater the value.

<div class="props">
	<table class="summaryTable">
		<thead>
			<tr>
				<th scope="col">Line Type</th>
				<th scope="col">Description</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="property">
					Transactions
					<br>
					<img src="../../../img/screenshots/icons/transaction.png" alt="Transactions">
				</td>
				<td class="description">Appear between accounts or clusters that have been involved in a transaction. The account/cluster on the left side of the line sent a transaction to the account/cluster on the right side.
				<p>Indicates that neither account in the transaction is the highlighted focal point of the workspace.	
				</td>
			</tr>
			<tr>
				<td class="property">
				Highlighted Transactions
				<br>
				<img src="../../../img/screenshots/icons/transaction-highlighted.png" alt="Highlighted Transactions">
				</td>
				<td class="description">Appear between the account or cluster that is currently highlighted and all of the other entities in its transaction flow.</td>
			</tr>
			<tr>
				<td class="property">
					Asserted Relationships
					<br>
					<img src="../../../img/screenshots/icons/relationship.png" alt="Asserted Relationships">
				</td>
				<td class="description">Appear between files in the workspace. Indicates a user-asserted relationship between the accounts/clusters stored in the files.</td>
			</tr>
			<tr>
				<td class="property">
					Highlighted Asserted Relationships
					<br>
					<img src="../../../img/screenshots/icons/relationship-highlighted.png" alt="Highlighted Asserted Relationships">
				</td>
				<td class="description">Appear between files in the workspace. Indicates one of the accounts or clusters in the user-asserted relationship is the highlighted focal point of the workspace.</td>
			</tr>
		</tbody>
	</table>
</div>

### Highlighting an Account's Transactions ###

Influent allows you to apply focus to the workspace by highlighting transactions involving a specific entity. When you highlight an entity:

- All direct transactions with the highlighted account are shown in orange. Unrelated and indirect transactions are displayed in grayscale. 
- All other Activity History charts in the workspace are scaled in proportion to the transaction values on the highlighted card.

<h6 class="procedure">To make a card the main focal point of the Flow workspace</h6>

- Mouse over the card and click the **Highlight Flow** ![Highlight Flow](../../../img/screenshots/buttons/highlight.png) button.

## <a name="managing-entities"></a> Managing Entities ###

After you have expanded the transaction flow of the accounts you are investigating, you may want to manage the newly added entities to organize the workspace, save additional accounts of interest for future use and remove any irrelevant entities.

### <a name="moving-accounts"></a> Moving Accounts ###

The following sections describe how to move accounts to [files](#files), which are used to distinguish accounts of interest and manage the workspace.

<h6 class="procedure">To move an activity search result to an existing file</h6>

- Mouse over the card and click the **Add to File** ![Add to File](../../../img/screenshots/buttons/add-to-file.png) button to add the account to the file under which the results were returned. 

<p class="procedure-text"><strong>NOTE</strong>: You can also click and drag account cards to manually move them to or between files.</p>

<h6 class="procedure">To move an account to a new file</h6>

- Mouse over an account in the workspace that has been [branched](#branching) off of a filed card and click the **Move to New File** ![Move to New File](../../../img/screenshots/buttons/move-to-new-file.png) button.

<p class="procedure-text">Influent creates a new file with the same name as the selected card and adds the card to it.</p>

<h6 class="procedure">Removing an Account from the Workspace</h6>

- To remove an account from the workspace, mouse over the card and click the **Remove** ![Remove](../../../img/screenshots/buttons/remove.png) button.

<p class="procedure-text">Influent removes the card from the workspace, file or search results.</p>

<h6 class="procedure"><a name="expand-collapse-clusters"></a>To expand or collapse a cluster</h6>

- Click the paper clip icon on the left side of the stack:
	- **Unstack** ![Unstack](../../../img/screenshots/buttons/unstack.png): Indicates that you can expand the member accounts and sub-clusters that make up the stack.
	- **Restack** ![Restack](../../../img/screenshots/buttons/restack.png): Indicates that you can collapse the member accounts and sub-clusters that make up the stack.

## Next Steps

For information on searching for accounts with similar activity histories, see the [Activity Search](../activity-search) topic. 