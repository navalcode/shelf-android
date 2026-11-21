"""Protect evidence interpretation: missing deadlines must never look like zero jank."""
import unittest
from summarize_frame_metrics import summarize


class FrameMetricsTests(unittest.TestCase):
    def report(self, frames):
        return {"frame_columns": ["intended_vsync_ns", "total_duration_ns", "deadline_ns"],
                "frames": frames, "api": 34, "round_trips": 20, "warmup_round_trips": 3,
                "dropped_frame_metric_reports": 0, "limits": "One physical device"}

    def test_exact_deadline_is_not_an_overrun(self):
        result = summarize(self.report([[0, 10_000_000, 10_000_000], [1, 10_000_001, 10_000_000]]))
        self.assertEqual(1, result["frames_exceeding_own_deadline"])
        self.assertEqual(50, result["over_deadline_percent"])

    def test_missing_deadlines_are_unknown_not_zero(self):
        result = summarize(self.report([[0, 20_000_000, -1]]))
        self.assertEqual(0, result["frames_with_known_deadline"])
        self.assertIsNone(result["over_deadline_percent"])

    def test_idle_gap_does_not_change_frame_duration(self):
        result = summarize(self.report([[0, 2_000_000, 10_000_000], [50_000_000_000, 3_000_000, 10_000_000]]))
        self.assertEqual(3, result["duration_ms"]["p95"])
        self.assertNotIn("fps", result)

    def test_empty_or_nonpositive_samples_are_rejected(self):
        for frames in [[], [[0, 0, 10_000_000]]]:
            with self.assertRaises(ValueError):
                summarize(self.report(frames))

    def test_phase_batches_cannot_be_mixed_with_different_frames(self):
        report = self.report([[0, 20_000_000, 33_333_333]])
        report.update(phase_columns=["draw_ns"], phase_frames=[])
        with self.assertRaises(ValueError):
            summarize(report)

    def test_unavailable_gpu_phase_is_not_zero_duration(self):
        report = self.report([[0, 20_000_000, 33_333_333]])
        report.update(phase_columns=["gpu_ns"], phase_frames=[[-1]])
        result = summarize(report)
        self.assertIsNone(result["phases_ms"]["gpu"]["p95"])
        self.assertEqual(0, result["phases_ms"]["gpu"]["known_samples"])


if __name__ == "__main__":
    unittest.main()
