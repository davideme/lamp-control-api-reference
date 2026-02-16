# Benchmark Summary

Generated: 2026-02-16T07:12:16.258Z

## Memory Pass Ranking

| Rank | Service    | p95 (ms) | p99 (ms) | Avg (ms) | Error Rate | Max Stable RPS |
|------|------------|---------:|---------:|---------:|-----------:|---------------:|
| 1    | typescript |    38.77 |    54.32 |    32.92 |     0.000% |            160 |
| 2    | csharp     |    41.65 |    56.16 |    34.41 |     0.000% |            160 |
| 3    | go         |   155.28 |   180.80 |    72.28 |     0.000% |            100 |
| 4    | java       |   166.76 |   270.08 |    77.69 |     0.000% |            120 |
| 5    | python     |  6308.79 |  6429.67 |  4573.09 |     0.000% |             80 |
| 6    | kotlin     |  6704.49 | 13765.58 |  1356.00 |     0.000% |            120 |

## Cold Start Appendix

| Pass   | Service    | Ready Time (ms) | Attempts | Error Rate | Samples (ok/failed) |
|--------|------------|----------------:|---------:|-----------:|--------------------:|
| memory | go         |          502.00 |        1 |     0.000% |                 1/0 |
| memory | python     |          284.00 |        1 |     0.000% |                 1/0 |
| memory | csharp     |        11722.00 |        1 |     0.000% |                 1/0 |
| memory | typescript |        11545.00 |        1 |     0.000% |                 1/0 |
| memory | java       |        11734.00 |        1 |     0.000% |                 1/0 |
| memory | kotlin     |        11692.00 |        1 |     0.000% |                 1/0 |

## Extreme Load Appendix (1000 RPS)

| Pass | Service | p95 (ms) | p99 (ms) | Avg (ms) | Error Rate |
|---|---|---:|---:|---:|---:|

Raw per-run k6 summaries are under `benchmarks/results/raw/`.
