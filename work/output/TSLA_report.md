# Tesla, Inc. (TSLA): Research Report

**Recommendation: AVOID** (fixed by the rule engine; this report explains it and does not alter it)
**Price:** $377.89 | **Market cap:** about $1.49T | **Currency:** USD

---

## 1. Executive summary

The rule engine's recommendation is **AVOID**. The decision rests on four failed checks:

- **Valuation.** The DCF gives $20.12 (bear), $28.51 (base) and $37.69 (bull) per share against a price of $377.89. Even the bull case is about 90% below the market price. The margin-of-safety check failed, with an actual value of -12.2556 as supplied by the rule engine.
- **Bear case.** The bear scenario implies -94.7% versus price, so the "bear not catastrophic" test failed.
- **Returns.** ROE is 4.67%, which failed the ROE check.
- **Skeptic review.** Severity is **major**, driven by governance, dilution and capital-allocation concerns, and 13 accounting red flags were logged.

Two checks passed. Debt is acceptable (debt/equity 0.18373), and cash conversion is acceptable (FY2025 FCF was 1.64x net income).

The fundamentals behind the failures:

- Operating margin fell from 17.0% in FY2022 to 5.1% in FY2025, and was 1.4% in Q2 2026.
- Trailing P/E is 349.9x and EV/EBITDA is 133.6x.
- Stock-based compensation (SBC) was 3.1x operating income in Q2 2026.
- Shares outstanding rose about 5.3% in six months.

**Key uncertainty:** the DCF may understate the value of autonomy and robotics (robotaxi, Optimus, FSD), which the model does not capture. The inputs are also imperfect (see sections 4 and 8). The rule engine's verdict is nevertheless unambiguous on the data provided.

---

## 2. Business overview

Tesla (Nasdaq: TSLA; Texas-incorporated, headquartered in Austin) is classified as Consumer Cyclical / Auto Manufacturers. The 10-Q reports three revenue lines: Automotive (sales, regulatory credits, leasing), Energy generation and storage, and Services and other.

| Q2 2026 revenue | $M |
|---|---|
| Automotive | 20,516 |
| Energy | 3,139 |
| Services and other | 4,581 |
| **Total** | **28,236** |

*(Source: SEC 10-Q, 2026-07-23.)*

FY2025 revenue was $94.827B and operating income was $4.849B (market data history).

XBRL tags suggest FSD subscriptions, robotaxi and bot deliveries. The excerpts contain no narrative describing them, and the 10-K Item 1 business description is cut off, so this summary is limited to the financial statements, market data and third-party summaries.

Per the qualitative research, Model 3 and Model Y made up 97% of deliveries. Q2 2026 deliveries were 480,126 against BYD's 557,090 BEVs (third-party sources listed in section 9).

---

## 3. Financial snapshot

**Annual history** (market data history; FCF = operating cash flow less capex):

| FY | Revenue | Operating income | Net income | Op. cash flow | Capex | FCF |
|---|---|---|---|---|---|---|
| 2022 | $81.462B | $13.832B | $12.583B | $14.724B | $7.172B | $7.552B |
| 2023 | $96.773B | $8.891B | $14.999B | $13.256B | $8.899B | $4.357B |
| 2024 | $97.690B | $7.760B | $7.130B | $14.923B | $11.342B | $3.581B |
| 2025 | $94.827B | $4.849B | $3.794B | $14.747B | $8.527B | $6.220B |

Derived figures:
- Operating margin was 5.1% in FY2025 versus 17.0% in FY2022.
- FCF CAGR over FY2022–FY2025 is about -6.3% per year, against revenue CAGR of about +5.2%.

**Recent results** (SEC 10-Q, 2026-07-23):

| Metric | Q2 2026 | Q2 2025 |
|---|---|---|
| Revenue | $28,236M (+25.5%) | $22,496M |
| Gross profit | $4,751M (16.8%) | $3,878M (17.2%) |
| Income from operations | $398M (1.4% margin) | $923M (4.1%) |
| R&D | $2,371M (+49%) | $1,589M |
| SG&A | $1,982M (+45%) | $1,366M |
| Net income / diluted EPS (GAAP) | $1,128M / $0.32 | $1,190M / $0.33 |
| SBC | $1,222M | $692M |
| Regulatory credits | $146M | $439M |

