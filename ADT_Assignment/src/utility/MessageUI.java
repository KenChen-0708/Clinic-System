package utility;

import java.util.Scanner;

public class MessageUI {

    public static void displayInvalidChoiceMessage() {
        System.out.println("\nInvalid choice");
    }

    public static void displayExitMessage() {
        System.out.println("\nExiting system");
    }

    public static void displayMainMenuMessage() {
        System.out.println("\nReturning to the main menu...");
    }

    public static void displayEmptyMessage() {
        System.out.println("=== No Data Available ===\n");
    }

    public static void clearConsole() {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    public static void pressEnterToContinue() {
        System.out.print("Press Enter to continue...");

        try {
            new Scanner(System.in).nextLine();
            clearConsole();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
