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

--
-- FINANCIAL FLOW
-- 
create table TEST_FinFlow (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), FirstTransaction datetime, LastTransaction datetime, Amount float, CONSTRAINT pk_FF_ID PRIMARY KEY (FromEntityId, ToEntityId));

--
-- FINANCIAL FLOW SUMMARY
--
create table TEST_FinFlowDaily     (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), Amount float, PeriodDate datetime, CONSTRAINT pk_FFD_ID PRIMARY KEY (FromEntityId, ToEntityId, PeriodDate));
create table TEST_FinFlowWeekly    (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), Amount float, PeriodDate datetime, CONSTRAINT pk_FFW_ID PRIMARY KEY (FromEntityId, ToEntityId, PeriodDate));
create table TEST_FinFlowMonthly   (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), Amount float, PeriodDate datetime, CONSTRAINT pk_FFM_ID PRIMARY KEY (FromEntityId, ToEntityId, PeriodDate));
create table TEST_FinFlowQuarterly (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), Amount float, PeriodDate datetime, CONSTRAINT pk_FFQ_ID PRIMARY KEY (FromEntityId, ToEntityId, PeriodDate));
create table TEST_FinFlowYearly    (FromEntityId varchar(100), FromEntityType varchar(1), ToEntityId varchar(100), ToEntityType varchar(1), Amount float, PeriodDate datetime, CONSTRAINT pk_FFY_ID PRIMARY KEY (FromEntityId, ToEntityId, PeriodDate));

--
-- FINANCIAL ENTITY SUMMARY
--
create table TEST_FinEntityDaily     (EntityId varchar(100), PeriodDate datetime, InboundAmount float, InboundDegree int, OutboundAmount float, OutboundDegree int, Balance float, CONSTRAINT pk_FED_ID PRIMARY KEY (EntityId, PeriodDate));
create table TEST_FinEntityWeekly    (EntityId varchar(100), PeriodDate datetime, InboundAmount float, InboundDegree int, OutboundAmount float, OutboundDegree int, Balance float, CONSTRAINT pk_FEW_ID PRIMARY KEY (EntityId, PeriodDate));
create table TEST_FinEntityMonthly   (EntityId varchar(100), PeriodDate datetime, InboundAmount float, InboundDegree int, OutboundAmount float, OutboundDegree int, Balance float, CONSTRAINT pk_FEM_ID PRIMARY KEY (EntityId, PeriodDate));
create table TEST_FinEntityQuarterly (EntityId varchar(100), PeriodDate datetime, InboundAmount float, InboundDegree int, OutboundAmount float, OutboundDegree int, Balance float, CONSTRAINT pk_FEQ_ID PRIMARY KEY (EntityId, PeriodDate));
create table TEST_FinEntityYearly    (EntityId varchar(100), PeriodDate datetime, InboundAmount float, InboundDegree int, OutboundAmount float, OutboundDegree int, Balance float, CONSTRAINT pk_FEY_ID PRIMARY KEY (EntityId, PeriodDate));

--
-- CLUSTER SUMMARY
-- 
create table TEST_ClusterSummary	(EntityId varchar(100), Property varchar(50), Tag varchar(50), Type varchar(50), Value varchar(200), Stat float, CONSTRAINT pk_CS_ID PRIMARY KEY (EntityId, Property, Value));

--
-- CLUSTER SUMMARY MEMBERS
-- 
create table TEST_ClusterSummaryMembers (SummaryId varchar(100), EntityId varchar(100), CONSTRAINT pk_CSM_ID PRIMARY KEY (SummaryId, EntityId));

--
-- KIVA FLOW GENERATION
--
insert into TEST_FinFlowDaily (FromEntityId, FromEntityType, ToEntityId, ToEntityType, Amount, PeriodDate)
	select 
		TransactionSource, 
		'A', 
		TransactionTarget, 
		'A', 
		sum(TransactionAmount), 
		convert(varchar(50), TransactionDate, 101)
	from TEST_Transactions
	group by TransactionSource, TransactionTarget, convert(varchar(50), TransactionDate, 101)

insert into TEST_FinFlowDaily (FromEntityId, FromEntityType, ToEntityId, ToEntityType, Amount, PeriodDate)
 	select 
		substring(TransactionSource, 1, charindex('-',TransactionSource)-1),
		'O',
		TransactionTarget,
		'A', 
		sum(TransactionAmount), 
		convert(varchar(50), TransactionDate, 101)
  	from TEST_Transactions
  	where substring(TransactionSource,1,8) = 'partner.'
  	group by substring(TransactionSource,1,charindex('-',TransactionSource)-1), TransactionTarget, convert(varchar(50), TransactionDate, 101)

