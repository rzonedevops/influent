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

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

-- -----------------------------
-- Influent Data Views 1.0 DRAFT
-- -----------------------------

--
-- FINANCIAL FLOW - ALL
--  Used to build the aggregate flow diagrams
--   
--   FromEntityId - entity UID that is the source of the transactions
--   ToEntityId - entity UID that is the target of the transactions
--   FirstTransaction - datetime of first transaction
--   LastTransaction - datetime of last transaction
--   Amount - aggregate amount
--
create table FinFlow (FromEntityId varchar(100), ToEntityId varchar(100), FirstTransaction datetime, LastTransaction datetime, Amount float);

--
-- FINANCIAL FLOW - AGGREGATED BY TIME
--  Used to build the aggregate flow diagrams (aggregated by time)
--  and used to build the highlighted sub-section of the time series charts on entities.
--   
--   FromEntityId - entity UID that is the source of the transactions
--   ToEntityId - entity UID that is the target of the transactions
--   Amount - aggregate amount for this time period
--   Date - start of the time period
--
create table FinFlowDaily     (FromEntityId varchar(100), ToEntityId varchar(100), Amount float, PeriodDate datetime);
create table FinFlowWeekly    (FromEntityId varchar(100), ToEntityId varchar(100), Amount float, PeriodDate datetime);
create table FinFlowMonthly   (FromEntityId varchar(100), ToEntityId varchar(100), Amount float, PeriodDate datetime);
create table FinFlowQuarterly (FromEntityId varchar(100), ToEntityId varchar(100), Amount float, PeriodDate datetime);
create table FinFlowYearly    (FromEntityId varchar(100), ToEntityId varchar(100), Amount float, PeriodDate datetime);

--
-- FINANCIAL ENTITY SUMMARY
--  Used to build the time series charts on entities (aggregated by time).
--   
--   EntityId - entity UID
--   Date - start of the time period
--   InboundAmount - aggregate credits for this time period
--   OutboundAmount - aggregate debits for this time period
--   Balance - aggregate credits - debits up until this time period
--
create table FinEntityDaily     (EntityId varchar(100), PeriodDate datetime, InboundAmount float, OutboundAmount float, Balance float);
create table FinEntityWeekly    (EntityId varchar(100), PeriodDate datetime, InboundAmount float, OutboundAmount float, Balance float);
create table FinEntityMonthly   (EntityId varchar(100), PeriodDate datetime, InboundAmount float, OutboundAmount float, Balance float);
create table FinEntityQuarterly (EntityId varchar(100), PeriodDate datetime, InboundAmount float, OutboundAmount float, Balance float);
create table FinEntityYearly    (EntityId varchar(100), PeriodDate datetime, InboundAmount float, OutboundAmount float, Balance float);

--
-- DATA VIEW DRIVERS
--  These scripts will populate the data views above.
--
--  Step 1. Modify this to pull data from your raw data.  
--  
insert into FinFlowDaily
 SELECT [source_id], [dest_id], sum([amount]), convert(varchar(50), [dt], 101)
  FROM YOUR_RAW_DATA
  group by [source_id], [dest_id], convert(varchar(50), [dt], 101)

--
--  Step 2. The rest of the script will collect data from FinFlowDaily.
--          Execute the rest of this script "as-is".
--  

--  build the rest of the FinFlow aggregations
insert into FinFlowWeekly
 select FromEntityId, ToEntityId, sum(Amount), CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101)
  from FinFlowDaily
  group by FromEntityId, ToEntityId, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101);
  
insert into FinFlowMonthly
 select FromEntityId, ToEntityId, sum(Amount), CONVERT(varchar(50), (DATEADD(dd, 1 - DATEPART(d, PeriodDate), PeriodDate)), 101)
  from FinFlowWeekly
  group by FromEntityId, ToEntityId, CONVERT(varchar(50), (DATEADD(dd, 1 - DATEPART(d, PeriodDate), PeriodDate)), 101);
  
