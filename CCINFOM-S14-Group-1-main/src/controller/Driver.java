package src.controller;

import java.util.Scanner;
import src.model.AnimeSystem;
import src.view.gui.TopView;

/**
 * The Driver is the entrypoint of the program.
 */
public class Driver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        scanner.close();

        AnimeSystem animeSystem = new AnimeSystem(username, password);
        TopView topView = new TopView();
        new Controller(animeSystem, topView);
    }
}
