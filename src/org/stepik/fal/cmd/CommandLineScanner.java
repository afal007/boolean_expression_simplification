package org.stepik.fal.cmd;

import org.stepik.fal.compiler.Optimizer;

import java.util.Scanner;

/**
 * author: Alexander Fal (falalexandr007@gmail.com)
 */
public class CommandLineScanner {
    public static void start() {
        System.out.println("This is syntax analyzer for boolean expressions.");
        Scanner sc = new Scanner(System.in);

        while(true) {
            System.out.println("\nType the expression to simplify. Type quit to quit.");
            String input = sc.nextLine();

            if(input.equals("quit"))
                return;
            else {
                try {
                    Optimizer.simplify(input);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println();
                }
            }
        }
    }
}