insert into TEST_FinFlowDaily (FromEntityId, FromEntityType, ToEntityId, ToEntityType, Amount, PeriodDate)
 	select 
		TransactionSource, 
		'A', 
		substring(TransactionTarget,1,charindex('-',TransactionTarget)-1), 
		'O', 
		sum(TransactionAmount), 
		convert(varchar(50), TransactionDate, 101)
  	from TEST_Transactions
  	where substring(TransactionTarget,1,8) = 'partner.'
  	group by TransactionSource, substring(TransactionTarget,1,charindex('-',TransactionTarget)-1), convert(varchar(50), TransactionDate, 101)

--  create FinFlowDaily indices
create index ix_ffd_from on TEST_FinFlowDaily     (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffd_to   on TEST_FinFlowDaily     (ToEntityId,   PeriodDate, FromEntityId, Amount);

--
-- KIVA ENTITY GENERATION (LOANS)
--

-- create Fin Entity Loan table
create table TEST_FinEntityLoan(
	EntityId varchar(100) PRIMARY KEY, 
	InboundDegree int, 
	UniqueInboundDegree int,  
	OutboundDegree int, 
	UniqueOutboundDegree int, 
	NumTransactions int, 
	MaxTransaction float, 
	AvgTransaction float, 
	StartDate datetime, 
	EndDate datetime,
	[type] varchar(20), 
	LoanId bigint, 
	LoanName varchar(512), 
	LoanUse varchar(max), 
	LoanActivity varchar(512), 
	LoanSector varchar(512),  
	LoanStatus varchar(512), 
	LoanAmount bigint, 
	LoanFundedAmount bigint, 
	LoanBasketAmount bigint, 
	LoanPaidAmount float, 
	LoanCurrencyExchangeLossAmount float, 
	LoanPostedDate datetime, 
	LoanPaidDate datetime, 
	LoanDelinquent bit, 
	LoanFundedDate datetime, 
	LoanPlannedExpirationDate datetime, 
	LoanDescriptionTexts_en varchar(max), 
	LoanDescriptionTexts_ru varchar(max), 
	LoanDescriptionTexts_fr varchar(max), 
	LoanDescriptionTexts_es varchar(max), 
	LoanDescriptionTexts_vi varchar(max), 
	LoanDescriptionTexts_id varchar(max), 
	LoanDescriptionTexts_pt varchar(max), 
	LoanDescriptionTexts_mn varchar(max), 
	LoanDescriptionTexts_ar varchar(max), 
	LoanImageId bigint, 
	LoanImageURL varchar(2048), 
	LoanVideoId bigint, 
	LoanVideoYoutubeId varchar(512), 
	LoanLocationGeoLevel varchar(512), 
	LoanLocationGeoPairs varchar(512), 
	LoanLocationGeoType varchar(512), 
	LoanLocationTown varchar(512), 
	LoanLocationCountryCode varchar(20), 
	LoanLocationCountry varchar(512), 
	LoanTermsLoanAmount bigint, 
	LoanTermsDisbursalDate datetime, 
	LoanTermsDisbursalCurrency varchar(30), 
	LoanTermsDisbursalAmount float, 
	LoanTermsLossLiabilityCurrencyExchange varchar(512), 
	LoanTermsLossLiabilityCurrencyExchangeCoverageRate float, 
	LoanTermsLossLiabilityNonpayment varchar(512), 
	LoanJournalTotalsEntries bigint, 
	LoanJournalTotalsBulkEntries bigint, 
	LoanLat float, 
	LoanLon float
);

-- populate table with common attributes
insert into TEST_FinEntityLoan
select 
	t1.EntityId, 
	inboundDegree, 
	uniqueInboundDegree, 
	outboundDegree, 
	uniqueOutboundDegree, 
	NumTransactions, 
	MaxTransactions, 
	AvgTransactions, 
	StartDate, 
	EndDate,
	NULL,
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL
from (
	select 
		EntityId, 
		sum(inboundDegree) as inboundDegree, 
		sum(uniqueInboundDegree) as uniqueInboundDegree, 
		sum(outboundDegree) as outboundDegree, 
		sum(uniqueOutboundDegree) as uniqueOutboundDegree
	from (
		select 
			FromEntityId as EntityId, 
			0 as inboundDegree, 
			0 as uniqueInboundDegree, 
			count(ToEntityId) as outboundDegree, 
			count( distinct ToEntityId ) as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where ToEntityType = 'A'
		group by FromEntityId
		union
		select 
			ToEntityId as EntityId, 
			count(FromEntityId) as inboundDegree, 
			count( distinct FromEntityId ) as uniqueInboundDegree, 
			0 as outboundDegree, 
			0 as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where FromEntityType = 'A'
		group by ToEntityId
	) q
	where EntityId like 'loan.%'
	group by EntityId
) t1
left join
(
	select 
		EntityId, 
		sum(numTransactions) as NumTransactions, 
		max(MaxTransaction) as MaxTransactions, 
		sum(TotalTransactions) / sum(numTransactions) as AvgTransactions, 
		min(StartDate) as StartDate, 
		max(EndDate) as EndDate
	from (
		select 
			TransactionTarget as EntityId, 
			count(TransactionTarget) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate  
		from TEST_Transactions
		group by TransactionTarget
		union
		select 
			TransactionSource as EntityId, 
			count(TransactionSource) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate 
		from TEST_Transactions
		group by TransactionSource
	)q
	where EntityId like 'loan.%'
	group by EntityId
) t2
on t2.EntityId = t1.EntityId

-- update table with loan specific attributes
update TEST_FinEntityLoan
set 
	[type] = 'loan', 
	LoanId = FEP.LoanId,
	LoanName = FEP.LoanName,
	LoanUse = FEP.LoanUse, 
	LoanActivity = FEP.LoanActivity, 
	LoanSector = FEP.LoanSector, 
	LoanStatus = FEP.LoanStatus, 
	LoanAmount = FEP.LoanAmount, 
	LoanFundedAmount = FEP.LoanFundedAmount, 
	LoanBasketAmount = FEP.LoanBasketAmount, 
	LoanPaidAmount = FEP.LoanPaidAmount, 
	LoanCurrencyExchangeLossAmount = FEP.LoanCurrencyExchangeLossAmount, 
	LoanPostedDate = FEP.LoanPostedDate, 
	LoanPaidDate = FEP.LoanPaidDate, 
	LoanDelinquent = FEP.LoanDelinquent, 
	LoanFundedDate = FEP.LoanFundedDate, 
	LoanPlannedExpirationDate = FEP.LoanPlannedExpirationDate, 
	LoanDescriptionTexts_en = FEP.LoanDescriptionTexts_en, 
	LoanDescriptionTexts_ru = FEP.LoanDescriptionTexts_ru, 
	LoanDescriptionTexts_fr = FEP.LoanDescriptionTexts_fr, 
	LoanDescriptionTexts_es = FEP.LoanDescriptionTexts_es, 
	LoanDescriptionTexts_vi = FEP.LoanDescriptionTexts_vi,
	LoanDescriptionTexts_id = FEP.LoanDescriptionTexts_id, 
	LoanDescriptionTexts_pt = FEP.LoanDescriptionTexts_pt, 
	LoanDescriptionTexts_mn = FEP.LoanDescriptionTexts_mn, 
	LoanDescriptionTexts_ar = FEP.LoanDescriptionTexts_ar, 
	LoanImageId = FEP.LoanImageId, 
	LoanImageURL = FEP.LoanImageURL, 
	LoanVideoId = FEP.LoanVideoId, 
	LoanVideoYoutubeId = FEP.LoanVideoYoutubeId, 
	LoanLocationGeoLevel = FEP.LoanLocationGeoLevel, 
	LoanLocationGeoPairs = FEP.LoanLocationGeoPairs, 
	LoanLocationGeoType = FEP.LoanLocationGeoType, 
	LoanLocationTown = FEP.LoanLocationTown, 
	LoanLocationCountryCode = FEP.LoanLocationCountryCode, 
	LoanLocationCountry = FEP.LoanLocationCountry,
	LoanTermsLoanAmount = FEP.LoanTermsLoanAmount,
	LoanTermsDisbursalDate = FEP.LoanTermsDisbursalDate, 
	LoanTermsDisbursalCurrency = FEP.LoanTermsDisbursalCurrency, 
	LoanTermsDisbursalAmount = FEP.LoanTermsDisbursalAmount, 
	LoanTermsLossLiabilityCurrencyExchange = FEP.LoanTermsLossLiabilityCurrencyExchange, 
	LoanTermsLossLiabilityCurrencyExchangeCoverageRate = FEP.LoanTermsLossLiabilityCurrencyExchangeCoverageRate, 
	LoanTermsLossLiabilityNonpayment = FEP.LoanTermsLossLiabilityNonpayment, 
	LoanJournalTotalsEntries = FEP.LoanJournalTotalsEntries, 
	LoanJournalTotalsBulkEntries = FEP.LoanJournalTotalsBulkEntries, 
	LoanLat = FEP.LoanLat, 
	LoanLon = FEP.LoanLon
from TEST_FinEntityLoan FE
inner join
TEST_FinEntityProperties_loan FEP
on FE.EntityId = FEP.EntityId

-- create index on Fin Entity Loan table
create index ix_fe_lo_id on TEST_FinEntityLoan (EntityId);

--
-- KIVA ENTITY GENERATION (LENDERS)
--

-- create Fin Entity Lender table
create table TEST_FinEntityLender(
	EntityId varchar(100) PRIMARY KEY, 
	InboundDegree int, 
	UniqueInboundDegree int,  
	OutboundDegree int, 
	UniqueOutboundDegree int, 
	NumTransactions int, 
	MaxTransaction float, 
	AvgTransaction float, 
	StartDate datetime, 
	EndDate datetime,
	[type] varchar(20), 
	LenderId varchar(512),
    LenderName varchar(512),
    LenderImageId bigint,
	LenderImageURL varchar(2048),
    LenderMemberSince datetime,
    LenderWhereabouts varchar(512),
    LenderCountryCode varchar(20),
    LenderOccupation varchar(512),
    LenderOccupationalInfo varchar(2048),
    LenderInviterId varchar(512),
    LenderInviteeCount bigint,
    LenderLoanCount bigint,
    LenderLoanBecause varchar(2048),
    LenderLat float,
    LenderLon float
);

-- populate table with common attributes
insert into TEST_FinEntityLender
select 
	t1.EntityId, 
	inboundDegree, 
	uniqueInboundDegree, 
	outboundDegree, 
	uniqueOutboundDegree, 
	NumTransactions, 
	MaxTransactions, 
	AvgTransactions, 
	StartDate, 
	EndDate,
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL
from (
	select 
		EntityId, 
		sum(inboundDegree) as inboundDegree, 
		sum(uniqueInboundDegree) as uniqueInboundDegree, 
		sum(outboundDegree) as outboundDegree, 
		sum(uniqueOutboundDegree) as uniqueOutboundDegree
	from (
		select 
			FromEntityId as EntityId, 
			0 as inboundDegree, 
			0 as uniqueInboundDegree, 
			count(ToEntityId) as outboundDegree, 
			count( distinct ToEntityId ) as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where ToEntityType = 'A'
		group by FromEntityId
		union
		select 
			ToEntityId as EntityId, 
			count(FromEntityId) as inboundDegree, 
			count( distinct FromEntityId ) as uniqueInboundDegree, 
			0 as outboundDegree, 
			0 as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where FromEntityType = 'A'
		group by ToEntityId
	) q
	where EntityId like 'lender.%'
	group by EntityId
) t1
left join
(
	select 
		EntityId, 
		sum(numTransactions) as NumTransactions, 
		max(MaxTransaction) as MaxTransactions, 
		sum(TotalTransactions) / sum(numTransactions) as AvgTransactions, 
		min(StartDate) as StartDate, 
		max(EndDate) as EndDate
	from (
		select 
			TransactionTarget as EntityId, 
			count(TransactionTarget) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate  
		from TEST_Transactions
		group by TransactionTarget
		union
		select 
			TransactionSource as EntityId, 
			count(TransactionSource) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate 
		from TEST_Transactions
		group by TransactionSource
	)q
	where EntityId like 'lender.%'
	group by EntityId
) t2
on t2.EntityId = t1.EntityId

-- update table with lender specific attributes
update TEST_FinEntityLender
set 
	[type] = 'lender', 
	LenderId = FEP.LenderId, 
	LenderName = FEP.LenderName, 
	LenderImageId = FEP.LenderImageId,
	LenderImageURL = 'http://www.kiva.org/img/default_lender.png',
	LenderMemberSince = FEP.LenderMemberSince, 
	LenderWhereabouts = FEP.LenderWhereabouts, 
	LenderCountryCode = FEP.LenderCountryCode, 
	LenderOccupation = FEP.LenderOccupation, 
	LenderOccupationalInfo = FEP.LenderOccupationalInfo, 
	LenderInviterId = FEP.LenderInviterId, 
	LenderInviteeCount = FEP.LenderInviteeCount, 
	LenderLoanCount = FEP.LenderLoanCount, 
	LenderLoanBecause = FEP.LenderLoanBecause, 
	LenderLat = FEP.LenderLat, 
	LenderLon = FEP.LenderLon
from TEST_FinEntityLender FE
inner join
TEST_FinEntityProperties_lender FEP
on FE.EntityId = FEP.EntityId

-- create index on Fin Entity Lender table
create index ix_fe_le_id on TEST_FinEntityLender (EntityId);

--
-- KIVA ENTITY GENERATION (PARTNERS + BROKERS)
--

-- create Fin Entity Partner table
create table TEST_FinEntityPartner(
	EntityId varchar(100) PRIMARY KEY, 
	InboundDegree int, 
	UniqueInboundDegree int,  
	OutboundDegree int, 
	UniqueOutboundDegree int, 
	NumTransactions int, 
	MaxTransaction float, 
	AvgTransaction float, 
	StartDate datetime, 
	EndDate datetime,
	[type] varchar(20), 
	PartnerId bigint,
	OwnerId varchar(512),
    PartnerName varchar(512),
    PartnerStatus varchar(512),
    PartnerRating varchar(512),
    PartnerDueDiligenceType varchar(512),
    PartnerImageId bigint,
	PartnerImageURL varchar(2048), 
    PartnerStartDate datetime,
    PartnerDelinquencyRate float,
    PartnerDefaultRate float,
    PartnerTotalAmountRaised float,
    PartnerLoansPosted bigint,
    PartnerCountryCode varchar(max),
    PartnerLat varchar(max),
    PartnerLon varchar(max),
);

-- populate table with common attributes
insert into TEST_FinEntityPartner
select 
	t1.EntityId, 
	inboundDegree, 
	uniqueInboundDegree, 
	outboundDegree, 
	uniqueOutboundDegree, 
	NumTransactions, 
	MaxTransactions, 
	AvgTransactions, 
	StartDate, 
	EndDate,
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL, 
	NULL
from (
	select 
		EntityId, 
		sum(inboundDegree) as inboundDegree, 
		sum(uniqueInboundDegree) as uniqueInboundDegree, 
		sum(outboundDegree) as outboundDegree, 
		sum(uniqueOutboundDegree) as uniqueOutboundDegree
	from (
		select 
			FromEntityId as EntityId, 
			0 as inboundDegree, 
			0 as uniqueInboundDegree, 
			count(ToEntityId) as outboundDegree, 
			count( distinct ToEntityId ) as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where ToEntityType = 'A'
		group by FromEntityId
		union
		select 
			ToEntityId as EntityId, 
			count(FromEntityId) as inboundDegree, 
			count( distinct FromEntityId ) as uniqueInboundDegree, 
			0 as outboundDegree, 
			0 as uniqueOutboundDegree
		from TEST_FinFlowDaily
		where FromEntityType = 'A'
		group by ToEntityId
	) q
	where EntityId like 'partner.%'
	group by EntityId
) t1
left join
(
	select 
		EntityId, 
		sum(numTransactions) as NumTransactions, 
		max(MaxTransaction) as MaxTransactions, 
		sum(TotalTransactions) / sum(numTransactions) as AvgTransactions, 
		min(StartDate) as StartDate, 
		max(EndDate) as EndDate
	from (
		select 
			TransactionTarget as EntityId, 
			count(TransactionTarget) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate  
		from TEST_Transactions
		group by TransactionTarget
		union
		select 
			TransactionSource as EntityId, 
			count(TransactionSource) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate 
		from TEST_Transactions
		group by TransactionSource
	)q
	where EntityId like 'partner.%'
	group by EntityId
) t2
on t2.EntityId = t1.EntityId

-- Compute Partner NumTransactions, MaxTransactions, AvgTransactions, StartDate, EndDate
update TEST_FinEntityPartner
set NumTransactions = FEP.NumTransactions,
	MaxTransaction = FEP.MaxTransaction, 
	AvgTransaction = FEP.AvgTransaction, 
	StartDate = FEP.StartDate, 
	EndDate = FEP.EndDate
from TEST_FinEntityPartner FE
inner join 
(
	select 
		EntityId, 
		sum(numTransactions) as NumTransactions, 
		max(MaxTransaction) as MaxTransaction, 
		sum(TotalTransactions) / sum(numTransactions) as AvgTransaction, 
		min(StartDate) as StartDate, 
		max(EndDate) as EndDate
	from (
		select 
			case 
				when charindex('-', TransactionTarget) is NULL then TransactionTarget
				when charindex('-', TransactionTarget) = 0 then TransactionTarget
				else left(TransactionTarget, (charindex('-', TransactionTarget) - 1))
			end as EntityId, 
			count(TransactionTarget) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate  
		from TEST_Transactions
		group by 
			case 
				when charindex('-', TransactionTarget) is NULL then TransactionTarget
				when charindex('-', TransactionTarget) = 0 then TransactionTarget
				else left(TransactionTarget, (charindex('-', TransactionTarget) - 1))
			end 
		union
		select 
			case 
				when charindex('-', TransactionSource) is NULL then TransactionSource
				when charindex('-', TransactionSource) = 0 then TransactionSource
				else left(TransactionSource, (charindex('-', TransactionSource) - 1))
			end as EntityId, 
			count(TransactionSource) as numTransactions, 
			max(TransactionAmount) as MaxTransaction, 
			sum(TransactionAmount) as TotalTransactions, 
			min(TransactionDate) as StartDate, 
			max(TransactionDate) as EndDate 
		from TEST_Transactions
		group by 
			case 
				when charindex('-', TransactionSource) is NULL then TransactionSource
				when charindex('-', TransactionSource) = 0 then TransactionSource
				else left(TransactionSource, (charindex('-', TransactionSource) - 1))
			end 
	)q
	where EntityId like 'partner.%'
	group by EntityId
) FEP
on FE.EntityId = FEP.EntityId

-- update table with partner specific attributes
update TEST_FinEntityPartner
set 
	[type] = 'partner', 
	PartnerId = FEP.PartnerId, 
	PartnerName = FEP.PartnerName, 
	PartnerStatus = FEP.PartnerStatus, 
	PartnerRating = FEP.PartnerRating, 
	PartnerDueDiligenceType = FEP.PartnerDueDiligenceType, 
	PartnerImageId = FEP.PartnerImageId, 
	PartnerImageURL = FEP.PartnerImageURL, 
    PartnerStartDate = FEP.PartnerStartDate,
    PartnerDelinquencyRate = FEP.PartnerDelinquencyRate,
    PartnerDefaultRate = FEP.PartnerDefaultRate,
    PartnerTotalAmountRaised = FEP.PartnerTotalAmountRaised,
    PartnerLoansPosted = FEP.PartnerLoansPosted,
    PartnerCountryCode = FEP.PartnerCountryCode,
    PartnerLat = FEP.PartnerLat,
    PartnerLon = FEP.PartnerLon
from TEST_FinEntityPartner FE
inner join
TEST_FinEntityProperties_partner FEP
on 
case
	when charindex('-', FE.EntityId) > 0 then left(FE.EntityId, (charindex('-', FE.EntityId) - 1))
	else FE.EntityId
end = FEP.EntityId

-- add ownerId information
update TEST_FinEntityPartner
set OwnerId = left(EntityId, (charindex('-', EntityId) - 1))
where charindex('-', EntityId) > 0

-- create index on Fin Entity Partner table
create index ix_fe_pa_id on TEST_FinEntityPartner (EntityId);

--
--  FIN FLOW AGGREGATIONS
--
insert into TEST_FinFlowWeekly
 select FromEntityId, FromEntityType, ToEntityId, ToEntityType, sum(Amount), CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101)
  from TEST_FinFlowDaily
  group by FromEntityId, FromEntityType, ToEntityId, ToEntityType, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101);
  
