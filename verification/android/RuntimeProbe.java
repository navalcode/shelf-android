package dev.navalcode.shelf.verification;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.FrameMetrics;
import android.view.Window;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observes the installed R8 app through public Android APIs, without replacing its graph. */
public final class RuntimeProbe extends Instrumentation {
    private static final String PACKAGE = "dev.navalcode.shelf";
    private final List<long[]> frames = new CopyOnWriteArrayList<>();
    private final HandlerThread metricsThread = new HandlerThread("shelf-frame-metrics");
    private volatile Activity activity;
    private volatile boolean measuring;
    private volatile long droppedReports;
    private UiAutomation automation;
    private Bundle arguments;
    private Window observedWindow;
    private float refreshRate;
    private final Window.OnFrameMetricsAvailableListener metrics =
            (window, frame, dropped) -> {
                if (!measuring || frame.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 1) return;
                long deadline =
                        Build.VERSION.SDK_INT >= 31 ? frame.getMetric(FrameMetrics.DEADLINE) : -1;
                frames.add(
                        new long[] {
                            frame.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP),
                            frame.getMetric(FrameMetrics.TOTAL_DURATION),
                            deadline
                        });
                droppedReports += dropped;
            };

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        arguments = args == null ? new Bundle() : args;
        metricsThread.start();
        Application app = (Application) getTargetContext().getApplicationContext();
        app.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityResumed(Activity current) {
                        activity = current;
                        if (observedWindow == current.getWindow()) return;
                        if (observedWindow != null)
                            observedWindow.removeOnFrameMetricsAvailableListener(metrics);
                        observedWindow = current.getWindow();
                        refreshRate =
                                current.getWindowManager().getDefaultDisplay().getRefreshRate();
                        observedWindow.addOnFrameMetricsAvailableListener(
                                metrics, new Handler(metricsThread.getLooper()));
                    }

                    @Override
                    public void onActivityCreated(Activity a, Bundle b) {}

                    @Override
                    public void onActivityStarted(Activity a) {}

                    @Override
                    public void onActivityPaused(Activity a) {}

                    @Override
                    public void onActivityStopped(Activity a) {}

                    @Override
                    public void onActivitySaveInstanceState(Activity a, Bundle b) {}

                    @Override
                    public void onActivityDestroyed(Activity a) {}
                });
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        try {
            automation = getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            AccessibilityServiceInfo service = automation.getServiceInfo();
            service.flags |=
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                            | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            automation.setServiceInfo(service);
            Bundle ready = new Bundle();
            ready.putString("stream", "SHELF_PROBE_READY\n");
            sendStatus(
                    1, ready); // The caller brings only Shelf to the foreground via its launcher.
            waitForNode("search_input", 45_000);
            JSONObject report = new JSONObject();
            report.put("package", PACKAGE);
            report.put("api", Build.VERSION.SDK_INT);
            report.put("search_input_accessible", true);
            report.put(
                    "graph", "Unmodified installed application; public platform inspection only");
            saveScreenshot("optimized-startup.png");
            report.put("startup_accessibility", describeTree(root()));
            if ("true".equals(arguments.getString("journey"))) {
                int cycles = Integer.parseInt(arguments.getString("cycles", "20"));
                if (cycles < 1 || cycles > 50)
                    throw new IllegalArgumentException("cycles must be1..50");
                Bundle text = new Bundle();
                text.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Dune");
                if (!waitForNode("search_input", 5_000)
                        .performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, text))
                    throw new IllegalStateException("Search input rejected accessibility set-text");
                waitForBook(35_000);
                for (int i = 0; i < 3; i++) roundTrip(); // Unmeasured warm-up.
                frames.clear();
                droppedReports = 0;
                measuring = true;
                long started = SystemClock.elapsedRealtime();
                for (int i = 0; i < cycles; i++) roundTrip();
                measuring = false;
                report.put("measurement_elapsed_ms", SystemClock.elapsedRealtime() - started);
                report.put("round_trips", cycles);
                report.put("warmup_round_trips", 3);
                report.put("refresh_rate_at_resume_hz", refreshRate);
                report.put("dropped_frame_metric_reports", droppedReports);
                report.put(
                        "metric",
                        "Window.OnFrameMetricsAvailableListener; TOTAL_DURATION and per-frame"
                            + " DEADLINE");
                JSONArray samples = new JSONArray();
                for (long[] frame : frames)
                    samples.put(new JSONArray().put(frame[0]).put(frame[1]).put(frame[2]));
                if (samples.length() == 0)
                    throw new IllegalStateException("No measured native frames");
                report.put(
                        "frame_columns",
                        new JSONArray()
                                .put("intended_vsync_ns")
                                .put("total_duration_ns")
                                .put("deadline_ns"));
                report.put("frames", samples);
                report.put(
                        "limits",
                        "Warm navigation of one live-API book on one device."
                            + " Instrumentation/inspection overhead present; no universal FPS or"
                            + " cold-start benchmark.");
                saveScreenshot("optimized-after-journey.png");
            }
            write("result.json", report.toString(2));
            result.putString("stream", "SHELF_PROBE_PASSED\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", "SHELF_PROBE_FAILED " + error + "\n");
            finish(Activity.RESULT_CANCELED, result);
        } finally {
            measuring = false;
            metricsThread.quitSafely();
        }
    }

    private AccessibilityNodeInfo root() {
        Activity current = activity;
        if (current == null || !current.hasWindowFocus()) return null;
        AccessibilityNodeInfo active = automation.getRootInActiveWindow();
        if (belongsToShelf(active)) return active;
        for (AccessibilityWindowInfo window : automation.getWindows()) {
            AccessibilityNodeInfo candidate = window.getRoot();
            if (belongsToShelf(candidate)) return candidate;
        }
        return null;
    }

    private boolean belongsToShelf(AccessibilityNodeInfo node) {
        return node != null
                && PACKAGE.contentEquals(
                        node.getPackageName() == null ? "" : node.getPackageName());
    }

    private AccessibilityNodeInfo find(AccessibilityNodeInfo node, String id, boolean book) {
        if (node == null) return null;
        String resource = node.getViewIdResourceName();
        if (resource != null && (book ? resource.startsWith("book_") : resource.equals(id)))
            return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = find(node.getChild(i), id, book);
            if (found != null) return found;
        }
        return null;
    }

    private AccessibilityNodeInfo waitForNode(String id, long timeout) throws Exception {
        long end = SystemClock.elapsedRealtime() + timeout;
        do {
            AccessibilityNodeInfo node = find(root(), id, false);
            if (node != null) return node;
            Thread.sleep(100);
        } while (SystemClock.elapsedRealtime() < end);
        throw new IllegalStateException("Shelf accessible node not found: " + id);
    }

    private AccessibilityNodeInfo waitForBook(long timeout) throws Exception {
        long end = SystemClock.elapsedRealtime() + timeout;
        do {
            AccessibilityNodeInfo book = find(root(), null, true);
            if (book != null) return book;
            Thread.sleep(100);
        } while (SystemClock.elapsedRealtime() < end);
        throw new IllegalStateException("Live API did not expose an accessible book");
    }

    private void roundTrip() throws Exception {
        AccessibilityNodeInfo book = waitForBook(5_000);
        if (!book.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            throw new IllegalStateException("Book action rejected");
        waitForNode("detail_screen", 5_000);
        Thread.sleep(450);
        if (!waitForNode("back", 5_000).performAction(AccessibilityNodeInfo.ACTION_CLICK))
            throw new IllegalStateException("Back action rejected");
        waitForNode("search_input", 5_000);
        Thread.sleep(450);
    }

    private JSONArray describeTree(AccessibilityNodeInfo node) throws Exception {
        JSONArray entries = new JSONArray();
        appendNode(entries, node);
        return entries;
    }

    private void appendNode(JSONArray entries, AccessibilityNodeInfo node) throws Exception {
        if (node == null || !belongsToShelf(node)) return;
        JSONObject item = new JSONObject();
        item.put("id", node.getViewIdResourceName());
        item.put("text", node.getText());
        item.put("description", node.getContentDescription());
        item.put("class", node.getClassName());
        item.put("clickable", node.isClickable());
        entries.put(item);
        for (int i = 0; i < node.getChildCount(); i++) appendNode(entries, node.getChild(i));
    }

    private void write(String name, String text) throws Exception {
        try (FileOutputStream out =
                new FileOutputStream(
                        new File(getTargetContext().getExternalFilesDir(null), name))) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void saveScreenshot(String name) throws Exception {
        Bitmap bitmap = automation.takeScreenshot();
        if (bitmap == null) throw new IllegalStateException("Native screenshot unavailable");
        try (FileOutputStream out =
                new FileOutputStream(
                        new File(getTargetContext().getExternalFilesDir(null), name))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            bitmap.recycle();
        }
    }
}
