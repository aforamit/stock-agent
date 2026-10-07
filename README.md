# Stock research agent (Java)

Analyses a stock with a pipeline of agents and monitors a watchlist for BUY / SELL opportunities.
Targets **JDK 25** (`<java.version>` in `pom.xml`). No preview features are used.

## Build
    mvn clean package
    setx ANTHROPIC_API_KEY sk-ant-...            # Linux/macOS: export ANTHROPIC_API_KEY=...
    setx ANTHROPIC_WORKSPACE_ID wrkspc_...       # only if your key is not scoped to a workspace

## Run
    java -jar target/stock-agent-1.0.0.jar <TICKER> | --watchlist  [options]

| Option | Meaning |
|---|---|
| `--tier check` | Cheap check: today's price against the stored valuation. No LLM calls, no API key needed. |
| `--tier full` | Full agent analysis; writes the report and stores the valuation. |
| `--tier auto` | Check first, run the full analysis only if a trigger fires. Default (`monitor.default_tier`). |
| `--refresh` | Full analysis re-reads the filings instead of reusing the stored filings analysis. |
| `--config FILE` | Config file. Default: `config.yaml` in the working directory, else the one bundled in the jar. |
| `--out DIR` | Report directory. Default `work/output`. |

Examples:

    java -jar target/stock-agent-1.0.0.jar NKE --tier check    # price vs stored valuation, no LLM, no API key
    java -jar target/stock-agent-1.0.0.jar NKE --tier full     # full agent analysis
    java -jar target/stock-agent-1.0.0.jar NKE                 # auto: check, then full only if a trigger fires
    java -jar target/stock-agent-1.0.0.jar --watchlist         # every ticker in the watchlist


## Architecture

The app is layered: `Main` hands each ticker to `Monitor`, which uses three groups of classes. Only the
middle group calls the LLM; data collection and every rule (valuation, recommendation, signal) are plain Java.

```text
+-----------------------------------------------------------------------------+
| ENTRY          Main - command line, or Task Scheduler via run-watchlist.cmd |
|                one ticker or --watchlist; --tier check | full | auto        |
+-----------------------------------------------------------------------------+
| ORCHESTRATION  Monitor - picks the tier, checks triggers, runs the agents,  |
|                confirms signal changes                                      |
+------------------------+------------------------+---------------------------+
| DATA (no LLM)          | LLM AGENTS             | RULES (no LLM)            |
|                        |                        |                           |
| DataCollector          | Agents                 | Valuation                 |
| YahooClient            |   filings analyst      |   DCF bear/base/bull      |
| WebFetcher             |   qualitative analyst  |   INVEST/WATCHLIST/AVOID  |
| PdfText                |   skeptic panel        | Signal                    |
|                        |   synthesizer          |   BUY/SELL/HOLD/WATCH/    |
|                        | Llm                    |   AVOID                   |
|       |                |       |                |                           |
|       v                |       v                |                           |
| Yahoo Finance          | Anthropic Messages API | nothing external          |
| SEC EDGAR              | + web search tool      |                           |
| configured websites    |                        |                           |
+------------------------+------------------------+---------------------------+
| PERSISTENCE    State  work/state/<TICKER>.json, signals.csv                 |
|                Log    work/logs/<run>.log, alerts.log                       |
|                Report work/output/<TICKER>_report.md, _raw.json             |
+-----------------------------------------------------------------------------+
```

### Agent flow and tiers

A run has two tiers. **Tier 1 (check)** is the cheap pass and makes no LLM calls. **Tier 2 (full analysis)**
is the agent pipeline. `--tier check` stops after Tier 1, `--tier full` goes straight to Tier 2, and `--tier auto`
runs Tier 1 and continues to Tier 2 only when a trigger fires. The signal step runs after either tier.
Each step is tagged `[code]` (no LLM) or `[LLM xN]` (N model calls).

