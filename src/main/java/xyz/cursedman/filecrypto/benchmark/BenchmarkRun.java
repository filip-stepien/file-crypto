package xyz.cursedman.filecrypto.benchmark;

import java.util.List;

/*
 * results: only fully completed measurements (skipped ones are interrupted and leave nothing behind)
 * cancelled: whole run was cancelled before finishing
 */
public record BenchmarkRun(List<BenchmarkResult> results, boolean cancelled) {
}
