package com.timelapse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBar {
    private final int total;
    private final int barLength;
    private final AtomicInteger current = new AtomicInteger(0);
    private final long startTime = System.currentTimeMillis();

    public ProgressBar(int total, int barLength) {
        this.total = total;
        this.barLength = barLength;
    }

    /** Increment the progress by 1 */
    public void increment() {
        current.incrementAndGet();
    }

    /** Start a thread that periodically redraws the progress bar */
    public void start() {
        final var updater = new Thread(() -> {
            while (current.get() < total) {
                draw();
                try {
                    Thread.sleep(100); // refresh every 100ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            draw(); // final draw at 100%
            System.out.println(); // move to next line
        });
        updater.setDaemon(true); // allow JVM to exit if main finishes
        updater.start();
    }

    private synchronized void draw() {
        final var done = current.get();
        final var filled = (int) (done / (double) total * barLength);
        final var bar = "=".repeat(filled) + " ".repeat(barLength - filled);
        final var percent = done * 100.0 / total;

        // ETA calculation
        final var elapsed = System.currentTimeMillis() - startTime;
        final var remaining = done == 0 ? 0 : (elapsed * (total - done) / done);
        final var eta = String.format("%02d:%02d", remaining / 60000, (remaining / 1000) % 60);

        drawAtBottom("[" + bar + "] " + String.format("%.1f", percent) + "% ETA: " + eta);
    }

    private synchronized void drawAtBottom(String barText) {
        int height = getTerminalHeight();
        System.out.print("\033[s"); // save cursor
        System.out.print("\033[" + height + ";0H"); // move to bottom line
        System.out.print("\033[K"); // clear line
        System.out.print(barText); // draw bar
        System.out.print("\033[u"); // restore cursor
        System.out.flush();
    }

    private static int getTerminalHeight() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty size </dev/tty"});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            if (line != null) {
                String[] parts = line.split(" ");
                return Integer.parseInt(parts[0]);
            }
        } catch (Exception e) {
            // fallback
        }
        return 24;
    }
}