- H1 2026 revenue was $50,623M versus $41,831M (+21.0%). H1 operating income was $1,339M versus $1,322M.
- Energy gross margin was about 20.4% versus 30.3% (derived).
- Pre-tax income of $1,329M included $931M (about 70%) from interest income and other income, net, which is not itemized in the excerpts.

**Balance sheet at 2026-06-30** (10-Q):
- Cash plus short-term investments are $43,524M (Dec 2025: $44,059M).
- Debt and finance leases are $9,342M (Dec 2025: $8,376M).
- Equity is $86,858M and total assets are $148,524M.
- PP&E, net rose to $47,255M from $40,643M.
- Other non-current assets rose to $9,453M from $5,045M.

**Multiples** (market data): trailing P/E 349.9x, forward P/E 176.2x, EV/EBITDA 133.6x, P/B 17.18x. Derived EV/FY2025 FCF is about 235x, and FCF yield on market cap is about 0.42%.

**Consensus** (Yahoo Finance – Analysis): revenue of $106.59B for 2026 (+12.41%) and $120.48B for 2027 (+13.03%). EPS is $1.74 for 2026 and $2.14 for 2027, down from $2.11 and $2.56 90 days ago. The GAAP/non-GAAP basis is not stated.

---

## 4. Valuation

**Method:** a DCF flagged as "reliable" with no notes, using a base FCF of $4.719B, the 2023–2025 average from the FCF history.

| Scenario | FCF growth | Discount rate | Terminal growth | Intrinsic value / share | vs. price ($377.89) |
|---|---|---|---|---|---|
| Bear | 4% | 12% | 2.0% | **$20.12** | **-94.7%** |
| Base | 8% | 10% | 3.0% | **$28.51** | **-92.5%** |
| Bull | 11% | 9% | 3.5% | **$37.69** | **-90.0%** |

**Reading the result:** no scenario comes close to the market price. The price implies large future cash flows from autonomy, robotics and energy that are not yet visible in reported returns.

**Caveats on the model** (from the skeptic review):
- The base FCF is backward-looking. Third-party earnings-call summaries cite 2026 capex above $25B and Q2 FCF of about -$1.1B. These figures are not verifiable from the filings provided, because the 10-Q cash flow statement is truncated.
- The 8% growth rate is explicitly low-confidence. It sits between the -6.3% historical FCF CAGR and strong H1 revenue growth.
- The scenario range is narrow ($20–$38) and cannot represent the real uncertainty about autonomy payoffs.
- If anything, the near-term FCF inputs look more favorable than the capex guidance implies. The conclusion is therefore unlikely to be an artifact of overly harsh inputs, but the DCF cannot capture option value.

---

## 5. Moat & management

**Moat: narrow** (qualitative assessment).
- **Sources:** the Supercharger/NACS network, the FSD data flywheel (about 12B cumulative supervised miles and about 1.48M paid subscriptions, per third-party earnings-call summaries), vertical integration, brand and energy storage scale.
- **Erosion:** rivals can now use NACS, and the automotive gross margin ex-credits was 16.3%. Energy margins dropped sharply in Q2.
- **Returns:** FY2025 operating margin was 5.1% versus 17.0% in FY2022, and ROE is 4.67%. The autonomy moat is not yet evidenced in returns.
- **Third-party views differ:** GuruFocus scores the moat 7/10 ("entry-level wide"), while another analysis calls the tech lead "defensible but narrowing."

**Management: average.**
- **Heavy investment cycle:** capex is expected to exceed $25B in 2026, with up to $30B of borrowing capacity being lined up. Targets include Robotaxi, Optimus, semiconductors, AI compute and a lithium/cathode refinery. There are no disclosed return hurdles, so ROIC is unknown.
- **Execution is mixed:** record deliveries and backlog were reported, but Q2 EPS missed and margins fell. Revenue was $28.24B versus a $25.55B estimate, and adjusted EPS of $0.33 missed the $0.49 forecast (third-party summaries; the EPS estimate varies by source).
- **No buybacks or dividends.**

**Governance concerns:**
- A Delaware judge found the board too close to Musk to negotiate his pay at arm's length.
- On April 21, 2026 the board signed an implementation agreement to deliver the 2018 award. It registered 304M shares for it, now worth over $114B, and $9.97B of unrecognized SBC remains.
- A roughly $1T equity-based package was approved in Nov 2025 and would raise Musk's ownership to about 29%. ISS advised shareholders to reject it.
- Musk oversees five companies.
- Filings contain XBRL references to SpaceX, an "AI hardware company," X.AI and related-party activity, with no terms in the excerpts.

