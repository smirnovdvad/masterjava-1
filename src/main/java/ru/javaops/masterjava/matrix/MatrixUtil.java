package ru.javaops.masterjava.matrix;

import java.util.*;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        final int aColumns = matrixA[0].length;
        final int aRows = matrixA.length;
        final int bColumns = matrixB[0].length;
        final int bRows = matrixB.length;
        final int[][]  matrixC = new int[aRows][aRows];

        class subMatrixC{
           final int j;
           final int[] subMatrix;

            subMatrixC(int j, int[] subMatrix) {
                this.j = j;
                this.subMatrix = subMatrix;
            }
        }
        final CompletionService<subMatrixC> completionService = new ExecutorCompletionService<>(executor);

        class mThread implements Callable<subMatrixC> {

            final int j;
            final double[] thatColumn;

            mThread(int j, double[] thatColumn) {
                this.j = j;
                this.thatColumn = thatColumn;
            }

            @Override
            public subMatrixC call() throws Exception {
                int[] matr = new int[aRows];
                for (int i = 0; i < aRows; i++) {
                    int thisRow[] = matrixA[i];
                    int summand = 0;
                    for (int k = 0; k < aColumns; k++) {
                        summand += thisRow[k] * this.thatColumn[k];
                    }
                    matr[i] = summand;
                }
               return new subMatrixC(j,matr);
            }

        }

        List<Future<subMatrixC>> futures = new ArrayList<>();



        for (int j = 0; j < bColumns; j++) {
            double thatColumn[] = new double[bRows];
            for (int k = 0; k < aColumns; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            futures.add(completionService.submit(new mThread(j,thatColumn)));

        }


        while (!futures.isEmpty()){
            Future<subMatrixC> ready = completionService.poll();
            if (ready!=null && ready.isDone())
            {
                futures.remove(ready);
                subMatrixC matr = ready.get();
                for (int i=0; i<aRows;i++)
                    matrixC[i][matr.j] = matr.subMatrix[i];
            }

        }


        return matrixC;
    }

    private static int[] multiplyWithColumn(int j, int aRows, int aColumns, double[] thatColumn, int[][] matrixA, int[][] matrixC){
            for (int i = 0; i < aRows; i++) {
                int thisRow[] = matrixA[i];
                int summand = 0;
                for (int k = 0; k < aColumns; k++) {
                    summand += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = summand;
            }
            return matrixC[j];
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
//        final int matrixSize = matrixA.length;
//        final int[][] matrixC = new int[matrixSize][matrixSize];
//
//        for (int i = 0; i < matrixSize; i++) {
//            for (int j = 0; j < matrixSize; j++) {
//                int sum = 0;
//                for (int k = 0; k < matrixSize; k++) {
//                    sum += matrixA[i][k] * matrixB[k][j];
//                }
//                matrixC[i][j] = sum;
//            }
//        }
//        return matrixC;
        final int aColumns = matrixA[0].length;
        final int aRows = matrixA.length;
        final int bColumns = matrixB[0].length;
        final int bRows = matrixB.length;
        final int[][] matrixC = new int[aRows][aRows];

        double thatColumn[] = new double[bRows];

        for (int j = 0; j < bColumns; j++) {
            for (int k = 0; k < aColumns; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < aRows; i++) {
                int thisRow[] = matrixA[i];
                int summand = 0;
                for (int k = 0; k < aColumns; k++) {
                    summand += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = summand;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