insert into TEST_FinFlowMonthly
 select FromEntityId, FromEntityType, ToEntityId, ToEntityType, sum(Amount), CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + convert(varchar(2), DATEPART(mm, PeriodDate)) + '/01', 101)
  from TEST_FinFlowDaily
  group by FromEntityId, FromEntityType, ToEntityId, ToEntityType, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + convert(varchar(2), DATEPART(mm, PeriodDate)) + '/01', 101);
  
insert into TEST_FinFlowQuarterly
 select FromEntityId, FromEntityType, ToEntityId, ToEntityType, sum(Amount), CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101)
  from TEST_FinFlowMonthly
  group by FromEntityId, FromEntityType, ToEntityId, ToEntityType, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101);
  
insert into TEST_FinFlowYearly
 select FromEntityId, FromEntityType, ToEntityId, ToEntityType, sum(Amount), CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101)
  from TEST_FinFlowMonthly
  group by FromEntityId, FromEntityType, ToEntityId, ToEntityType, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101);

--  create FinFlow indices
create index ix_ffw_from on TEST_FinFlowWeekly    (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffw_to   on TEST_FinFlowWeekly    (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffm_from on TEST_FinFlowMonthly   (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffm_to   on TEST_FinFlowMonthly   (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffq_from on TEST_FinFlowQuarterly (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffq_to   on TEST_FinFlowQuarterly (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffy_from on TEST_FinFlowYearly    (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffy_to   on TEST_FinFlowYearly    (ToEntityId,   PeriodDate, FromEntityId, Amount);

--  build FinFlow
insert into TEST_FinFlow 
 select FromEntityId, FromEntityType, ToEntityId, ToEntityType, min(PeriodDate), max(PeriodDate), sum(Amount)
  from TEST_FinFlowDaily
  group by FromEntityId, FromEntityType, ToEntityId, ToEntityType;

create index ix_ff_to_from on TEST_FinFlow (ToEntityId, FromEntityId);
create index ix_ff_from_to on TEST_FinFlow (FromEntityId, ToEntityId);

--
--  FIN ENTITY AGGREGATIONS
--
create table temp_ids (Entity varchar(100));
create index tids on temp_ids (Entity);

insert into temp_ids
 select distinct FromEntityId
  from TEST_FinFlowYearly
 union
 select distinct ToEntityId
  from TEST_FinFlowYearly;
  
insert into TEST_FinEntityDaily select Entity, PeriodDate,
       sum(case when ToEntityId = Entity and FromEntityType = 'A' then Amount else 0 end),
       sum(case when ToEntityId = Entity and FromEntityType = 'A' then 1 else 0 end), -- calculate inbound degree
       sum(case when FromEntityId = Entity and ToEntityType = 'A' then Amount else 0 end),
       sum(case when FromEntityId = Entity and ToEntityType = 'A' then 1 else 0 end), -- calculate outbound degree
       0 -- TODO calculate balance
 from temp_ids
 join TEST_FinFlowDaily on FromEntityId = Entity or ToEntityId = Entity
 group by Entity, PeriodDate;
 
-- cleanup
drop table temp_ids;

insert into TEST_FinEntityWeekly
 select EntityId, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101), sum(InboundAmount), sum(InboundDegree), sum(OutboundAmount), sum(OutboundDegree), 0
  from TEST_FinEntityDaily
  group by EntityId, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101);
  
insert into TEST_FinEntityMonthly
 select EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + convert(varchar(2), DATEPART(mm, PeriodDate)) + '/01', 101), sum(InboundAmount), sum(InboundDegree), sum(OutboundAmount), sum(OutboundDegree), 0
  from TEST_FinEntityDaily
  group by EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + convert(varchar(2), DATEPART(mm, PeriodDate)) + '/01', 101);
  
insert into TEST_FinEntityQuarterly
 select EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101), sum(InboundAmount), sum(InboundDegree), sum(OutboundAmount), sum(OutboundDegree), 0
  from TEST_FinEntityMonthly
  group by EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101);
  
insert into TEST_FinEntityYearly
 select EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101), sum(InboundAmount), sum(InboundDegree), sum(OutboundAmount), sum(OutboundDegree), 0
  from TEST_FinEntityQuarterly
  group by EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101);
 
create index ix_fed on TEST_FinEntityDaily     (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_few on TEST_FinEntityWeekly    (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_fem on TEST_FinEntityMonthly   (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_feq on TEST_FinEntityQuarterly (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_fey on TEST_FinEntityYearly    (EntityId, PeriodDate, InboundAmount, OutboundAmount);

create index ix_csum on TEST_ClusterSummary	(EntityId);
create index ix_cmem on TEST_ClusterSummaryMembers  (SummaryId);