---

## 6. Risks & bear case

1. **Valuation requires unproven businesses.** The stock trades at about 350x trailing GAAP EPS, 134x EV/EBITDA and 235x FY2025 FCF, with a 0.42% FCF yield.
2. **Negative operating leverage.** Q2 revenue grew 25.5% year over year while R&D grew 49% and SG&A 45%. Operating margin was 1.4% versus 4.1%.
3. **Earnings quality.**
   - About 70% of Q2 pre-tax income came from interest and other income, net. Other income, net was +$590M in Q2 and about -$535M in Q1 (derived), and its composition is undisclosed.
   - The Q2 tax rate was 15.1% versus 23.2% a year earlier.
   - SBC was 3.1x operating income.
   - Regulatory credits fell to $146M from $439M.
4. **FCF likely to deteriorate.** Capex above $25B (reported by third parties) compares with $8.5B in 2025. Cash fell $0.5B in H1 despite share issuance, and debt rose.
5. **Dilution.**
   - Shares outstanding rose from 3,752M (Jan 2026) to 3,950M (Jul 2026), about +5.3%.
   - 194M shares were issued in Q2 "for equity incentive awards and acquisitions" at only $323M recorded. The cause is not explained in the excerpts, though the skeptic notes that CEO awards may account for much of it.
   - SBC is up 77% year over year.
6. **Governance and related-party exposure.** Other non-current assets rose 87% in six months, with no disclosed terms. A "shareholder settlement" of $89M was credited directly to equity in H1 2026.
7. **Competition.** BYD outsold Tesla in Q2 2026. Tesla's global EV share is about 12–13%. US federal EV tax credits have expired. Chinese overcapacity is being exported.
8. **Autonomy is unproven.** Robotaxi runs in about seven metros, Cybercab production only began in Q2 2026, and Optimus production has not started. A safety or regulatory setback would hit the highest-multiple part of the thesis.
9. **Falling estimates.** FY2026 and FY2027 EPS were cut by about 16–18% over 90 days (Yahoo Finance – Analysis).

**Skeptic severity: major.** The skeptic found no evidence of fraud or restatement. The concerns are structural: governance, dilution and capital allocation.

**Potential upside catalysts:** Robotaxi expansion (Phoenix and Las Vegas next), Cybercab and Optimus ramps, FSD approvals in Europe, and energy storage (13.5 GWh deployed in Q2, +41% year over year). A margin recovery would also help. These are not evidenced in current returns.

---

## 7. Rule-by-rule decision

| Rule check | Result | Detail |
|---|---|---|
| Margin of safety | **FAIL** | Actual: -12.2556 (as supplied by the rule engine; base-case DCF upside vs price is -92.5%) |
| Bear case not catastrophic | **FAIL** | Bear intrinsic value $20.12 (-94.7% vs price) |
| Debt acceptable | **PASS** | Debt/equity 0.18373; the 10-Q shows $9,342M of debt and finance leases against $43,524M of cash and short-term investments |
| ROE acceptable | **FAIL** | ROE 4.67% |
| Cash conversion | **PASS** | FCF conversion 1.64x (FY2025 FCF $6.22B vs net income $3.794B) |
| Skeptic review | **FAIL** | Severity "major" |
| Accounting red flags | **13 logged** | Detailed in the filings analysis |

**Outcome:** the passes (debt, cash conversion) do not offset the failures on valuation, downside, returns and skeptic severity. The fixed recommendation is **AVOID**.

*Note on the cash conversion pass:* the skeptic and filings analysis caution that FY2025's 1.64x ratio reflects depressed net income and non-cash charges and working capital. It is not a sign of strong earnings quality.

---

## 8. Data gaps & caveats