```text
                 for each ticker (one, or every watchlist entry)
                                    |
                                    v
          SEC EDGAR: ids of the latest 10-K / 10-Q          [code]
                                    |
          --tier check / auto       |       --tier full
         +--------------------------+---------------------------------+
         |                                                            |
         v                                                            |
+==================================================================+  |
| TIER 1 - CHECK                     no LLM calls, no API key      |  |
|                                                                  |  |
| Yahoo Finance: current price                          [code]     |  |
|      |                                                           |  |
|      v                                                           |  |
| Triggers: no stored analysis / analysis too old /     [code]     |  |
|           new 10-K or 10-Q / big price move                      |  |
+========+=================================+=======================+  |
         |                                 |                          |
         | no trigger,                     | trigger fired            |
         | or --tier check                 | and --tier auto          |
         |                                 v                          v
         |   +==================================================================+
         |   | TIER 2 - FULL ANALYSIS             4 LLM agents, up to 6 calls   |
         |   |                                                                  |
         |   | Agent 1  Data collector                               [code]     |
         |   |          Yahoo fundamentals, 10-K/10-Q text, websites, PDFs      |
         |   |      |                                                           |
         |   |      v                                                           |
         |   | Agent 2  Filings analyst                              [LLM x1]   |
         |   |          skipped while there is no new 10-K/10-Q:                |
         |   |          the stored filings analysis is reused                   |
         |   |      |                                                           |
         |   |      +---------------------------+                               |
         |   |      v                           v                               |
         |   | Agent 3  Valuation  [code]    Agent 4  Qualitative    [LLM x1]   |
         |   |          DCF bear/base/bull              analyst + web search    |
         |   |      |                           |                               |
         |   |      +-------------+-------------+                               |
         |   |                    v                                             |
         |   | Agent 5  Skeptic panel + web search                   [LLM x3]   |
         |   |          3 parallel runs, the median severity decides            |
         |   |      |                                                           |
         |   |      v                                                           |
         |   | Decision rule: INVEST / WATCHLIST / AVOID             [code]     |
         |   |          analysis and valuation saved to work/state              |
         |   |      |                                                           |
         |   |      v                                                           |
         |   | Agent 6  Synthesizer                                  [LLM x1]   |
         |   |          writes work/output/<TICKER>_report.md                   |
         |   +================================+=================================+
         |                                    |
         v                                    v
+===============================================================================+
| SIGNAL - runs after either tier    no LLM calls                               |
|                                                                               |
| Signal rules: stored valuation vs price               [code]                  |
|      -> BUY / SELL / HOLD / WATCH / AVOID                                     |
|      |                                                                        |
|      v                                                                        |
| Confirmation: a changed signal must repeat on         [code]                  |
|      confirm_runs consecutive runs                                            |
|      |                                                                        |
|      +--> work/logs/alerts.log      BUY / SELL changes, once confirmed        |
|      +--> work/state/signals.csv    every evaluation                          |
|      +--> work/state/<TICKER>.json  current signal                            |
+===============================================================================+
```

| | Tier 1 - check | Tier 2 - full analysis |
|---|---|---|
| Agents run | None | Agents 1 to 6 |
| LLM calls | 0 | Up to 6: filings analyst 1 (0 when reused), qualitative analyst 1, skeptic panel 3, synthesizer 1 |
| Needs `ANTHROPIC_API_KEY` | No | Yes |
| External calls | SEC EDGAR filing index, Yahoo price | SEC EDGAR, Yahoo fundamentals, configured websites, Anthropic API, web search |
| Updates | Signal, `signals.csv` | Stored analysis and valuation, report, then signal and `signals.csv` |

## Monitoring
Edit `watchlist:` and `monitor:` in `config.yaml`. Give a ticker `shares` above 0 to mark it as held:
only held positions can get a SELL signal.

**Triggers** that make `auto` escalate to a full analysis: no stored analysis, analysis older than
`reanalyze_after_days`, a new 10-K/10-Q on SEC EDGAR, or a price move of `price_move_trigger` since the analysis.

**Signals** are decided in code from the stored valuation and today's price:

| Signal | When |
|---|---|
| BUY | Price is below base value by `decision.margin_of_safety`, bear value is acceptable, and all checks pass. |
| SELL | Held, and price is `sell_above_base` above base value, or the skeptic / a quality check fails. |
| HOLD / WATCH | Held / not held, and neither of the above. |
| AVOID | Not held, and a check fails or the stock cannot be valued. |

A changed signal must repeat on `confirm_runs` consecutive runs before it takes effect, and `hysteresis`
keeps a price sitting on a threshold from flipping the signal every day.

**Where things go**

| Path | Content |
|---|---|
| `work/logs/alerts.log` | One line per confirmed change into or out of BUY / SELL. This is the alert channel. |
| `work/logs/<name>_<timestamp>.log` | Full log of one run, including every LLM prompt and reply. |
| `work/state/<TICKER>.json` | Stored analysis, valuation, seen filings and current signal. Delete it to start over. |
| `work/state/signals.csv` | Every signal evaluation with the price at the time, for judging the signals later. |
| `work/output/<TICKER>_report.md` | Latest research report (plus `_raw.json`). |

**Scheduling (Windows)**: `run-watchlist.cmd` runs the watchlist with the config in `src/main/resources`.
To run it on weekdays at 18:00:

    schtasks /Create /TN "StockAgent" /TR "C:\Work\workspace\stock-agent\run-watchlist.cmd" /SC WEEKLY /D MON,TUE,WED,THU,FRI /ST 18:00

## Add websites
Edit `sources:` in config.yaml. Placeholders: `{ticker}`, `{symbol}`, `{company}`. `market:` US | IN | ALL.
Per-ticker PDFs/transcripts go under `extra_documents:`. Sources are fetched in parallel on virtual threads.

## Structure
- `Main`: command line; runs one ticker or the watchlist
- `Monitor`: the two tiers, triggers, signal confirmation and the full pipeline
- `Signal`: BUY / SELL / HOLD / WATCH / AVOID rules, in plain Java
- `State`: per-ticker JSON state and `signals.csv`
- `DataCollector` (agent 1): Yahoo market data, SEC EDGAR 10-K/10-Q, configured websites, PDFs
- `Agents`: filings analyst, qualitative analyst, skeptic panel, synthesizer
- `Valuation` (agent 3): DCF bear/base/bull and the rule-based recommendation, all in plain Java
- `Llm`: Anthropic Messages API over java.net.http (no SDK)
- `Log`: run log and `alerts.log`

## Notes
- Yahoo Finance endpoints are unofficial and may change; replace `YahooClient` with a paid provider for reliability.
- DCF is not meaningful for banks/insurers or negative-FCF companies (returns INSUFFICIENT DATA).
- Research support only, not financial advice.
