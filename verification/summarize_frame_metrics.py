#!/usr/bin/env python3
"""Summarize native FrameMetrics samples without inventing FPS from idle intervals."""
import argparse
import json
import math
from pathlib import Path


def summarize(report):
    if report.get("frame_columns") != ["intended_vsync_ns", "total_duration_ns", "deadline_ns"]:
        raise ValueError("Unexpected frame schema")
    frames = report.get("frames", [])
    if not frames or any(len(frame) != 3 or frame[1] <= 0 for frame in frames):
        raise ValueError("Native samples must contain positive frame durations")
    durations = sorted(frame[1] / 1_000_000 for frame in frames)
    def percentile(value):
        return round(durations[math.ceil(len(durations) * value) - 1], 3)
    known = [frame for frame in frames if frame[2] > 0]
    exceeded = sum(frame[1] > frame[2] for frame in known)
    result = {
        "api": report["api"],
        "round_trips": report["round_trips"],
        "warmup_round_trips": report["warmup_round_trips"],
        "measured_frames": len(frames),
        "duration_ms": {"p50": percentile(.5), "p90": percentile(.9),
                        "p95": percentile(.95), "p99": percentile(.99),
                        "max": round(durations[-1], 3)},
        "percentile_method": "nearest rank",
        "frames_with_known_deadline": len(known),
        "frames_exceeding_own_deadline": exceeded if known else None,
        "over_deadline_percent": round(100 * exceeded / len(known), 3) if known else None,
        "dropped_frame_metric_reports": report["dropped_frame_metric_reports"],
        "scope": report["limits"],
    }
    if "environment" in report:
        result["environment"] = report["environment"]
    if "phase_columns" in report or "phase_frames" in report:
        columns = report.get("phase_columns", [])
        phases = report.get("phase_frames", [])
        if not columns or len(phases) != len(frames) or any(len(row) != len(columns) for row in phases):
            raise ValueError("Phase samples must correspond to the same measured frames")
        result["phases_ms"] = {}
        for index, name in enumerate(columns):
            known_values = sorted(row[index] / 1_000_000 for row in phases if row[index] >= 0)
            result["phases_ms"][name.removesuffix("_ns")] = {
                "known_samples": len(known_values),
                "p95": round(known_values[math.ceil(len(known_values) * .95) - 1], 3) if known_values else None,
            }
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    result = summarize(json.loads(args.report.read_text()))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result))


if __name__ == "__main__":
    main()