- **Truncated filings.** The 10-K text after the cover, forward-looking statements and index is missing (Item 1, Item 1A risk factors, MD&A, notes). The 10-Q cash flow statement is also truncated, so H1 2026 operating cash flow, capex, FCF, D&A and working capital are unavailable. The latest FCF figure is FY2025.
- **Third-party-sourced claims.** The capex guidance above $25B, Q2 FCF of about -$1.1B, the $30B borrowing capacity and the delivery figures come from earnings-call summaries and news sources, not audited filings. Some skeptic remarks rely on recollection (for example, the Q3 2025 delivery peak of about 497k) and are unverified.
- **Undisclosed items.** These include the nature of "Other income, net," the "acquisitions" share issuance, the SpaceX/AI-hardware/X.AI items, debt terms and the warehouse line, and the settlement.
- **Share count ambiguity.** There is a gap of about 700M shares between outstanding (3.95B) and Q2 weighted-average basic (3.24B) shares. Per-share and multiple calculations vary by up to about 22% depending on the denominator.
- **Debt reconciliation.** Market-data total debt of $16.08B does not reconcile to the 10-Q's $9.342B. The difference may be operating leases, which is unconfirmed. Total cash reconciles.
- **EPS basis.** Consensus and "actual" EPS on Yahoo appear to be on a different basis than GAAP (Q1 FY26 "actual" $0.41 versus implied GAAP of about $0.13). The 176.2x forward P/E implies EPS of about $2.14, which equals the 2027 estimate, not the 2026 estimate ($1.74).
- **Consensus revenue.** The $106.59B 2026 consensus implies H2 growth of only about 5.6% (skeptic's calculation), which conflicts with the "accelerating revenue" framing in the growth rationale.
- **Missing inputs.** Management guidance, segment profitability, peer benchmarks, TTM FCF and consensus FCF/capex/margin forecasts are unavailable. The Yahoo page loaded with an error message.
- **Model limits.** The DCF excludes option value from autonomy and robotics and uses a stale FCF base. The 8% base growth rate is low-confidence.

---

## 9. Sources

- Market data: price, multiples, FCF/OCF/capex/revenue/income history, ROE, debt/equity.
- SEC 10-K (filed 2026-01-29) and SEC 10-Q (filed 2026-07-23), excerpts only.
- Yahoo Finance – Analysis (consensus revenue and EPS, revisions, earnings history).
- Earnings-call summaries and news coverage:
  - https://www.marketbeat.com/instant-alerts/tesla-q2-earnings-call-highlights-2026-07-22/
  - https://www.gurufocus.com/news/8973433/tesla-inc-tsla-q2-2026-earnings-call-highlights-record-deliveries-and-strategic-investments-amid-margin-pressures
  - https://www.notateslaapp.com/news/4481/summary-of-teslas-2026-q2-earnings-call-cybercab-fsd-ai4-and-more
  - https://www.theglobeandmail.com/investing/markets/stocks/TSLA/pressreleases/3667292/tesla-tsla-q2-2026-earnings-call-transcript/
  - https://www.fool.com/earnings/call-transcripts/2026/08/05/tesla-tsla-q2-2026-earnings-call-transcript/
  - https://www.investing.com/news/transcripts/earnings-call-transcript-tesla-q2-2026-revenue-beats-eps-misses-as-stock-falls-93CH-4807216
  - https://www.shacknews.com/article/150109/tesla-tsla-q2-2026-earnings-call-transcript
  - https://finance.yahoo.com/quote/TSLA/earnings/TSLA-Q2-2026-earnings_call-653184.html
- Moat and analysis:
  - https://www.gurufocus.com/term/moat-score/TSLA
  - https://norrisai.us/analysis/tsla-q2-2026/
- Governance:
  - https://electrek.co/2026/04/27/tesla-files-deliver-elon-musk-56-billion-pay-package-shares/
  - https://fortune.com/2025/10/17/tesla-shareholders-should-reject-musks-1-trillion-pay-package-iss-advises
  - https://poole.ncsu.edu/thought-leadership/article/inside-the-implications-of-musks-massive-deal/
- Industry:
  - https://www.iea.org/reports/global-ev-outlook-2026/trends-in-electric-cars
  - https://electriccarsreport.com/2026/07/byd-overtakes-tesla-again-in-q2-2026-ev-sales-as-global-gap-widens/
  - https://www.technobaboy.com/2026/08/28/tesla-byd-geely-lead-global-ev-market-in-q2-2026/
  - https://recharged.com/articles/electric-car-market-trends-2026/
- Company exhibit: https://www.sec.gov/Archives/edgar/data/0001318605/000162828026049213/exhibit991.htm

---

*This report is research support only and is not financial advice. The author is not a licensed financial advisor. The recommendation was set by a rule engine, and all figures are as supplied. Independent verification and professional advice are recommended before making any investment decision.*