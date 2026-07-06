package com.rev.util;

import java.util.Scanner;

public class ConsoleUtil {

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" " + title.toUpperCase());
        System.out.println("========================================");
        System.out.println();
    }

    public static void printSuccess(String message) {
        System.out.println();
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println();
        System.out.println("[ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println();
        System.out.println("[INFO] " + message);
    }

    public static void pause(Scanner scanner) {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
        clearScreen();

    }

    public static void clearScreen() {
        System.out.println("\n".repeat(40));
    }
}