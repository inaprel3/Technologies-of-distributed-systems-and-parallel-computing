package com.company;

import java.util.Scanner;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.System;
import java.lang.String;

public class MatrixMultiplication {
    private static final int THREAD_COUNT = 4; // Кількість потоків
    private static final int MATRIX_SIZE = 1000; // Розмір матриць
    private static final ReentrantLock lock = new ReentrantLock(); // Монітор
    private static final int[][] MA = new int[MATRIX_SIZE][MATRIX_SIZE]; // Матриця MA
    private static final int[][] MX = new int[MATRIX_SIZE][MATRIX_SIZE]; // Матриця MX
    private static final int[][] MZ = new int[MATRIX_SIZE][MATRIX_SIZE]; // Матриця MZ
    private static final int[][] MO = new int[MATRIX_SIZE][MATRIX_SIZE]; // Матриця MO
    private static final int[] a = new int[1]; // Масив для зберігання константи a

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); //Час початку роботи програми

        Thread[] threads = new Thread[THREAD_COUNT]; // Масив потоків

        // Зчитування даних з пристроїв вводу/виводу
        readMatrix("MA", MA);
        readMatrix("MX", MX);
        readMatrix("MZ", MZ);
        readMatrix("MO", MO);
        readConstant();

        // Розподіл обчислень між потоками
        int rowsPerThread = MATRIX_SIZE / THREAD_COUNT;
        for (int i = 0; i < THREAD_COUNT; i++) {
            int startRow = i * rowsPerThread;
            int endRow = startRow + rowsPerThread;
            threads[i] = new Thread(new MatrixMultiplier(startRow, endRow));
            threads[i].start();
        }

        // Очікування завершення всіх потоків
        for (int i = 0; i < THREAD_COUNT; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Виведення результатів
        writeMatrix("MA", MA);
        writeMatrix("MX", MX);
        writeMatrix("MZ", MZ);
        writeMatrix("MO", MO);

        long endTime = System.currentTimeMillis(); //Час закінчення роботи програми
        long totalTime = endTime - startTime; //Час роботи програми
        System.out.println("Program running time: " + totalTime + " milliseconds");
    }

    // Код для зчитування матриці з пристрою вводу
    private static void readMatrix(String name, int[][] matrix) {
        System.out.println("Enter matrix " + name + ":");
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < MATRIX_SIZE; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                matrix[i][j] = scanner.nextInt(); /* код для зчитування елементу матриці з пристрою вводу */;
            }
        }
        scanner.close();
    }

    // Код для зчитування константи з пристрою вводу
    private static void readConstant() {
        System.out.println("Enter constant " + "a" + ":");
        Scanner scanner = new Scanner(System.in);
        MatrixMultiplication.a[0] = scanner.nextInt(); /* код для зчитування константи з пристрою вводу */;
        scanner.close();
    }

    private static void writeMatrix(String name, int[][] matrix) {
        // Код для виведення матриці на пристрій виводу
        System.out.println("Matrix " + name + ":");
        for (int i = 0; i < MATRIX_SIZE; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static class MatrixMultiplier implements Runnable {
        private final int startRow;
        private final int endRow;

        public MatrixMultiplier(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run() {
            // Обчислення частини матриці MA
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < MATRIX_SIZE; j++) {
                    lock.lock(); // Захоплення монітора
                    try {
                        MA[i][j] = a[0] * MX[i][j] + multiplyMZMO(i, j); // Обчислення елементу матриці MA
                    } finally {
                        lock.unlock(); // Звільнення монітора
                    }
                }
            }
        }

        private int multiplyMZMO(int row, int col) {
            int result = 0;
            for (int i = 0; i < MATRIX_SIZE; i++) {
                result += MZ[row][i] * MO[i][col];
            }
            return result;
        }
    }

    public static class Worker implements Runnable {
        private final int[][] MX;
        private final int[][] MZ;
        private final int[][] MO;
        private final int[][] MA;
        private final int[] a;
        private final int startRow;
        private final int endRow;
        private final Lock lock;

        public Worker(int[][] MX, int[][] MZ, int[][] MO, int[][] MA, int[] a, int startRow, int endRow, Lock lock) {
            this.MX = MX;
            this.MZ = MZ;
            this.MO = MO;
            this.MA = MA;
            this.a = a;
            this.startRow = startRow;
            this.endRow = endRow;
            this.lock = lock;
        }

        public void run() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < MATRIX_SIZE; j++) {
                    int sum = 0;
                    for (int k = 0; k < MATRIX_SIZE; k++) {
                        sum += a[0] * MX[i][k] + MZ[i][k] * MO[k][j];
                    }
                    lock.lock(); // блокуємо доступ до загальної пам'яті
                    MA[i][j] += sum; lock.unlock(); // розблокуємо доступ до загальної пам'яті
                }
            }
        }
    }
}
