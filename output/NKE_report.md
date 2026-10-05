# NIKE, Inc. (NKE): Research Report

**Recommendation: INVEST (long term)**, fixed by the rule engine. This report explains the reasoning and the uncertainties. It does not change any numbers.

**Price:** $33.315 | **Market cap:** $49.42B | **Currency:** USD (Market Data)

---

## 1. Executive summary

The rule engine returns INVEST (long term). Every rule check passed:

- **Margin of safety:** 34.9% (required check passed).
- **Bear case:** not catastrophic, at -8.3% versus the current price.
- **Debt, ROE and cash conversion:** all OK.
- **Skeptic review:** severity "minor", so the check passed.

The case rests on three points:

1. NIKE is the world's largest athletic footwear and apparel company, with an A+/A2 balance sheet and clean audit opinions (SEC 10-K, 2026-07-15).
2. Earnings and cash flow are at a cyclical low. FY26 EBIT margin was 8.3% against 12.7% in FY24, and FCF was $2.18B against $6.62B in FY24 (SEC 10-K; Market Data).
3. The base-case DCF gives $51.16 per share, 53.6% above the price.

**The main uncertainty is the valuation base.** The DCF starts from $4.023B of FCF, which is the 3-year average of FY24–FY26 and 84% above the latest year's $2.184B. The skeptic review scaled the outputs roughly linearly to the actual $2.18B and estimated a base value near $28 (about -17%) and a bear value near $17 (about -50%). These are the skeptic's approximations, not model outputs. The rule engine still passed the margin-of-safety check on the model's figures.

Near-term evidence is weak:

- Q1 FY27 revenue fell 4%.
- Q1 FCF was about -$64M (my computation from OCF less capex).
- Dividends exceeded FCF in FY26.
- Management guides FY27 revenue down high single digits (qualitative research; some figures come from secondary sources).

This is a long-term, patient position on a turnaround. It is not a near-term catalyst call.

---

## 2. Business overview

NIKE designs, develops, markets and sells athletic footwear, apparel, equipment, accessories and services under the NIKE, Jordan and Converse brands. It sells through NIKE Direct (owned stores and digital) and wholesale. Nearly all products are made by contract manufacturers. Vietnam, Indonesia and China account for about 52%, 27% and 16% of NIKE Brand footwear (SEC 10-K, 2026-07-15).

**Current situation:** management is midway through a product, marketplace and brand reset. The actions are:

- Cutting supply of certain footwear (Dunk supply cut nearly 50%, per qualitative research).
- Repositioning digital as full-price.
- Reinvesting in wholesale.
- Liquidating inventory through markdowns.

Management expects most reset actions to be complete by about December 2026. Sportswear and Jordan actions extend beyond FY27 (SEC 10-Q, 2026-10-02).

**Weak spots:** Q1 FY27 Greater China was -26% currency-neutral and Converse -28%. Management says China will worsen before it improves (SEC 10-Q).

**Strong spots:** North America FY26 revenue was +5% with EBIT +14%, and Q1 FY27 wholesale was +9% (SEC 10-K; 10-Q). Note that the North America EBIT figure includes a $965M tariff-refund benefit (see Section 6).

**Cost program:** in October 2026 NIKE announced "Pace". It carries about $1.0B of pre-tax charges (about $0.3B in FY27) and targets about $2.5B of cumulative savings through FY31. The savings are stated before reinvestment (SEC 10-Q).

---

## 3. Financial snapshot

| Metric | FY2026 (May 31, 2026) | Prior periods | Source |
|---|---|---|---|
| Revenue | $46,398M | FY25 $46,309M; FY24 $51,362M | SEC 10-K |
| Gross margin | 42.9% | FY25 42.7%; FY24 44.6% | SEC 10-K |
| Operating income | $3,797M | FY25 $3,702M; FY24 $6,311M | Market Data / 10-K |
| EBIT margin (non-GAAP) | 8.3% | FY24 12.7% | SEC 10-K |
| Net income / diluted EPS | $3,108M / $2.10 | FY25 $3,219M / $2.16; FY24 $5,700M / $3.73 | SEC 10-K |
| Operating cash flow | $2,868M | FY25 $3,698M; FY24 $7,429M | SEC 10-K |
| Capex | $684M | FY25 $430M; FY24 $812M | SEC 10-K |
| Free cash flow | $2,184M | FY25 $3,268M; FY24 $6,617M; FY23 $4,872M | Market Data / 10-K |
| Dividends paid | $2,407M | n/a | SEC 10-K |
| Stock-based compensation | $715M | n/a | SEC 10-K |
| ROIC (company-defined, non-GAAP) | 18.7% | prior year 20.2% | SEC 10-K |

