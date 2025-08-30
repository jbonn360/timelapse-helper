package com.timelapse;

import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
//        args = new String[1];
//        args[0] = "../test/";

        if (args.length != 1) {
            System.out.println("Usage: java -jar timelapse-helper.jar <directory>");
            return;
        }

        final var dir = new File(args[0]);
        final var analyser = new ImageAnalyser();
        final var cleaner = new ImageCleaner(analyser);

        cleaner.cleanDirectory(dir);
    }
}