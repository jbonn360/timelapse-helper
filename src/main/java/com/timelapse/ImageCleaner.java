package com.timelapse;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageCleaner {
    private final ImageAnalyser analyser;

    public ImageCleaner(ImageAnalyser analyser) {
        this.analyser = analyser;
    }

    /**
     * Processes all images in the given directory and moves
     * the ones that are completely black into a "blacks" subdirectory.
     *
     * @param directory The directory containing image files
     */
    public void cleanDirectory(final File directory) {
        if (!directory.isDirectory()) {
            System.out.println("Provided path is not a directory.");
            return;
        }

        final var files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the directory.");
            return;
        }

        // Ensure "blacks" subdirectory exists
        final var blacksDir = new File(directory, "blacks");
        if (!blacksDir.exists() && !blacksDir.mkdir()) {
            System.out.println("Failed to create blacks directory.");
            return;
        }

        doClean(files, blacksDir, initProgressBar(files));
    }

    private void doClean(final File[] files, final File blacksDir, final ProgressBar progressBar){
        // Thread pool with as many threads as available cores
        final var threads = Runtime.getRuntime().availableProcessors();
        try (var executor = Executors.newFixedThreadPool(threads)) {

            for (final var file : files) {
                if (!file.isFile() || !isImageFile(file)){
                    continue;
                }
                executor.submit(() -> {
                    processFile(file, blacksDir);
                    progressBar.increment();
                });
            }

            // Shut down and wait for all tasks to finish
            executor.shutdown();
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                System.err.println("Some tasks did not finish!");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.out.println("Image processing interrupted.");
        }
    }

    private ProgressBar initProgressBar(final File[] files){
        final int totalFiles = (int) Arrays.stream(files).filter(f -> f.isFile() && isImageFile(f)).count();
        final var progressBar = new ProgressBar(totalFiles, 50);

        progressBar.start();

        return progressBar;
    }

    private void processFile(final File file, final File blacksDir){
        try {
            final var img = ImageIO.read(file);
            if (img == null) return; // not an image

            if (analyser.isVirtuallyBlack(img)) {
                final var dest = new File(blacksDir, file.getName());
                Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved: " + file.getName() + " → " + dest.getAbsolutePath());
            }
        } catch (final IOException e) {
            System.out.println("Could not process file: " + file.getName());
        }
    }

    /**
     * Checks if the file has a common image extension.
     */
    private boolean isImageFile(final File file) {
        final var name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".bmp") ||
                name.endsWith(".gif") || name.endsWith(".tiff") ||
                name.endsWith(".webp");
    }
}