insert into FinFlowQuarterly
 select FromEntityId, ToEntityId, sum(Amount), CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101)
  from FinFlowMonthly
  group by FromEntityId, ToEntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101);
  
insert into FinFlowYearly
 select FromEntityId, ToEntityId, sum(Amount), CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101)
  from FinFlowMonthly
  group by FromEntityId, ToEntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101);

--  create FinFlow indices
create index ix_ffd_from on FinFlowDaily     (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffd_to   on FinFlowDaily     (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffw_from on FinFlowWeekly    (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffw_to   on FinFlowWeekly    (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffm_from on FinFlowMonthly   (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffm_to   on FinFlowMonthly   (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffq_from on FinFlowQuarterly (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffq_to   on FinFlowQuarterly (ToEntityId,   PeriodDate, FromEntityId, Amount);
create index ix_ffy_from on FinFlowYearly    (FromEntityId, PeriodDate, ToEntityId,   Amount);
create index ix_ffy_to   on FinFlowYearly    (ToEntityId,   PeriodDate, FromEntityId, Amount);


--  build FinFlow
insert into FinFlow 
 select FromEntityId, ToEntityId, min(PeriodDate), max(PeriodDate), sum(Amount)
  from FinFlowDaily
  group by FromEntityId, ToEntityId;

create index ix_ff_to_from on FinFlow (ToEntityId, FromEntityId);
create index ix_ff_from_to on FinFlow (FromEntityId, ToEntityId);


--  build FinEntityDaily
create table temp_ids (Entity varchar(100));

insert into temp_ids
 select distinct FromEntityId
  from FinFlowYearly
 union
 select distinct ToEntityId
  from FinFlowYearly;
  
create index tids on temp_ids (Entity);

insert into FinEntityDaily select Entity, PeriodDate,
       sum(case when ToEntityId = Entity then Amount else 0 end),
       sum(case when FromEntityId = Entity then Amount else 0 end),
       0 -- TODO calculate balance
 from temp_ids
 join FinFlowDaily on FromEntityId = Entity or ToEntityId = Entity
 group by Entity, PeriodDate;

drop table temp_ids;

-- build the rest of the FinEntity aggregations
insert into FinEntityWeekly
 select EntityId, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101), sum(InboundAmount), sum(OutboundAmount), 0
  from FinEntityDaily
  group by EntityId, CONVERT(varchar(50), (DATEADD(dd, @@DATEFIRST - DATEPART(dw, PeriodDate) - 6, PeriodDate)), 101);
  
insert into FinEntityMonthly
 select EntityId, CONVERT(varchar(50), (DATEADD(dd, 1 - DATEPART(d, PeriodDate), PeriodDate)), 101), sum(InboundAmount), sum(OutboundAmount), 0
  from FinEntityWeekly
  group by EntityId, CONVERT(varchar(50), (DATEADD(dd, 1 - DATEPART(d, PeriodDate), PeriodDate)), 101);
  
insert into FinEntityQuarterly
 select EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101), sum(InboundAmount), sum(OutboundAmount), 0
  from FinEntityMonthly
  group by EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/' + case when DATEPART(q, PeriodDate)=1 then '01' when DATEPART(q, PeriodDate)=2 then '04' when DATEPART(q, PeriodDate)=3 then '07' when DATEPART(q, PeriodDate)=4 then '010' end + '/01', 101);
  
insert into FinEntityYearly
 select EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101), sum(InboundAmount), sum(OutboundAmount), 0
  from FinEntityQuarterly
  group by EntityId, CONVERT(varchar(50), convert(varchar(4), DATEPART(yyyy, PeriodDate)) + '/01/01', 101);
 
create index ix_fed on FinEntityDaily     (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_few on FinEntityWeekly    (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_fem on FinEntityMonthly   (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_feq on FinEntityQuarterly (EntityId, PeriodDate, InboundAmount, OutboundAmount);
create index ix_fey on FinEntityYearly    (EntityId, PeriodDate, InboundAmount, OutboundAmount);
