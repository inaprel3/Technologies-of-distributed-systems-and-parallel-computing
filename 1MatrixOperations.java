package com.company;

import java.util.Scanner;
import java.lang.System;
import java.lang.String;

public class MatrixOperations {
    static int[][] MA, MX, MZ, MO;
    static int a;
    static int rows, columns;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); //Час початку роботи програми

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number of rows:");
        rows = scanner.nextInt();
        System.out.println("Enter the number of columns:");
        columns = scanner.nextInt();
        MA = new int[rows][columns];
        MX = new int[rows][columns];
        MZ = new int[rows][columns];
        MO = new int[rows][columns];
        System.out.println("Enter values for MA:");
        readMatrix(MA, scanner);
        System.out.println("Enter a:");
        a = scanner.nextInt();
        System.out.println("Enter values for MX:");
        readMatrix(MX, scanner);
        System.out.println("Enter values for MZ:");
        readMatrix(MZ, scanner);
        System.out.println("Enter values for MO:");
        readMatrix(MO, scanner);
        int[][] result = calculate();
        System.out.println("Result:");
        printMatrix(result);

        long endTime = System.currentTimeMillis(); //Час закінчення роботи програми
        long totalTime = endTime - startTime; //Час роботи програми
        System.out.println("Program running time: " + totalTime + " milliseconds");
    }

    static int[][] calculate() {
        final int[][] result = new int[rows][columns];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (MatrixOperations.class) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < columns; j++) {
                            result[i][j] += a * MX[i][j];
                        }
                    }
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (MatrixOperations.class) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < columns; j++) {
                            result[i][j] += MZ[i][j] * MO[i][j];
                        }
                    }
                }
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void readMatrix(int[][] matrix, Scanner scanner) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = scanner.nextInt();
            }
        }
    }

    static void printMatrix(int[][] matrix) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
