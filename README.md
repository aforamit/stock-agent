# Stock research agent (Java)

Java port of the Python project. Targets **JDK 27** (GA 15 Sep 2026). JDK 27 is a short-term release;
for the current LTS change `<java.version>` in `pom.xml` to `25`. No preview features are used.

## Build and run
    mvn clean package
    export ANTHROPIC_API_KEY=sk-ant-...          # Windows: set ANTHROPIC_API_KEY=...
    java -jar target/stock-agent-1.0.0.jar AAPL
    java -jar target/stock-agent-1.0.0.jar RELIANCE.NS --config config.yaml --out output

Copy `src/main/resources/config.yaml` next to the jar to edit it without rebuilding
(a `config.yaml` in the working directory overrides the bundled one).

## Add websites
Edit `sources:` in config.yaml. Placeholders: `{ticker}`, `{symbol}`, `{company}`. `market:` US | IN | ALL.
Per-ticker PDFs/transcripts go under `extra_documents:`. Sources are fetched in parallel on virtual threads.

## Structure
- `DataCollector` (agent 1): Yahoo market data, SEC EDGAR 10-K/10-Q, configured websites, PDFs
- `Agents`: filings analyst, qualitative analyst, skeptic, synthesizer
- `Valuation` (agent 3): DCF bear/base/bull and the rule-based decision, all in plain Java
- `Main`: orchestrator (valuation and qualitative analyst run in parallel)
- `Llm`: Anthropic Messages API over java.net.http (no SDK)

## Notes
- Dependency versions in `pom.xml` are placeholders: bump to the latest on Maven Central.
- Yahoo Finance endpoints are unofficial and may change; replace `YahooClient` with a paid provider for reliability.
- DCF is not meaningful for banks/insurers or negative-FCF companies (returns INSUFFICIENT DATA).
- Research support only, not financial advice.