**Latest quarter (Q1 FY27, ended Aug 31, 2026; SEC 10-Q):**

- Revenue was $11,213M against $11,720M a year earlier, down 4% reported and 5% currency-neutral.
- Gross margin was 42.8%, up 60 bps.
- Net income was $712M and diluted EPS $0.48.
- OCF was $135M and capex $199M, so FCF was about -$64M (computed).

**Balance sheet (Aug 31, 2026; SEC 10-Q):**

- Cash and short-term investments were $8,368M.
- Long-term debt was $5,893M plus a $2,000M current portion.
- Operating lease liabilities were $473M current plus $2,706M long-term.
- There were no borrowings under credit facilities.
- Ratings are A+ (S&P) and A2 (Moody's). Both were downgraded in 2025 (SEC 10-K).

By my arithmetic, debt excluding leases ($7,893M) is slightly below cash, so NIKE is marginally net cash. Including leases ($11,072M), it is net debt of about $2.7B. The skeptic cites about $2.8B using the Market Data debt figure.

**Multiples (Market Data):** trailing P/E 15.86x, forward P/E 18.96x, EV/EBITDA 10.85x, P/B 3.32x.

**Shares:** 1,485,308,829 total Class A and B as of Sept 28, 2026 (SEC 10-Q).

---

## 4. Valuation

The DCF is a free-cash-flow model. **Base FCF used: $4.023B.** The current price is $33.315. The model's reliability flag is `true` with no notes.

| Scenario | FCF growth | Discount rate | Terminal growth | Intrinsic value / share | Upside vs. price |
|---|---|---|---|---|---|
| Bear | 1% | 12% | 2.0% | $30.53 | -8.3% |
| **Base** | **5%** | **10%** | **3.0%** | **$51.16** | **+53.6%** |
| Bull | 8% | 9% | 3.5% | $73.77 | +121.4% |

**Margin of safety:** 34.9%. This is consistent with (base value - price) / base value = ($51.16 - $33.315) / $51.16.

**Growth rationale (analyst judgment, not guidance):** the 5% rate is moderate and applied to a trough base. No FCF guidance or analyst estimates were available. The filings analysis states that results depend more on the starting FCF normalization than on the growth rate.

**Arguments for growth:**
- FCF is about one third of FY24's level.
- EBIT margin is 8.3% against 12.7% in FY24.
- North America is recovering.
- Pace savings are targeted.
- About $684M of IEEPA tariff refunds were collected after year-end.

**Arguments for restraint:**
- Q1 FCF was negative.
- China and Converse are declining sharply.
- The Sportswear and Jordan resets extend beyond FY27.
- Pace carries mostly cash charges.
- Dividends exceed FCF.

**Key valuation caveat (skeptic):**
- The $4.023B base equals the exact 3-year average of FY24–FY26 FCF, (6.617 + 3.268 + 2.184) / 3. It is 84% above the latest year and leans on the FY24 peak.
- All three scenarios share this base, so even the bear case implies FCF roughly doubling from FY26.
- At $2.18B, the skeptic estimates roughly $28 base and $17 bear.
- The filings analysis says to treat $2.18B as a floor and consider normalizing the base. FY26 FCF was depressed by about $1.2B of working-capital outflow and tax payments, including a $268M final transition-tax installment and $260M of expected IRS resolution payments (SEC 10-K).
- The skeptic questions whether the per-share value uses the right share count. The Market Data "shares" field (1.202B) is Class B only, and the correct total is about 1.485B. The valuation output does not state which count was used. If 1.202B leaked into the model, per-share upside would be overstated by about 24% (skeptic estimate; unverified).

Treat the base-case upside as conditional on FCF normalizing toward the $4B region. It is not a forecast of FY27.

---

## 5. Moat and management

**Moat: narrow** (qualitative research).

- **Sources of moat:**
  - Global brand equity across NIKE, Jordan and Converse.
  - Scale in marketing and endorsements, with about $15.5B of endorsement commitments (SEC 10-K).
  - Performance innovation: the Performance portfolio was about $16B in FY26.
  - North America strength and first-party consumer data.
  - The NFL uniform and licensing deal runs through 2038.
- **Erosion:**
  - Switching costs are negligible.
  - Revenue fell from $51.4B to $46.4B between FY24 and FY26, and EBIT margin fell from 12.7% to 8.3%.
  - On, Hoka, Adidas and New Balance are gaining share, and NIKE's apparel share fell about 30 bps in 2025.
  - Market-share figures come from single-point, partly low-quality sources and are unverified.
- **ROIC:** 18.7% is company-defined and non-GAAP. The skeptic notes the numerator may include the one-off tariff benefit.

**Management: average** (qualitative research).

- CEO Elliott Hill, a NIKE insider of more than 32 years, returned in October 2024. He has reorganized the company by sport, rebuilt wholesale ties and cut inventory.
- **Capital allocation is a weakness.** The buyback program repurchased 124.4M shares at an average of $97.57 (about $12.1B) against a price near $33. Repurchases have been paused since Q1 FY26, with about $5.9B still authorized (SEC 10-K; 10-Q). FY26 dividends ($2,407M) exceeded FCF ($2,184M).
- **Governance concerns:**
  - The dual-class structure gives Class A holders (mainly Swoosh, LLC) their own director elections. Three of the eight Class A nominees are not independent (proxy).
  - There is turnover at the CFO and COO positions.
  - Oversight of the prior DTC-heavy strategy is a question.
  - SBC was $715M, about 23% of net income and about 33% of FY26 FCF.
- Auditor PwC gave clean opinions and ICFR was effective (SEC 10-K). The turnover is a continuity flag, not a control failure.

---

## 6. Risks and bear case

**Stated risks (SEC 10-K; 10-Q):**
- Tariffs and trade policy. The IEEPA tariffs were ruled unauthorized and NIKE recognized a $986M recovery benefit, but further policy changes are possible.
- Weakness in Greater China and Converse, expected through FY27.
- Digital and store traffic declines (FY26 digital -12% currency-neutral, comparable stores -4%).
- Intense competition, including AI-enabled shopping disruption.
- Supply concentration: four footwear contract manufacturers make about 60% of NIKE Brand footwear.
- Inventory and discounting pressure (inventory +5% since May 31, 2026).
- Pace execution risk.
- Leadership transition.
- Legal and tax exposure: the Belgian customs claim has an unestimable loss that could be material, the EU State Aid investigation is open, IRS audits of FY2017–2023 are ongoing, and there are securities class actions.
- Credit rating downgrades.
- FX tailwind fading: FY26 revenue benefit was about $1,023M and pre-tax benefit about $184M.
- Swoosh, LLC control and overhang.

**Skeptic bear case (severity: minor):**
1. **Cash flow is still falling.** FCF fell 67% over two years while net income fell 45%. Q1 FCF was about -$64M. Cash and short-term investments fell from $9.0B to $8.4B in one quarter.
2. **The dividend is not covered.** Q1 dividends were $610M against OCF of $135M. The skeptic calculates the dividend at about $1.64 a year, roughly a 4.9% yield. That is 120–140% of the unverified $1.15–$1.35 adjusted EPS guidance, which comes from a single secondary source. A $2.0B debt maturity is also current.
3. **Earnings quality questions:**
   - The $986M IEEPA benefit was non-cash and booked in cost of sales, with $684M still in receivables at year-end. The 10-K says it "largely offset" tariffs recognized in FY26, so the net margin effect cannot be isolated.
   - Receivables rose 26% on flat revenue.
   - Sales-related reserves fell 13% and the inventory reserve fell from $233M to $213M, while returns and discounts rose and inventory increased.
4. **Margin quality.** Q1 gross margin gains came from logistics (+90 bps) and FX (+40 bps), while ASP was -30 bps.
5. **Recurring restructuring:** $443M in FY24, $385M in FY26, and about $1.0B more through Pace.
6. **Other accounting items:**
   - Effective tax rate volatility (Q1 FY27 22.7%).
   - $953M of gross unrecognized tax benefits plus $438M of interest and penalties.
   - Supplier finance of about $1.1B inside $3.6B of payables.
   - Options (76.8M) with a $96.44 weighted-average exercise price, well above the share price.
7. **The multiple is not cheap on trough earnings.** On the unverified guidance, the skeptic estimates about 25–29x P/E.

**Why severity is "minor":** the skeptic judged this to be a valuation and timing problem, not accounting fraud or solvency risk. NIKE has a large franchise, an A+/A2 balance sheet, no facility borrowings and clean audits. It is a "show me" name until FCF turns.

---

## 7. Rule-by-rule decision

| Rule | Result | Detail |
|---|---|---|
| Margin of safety | **Pass** | 34.9% on the base-case intrinsic value ($51.16 vs. $33.315). |
| Bear case not catastrophic | **Pass** | Bear value $30.53 is -8.3% vs. price. See the caveat that all scenarios share the $4.023B base. |
| Debt | **Pass** | No credit-facility borrowings, $8.4B cash and short-term investments, A+/A2. Net cash excluding leases, net debt of about $2.7B including leases (my computation). |
| ROE | **Pass** | The engine reports a pass without a figure. As my rough cross-check, P/B of 3.32 divided by trailing P/E of 15.86 implies ROE near 21%. Company-defined ROIC is 18.7%. |
| Cash conversion | **Pass** | FCF conversion 0.70 (FCF $2,184M / net income $3,108M). The skeptic notes net income includes the non-cash $986M refund benefit. |
| Skeptic review | **Pass** | Severity "minor". |
| Accounting red flags | **Count: 11** | Listed in the filings analysis and summarized in Section 6. The engine passed the other checks despite this count. |

**Conclusion:** all rule checks passed, so the engine's outcome is INVEST (long term).

---

## 8. Data gaps and caveats

- **Share count:** the Market Data "shares" field (1,202,110,951) is Class B only. The market cap uses about 1.4835B total shares. The valuation output does not state which count was used.
- **Total debt:** Market Data shows $11.125B, which matches no balance-sheet sum ($11,072M at Aug 31, 2026; about $11,033M at May 31, 2026).
- **No guidance or estimates:** no FY27 FCF guidance and no analyst estimates (the Yahoo Finance estimate fields are empty), so the 5% growth rate is judgment.
- **Unverified guidance:** the $1.15–$1.35 adjusted EPS guidance comes from a single secondary source. The forward P/E of 18.96x implies EPS near $1.76, which conflicts with it.
- **FY23 data:** it is from Market Data only, with no filing in the provided documents.
- **Tariff effects:** the net effect of IEEPA costs versus the $986M refund on FY26 margin is not disclosed. Exact refund receipts after Aug 31, 2026 are also not stated. The skeptic notes Q1 OCF of only $135M despite the expected refund inflow, and this is unreconciled.
- **Q1 FCF** (about -$64M) is my computation, not a company-reported figure.
- **Pace:** cash costs beyond FY27 and savings realization are not detailed.
- **CFO transition:** the 8-K and offer-letter details are not in the provided text.
- **No peer valuation comparables** were provided.
- **Model reliability:** the "reliable: true" flag with empty notes sits uneasily with the gaps above, as the skeptic notes.
- **Unverified qualitative sources:** market-share and industry-growth figures (for example, about 7% sportswear growth) rely on secondary sources.

---

## 9. Sources

**Filings and market data:**
- SEC 10-K for FY2026 (filed 2026-07-15)
- SEC 10-Q for Q1 FY27 (filed 2026-10-02)
- Market Data (price, multiples, FCF and operating income history)
- NIKE 2026 proxy statement: https://www.sec.gov/Archives/edgar/data/0000320187/000032018726000089/nikecourtesypdf2026.pdf

**Qualitative research and citations:**
- https://finance.yahoo.com/markets/stocks/articles/nike-q1-earnings-call-highlights-000223968.html
- https://ca.investing.com/news/company-news/nike-inc-nke-q1-2027-earnings-call-highlights-performance-gains-offset-by-china-reset-and--4863264
- https://www.fool.com/earnings/call-transcripts/2026/10/02/nike-nke-q1-2027-earnings-call-transcript/
- https://gloom.sh/stocks/nke/transcripts/q1-2027
- https://finance.biggo.com/news/US_NKE_2026-10-01
- https://www.tradingkey.com/news/transcripts/262197031-tradingkey
- https://www.companieshistory.com/nike-swot-analysis/
- https://www.companieshistory.com/adidas-competitors
- https://www.growthnavigate.com/nike-competitors
- https://logos-pres.md/en/news/nike-loses-market-share-and-competitors-gain-ground/
- https://swotpal.com/blog/nike-swot-analysis-2026
- https://about.nike.com/en/magazine/elliott-hill-ceo-of-sport-interview
- https://wwd.com/footwear-news/shoe-industry-news/nike-annual-shareholder-meeting-ceo-elliott-hill-progress-1239201228/
- https://en.wikipedia.org/wiki/Elliott_Hill

---

*This report is research support only and not financial advice. The recommendation was set by a rule engine, and the author is not a licensed financial advisor. Valuation outputs depend on assumptions, especially the starting free-cash-flow base. Verify all figures independently before making any investment decision.*