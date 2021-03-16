package com.example.sudokuchallenge.utils;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

//first, we need to create a full sudoku, and return
//then turn it into a partial sudoku and return
//if user clicks the submit button, then metch partial sudoku with full sudoku


public class SudokuMaker implements Serializable {
    //we can't directly put an object of SudokuMaker in firebase because of 2 reasons
    //1. it contains a lot of useless fields which will have no use, but will take up a lot of space in the database, like the stateArrayList
    //2. it uses arrays for various purposes, and arrays can't be made Serializable by firebase
    //That's why we needed to make the OpponentSudoku class

    public int secondsElapsed = 0;
    public int timeLimit = 0;

    public static final int EASY = 10;
    public static final int MEDIUM = 20;
    public static final int DIFFICULT = 50;
    public static final int TIME_ATTACK = 15;

    public static final int UNDO = 11;
    public static final int REDO = 12;
    public static final int RESET = 0;

    private int selectedRow;
    private int selectedColumn;

    private int[][] fullBoard = null;
    private int[][] partialBoard = null;
    private int[][] workingBoard;

    private ArrayList<int[][]> stateArrayList;
    private int stateIndex;
    public boolean isFinished = false;

    public SudokuMaker(){
    }

    public void setDifficulty(int difficulty){
        selectedRow = -1;
        selectedColumn = -1;

        fullBoard = new int[9][9];
        partialBoard = new int[9][9];
        workingBoard = new int[9][9];

        makeSudoku();
        createPartSudoku(difficulty);

        stateArrayList = new ArrayList<>();
        int[][] arr = new int[9][9];
        for(int i = 0; i<9; i++){
            for(int j = 0; j<9; j++){
                arr[i][j] = partialBoard[i][j];
            }
        }
        stateArrayList.add(arr);
        stateIndex = 0;


        for(int i = 0; i<9; i++){
            System.arraycopy(stateArrayList.get(stateIndex)[i], 0, workingBoard[i], 0, workingBoard[i].length);
        }
    }

    public SudokuMaker(int difficulty){
        selectedRow = -1;
        selectedColumn = -1;

        fullBoard = new int[9][9];
        partialBoard = new int[9][9];
        workingBoard = new int[9][9];

        makeSudoku();
        createPartSudoku(difficulty);

        stateArrayList = new ArrayList<>();
        int[][] arr = new int[9][9];
        for(int i = 0; i<9; i++){
            for(int j = 0; j<9; j++){
                arr[i][j] = partialBoard[i][j];
            }
        }
        stateArrayList.add(arr);
        stateIndex = 0;


        for(int i = 0; i<9; i++){
            System.arraycopy(stateArrayList.get(stateIndex)[i], 0, workingBoard[i], 0, workingBoard[i].length);
        }
    }

    public void setNumberPosition(int num){

        if(num == UNDO){
            if(!stateArrayList.isEmpty()) {
                if (stateIndex > 0) {
                    stateIndex--;
                    for(int i = 0; i<workingBoard.length; i++){
                        System.arraycopy(stateArrayList.get(stateIndex)[i], 0, workingBoard[i], 0, workingBoard[i].length);
                    }
                }
            }
            return;
        }

        if(num == REDO){

            if(!stateArrayList.isEmpty()) {
                if (stateIndex < stateArrayList.size() - 1) {
                    stateIndex++;
                    for(int i = 0; i<workingBoard.length; i++){
                        System.arraycopy(stateArrayList.get(stateIndex)[i], 0, workingBoard[i], 0, workingBoard[i].length);
                    }
                }
            }
            return;
        }


        for(int i = 0; i<partialBoard.length; i++){
            for(int j = 0; j<partialBoard[i].length; j++){
                if(selectedRow != -1 && selectedColumn != -1) {
                    if (partialBoard[selectedRow-1][selectedColumn-1] != 0)
                        return;
                }
            }
        }

        if(this.selectedColumn != -1 && this.selectedRow != -1){
            if(num != workingBoard[selectedRow-1][selectedColumn-1]) {
                stateIndex++;
                stateArrayList.subList(stateIndex, stateArrayList.size()).clear();
                workingBoard[selectedRow - 1][selectedColumn - 1] = num;
                int[][] arr = new int[9][9];
                for(int i = 0; i<9; i++){
                    for(int j = 0; j<9; j++){
                        arr[i][j] = workingBoard[i][j];
                    }
                }
                stateArrayList.add(arr);
            }
        }
    }

    public int[][] getWorkingBoard(){
        return this.workingBoard;
    }

    public int[][] getPartialBoard(){
        return partialBoard;
    }

    public int[][] getFullBoard(){
        return fullBoard;
    }



    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

//    public SudokuMaker(){} //for custom view class SudokuBoard

    private void makeSudoku() {
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        makeSudokuDriver(fullBoard, 0, 0, new Random(), numbers);
    }

    private void createPartSudoku(int attempts) {
        for(int i = 0; i<fullBoard.length; i++){
            for(int j = 0; j<fullBoard[i].length; j++){
                partialBoard[i][j] = fullBoard[i][j];
            }
        }
        createDriver(partialBoard, attempts, new Random());
    }

    private void solve() {
        solveDriver(partialBoard, 0, 0);
    }



    private void createDriver(int[][] board, int attempts, Random random) {
        while (attempts > 0) {
            int removeRow = random.nextInt(9);
            int removeCol = random.nextInt(9);

            int number = board[removeRow][removeCol];
            board[removeRow][removeCol] = 0;
            if (solutions(board) != 1) {
                board[removeRow][removeCol] = number;
                attempts--;
            }
        }
    }

    private void solveDriver(int[][] sudoku, int startRowIndex, int startColIndex) {

        if (startColIndex == sudoku[startRowIndex].length) {
            startColIndex = 0;
            startRowIndex++;
        }

        if (startRowIndex == sudoku.length) {
            return;
        }

        if (sudoku[startRowIndex][startColIndex] == 0) {
            for (int i = 1; i <= 9; i++) {
                if (!isInRow(sudoku, startRowIndex, i) && !isInCol(sudoku, startColIndex, i) && !isInFamily(sudoku, startRowIndex, startColIndex, i)) {
                    sudoku[startRowIndex][startColIndex] = i;
                    solveDriver(sudoku, startRowIndex, startColIndex + 1);
                }
            }
            sudoku[startRowIndex][startColIndex] = 0;
        } else {
            solveDriver(sudoku, startRowIndex, startColIndex + 1);
        }
    }

    private int solutions(int[][] partBoard) {
        return solutionsDriver(partBoard, 0, 0, 0);
    }

    private int solutionsDriver(int[][] sudoku, int startRowIndex, int startColIndex, int solutions) {

        if (startColIndex == sudoku[startRowIndex].length) {
            startColIndex = 0;
            startRowIndex++;
        }

        if (startRowIndex == sudoku.length) {
            solutions++;
            return solutions;

        }

        if (sudoku[startRowIndex][startColIndex] == 0) {
            int sol = 0;
            for (int i = 1; i <= 9; i++) {
                if (!isInRow(sudoku, startRowIndex, i) && !isInCol(sudoku, startColIndex, i) && !isInFamily(sudoku, startRowIndex, startColIndex, i)) {
                    sudoku[startRowIndex][startColIndex] = i;
                    sol = sol + solutionsDriver(sudoku, startRowIndex, startColIndex, solutions);
                }
            }
            sudoku[startRowIndex][startColIndex] = 0;
            return sol;
        } else {
            return solutionsDriver(sudoku, startRowIndex, startColIndex + 1, solutions);
        }
    }



    private boolean makeSudokuDriver(int[][] board, int startRowIndex, int startColIndex, Random random, int[] numbers) {

        if (startColIndex == board[startRowIndex].length) {
            startColIndex = 0;
            startRowIndex++;
        }

        if (startRowIndex == board.length) {
            return true;
        }


        ArrayList<Integer> validNumbers = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++) {
            int element = numbers[i];
            if (!isInRow(board, startRowIndex, element) && !isInCol(board, startColIndex, element) && !isInFamily(board, startRowIndex, startColIndex, element)) {
                validNumbers.add(numbers[i]);
            }
        }

        if (validNumbers.isEmpty()) {
            return false;
        }

        for (int i = 0; i < validNumbers.size(); i++) {
            int index = random.nextInt(validNumbers.size());
            board[startRowIndex][startColIndex] = validNumbers.get(index);
            validNumbers.remove(index);
            boolean made = makeSudokuDriver(board, startRowIndex, startColIndex + 1, random, numbers);
            if (made)
                return true;
            board[startRowIndex][startColIndex] = 0;
        }

        return false;
    }

    public boolean isInCol(int[][] arr, int columnIndex, int element) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i][columnIndex] == element)
                return true; // is there
        }
        return false; // is not there
    }

    public boolean isInRow(int[][] arr, int rowIndex, int element) {
        for (int i = 0; i < arr[rowIndex].length; i++) {
            if (arr[rowIndex][i] == element)
                return true; //is there
        }
        return false; // is not there
    }

    public boolean isInFamily(int[][] arr, int rowIndex, int colIndex, int element) {
        for (int i = (rowIndex / 3) * 3; i < ((rowIndex / 3) * 3) + 3; i++) {
            for (int j = (colIndex / 3) * 3; j < ((colIndex / 3) * 3) + 3; j++) {
                if (arr[i][j] == element)
                    return true; //is there
            }
        }
        return false; // is not there
    }

    public ArrayList<Pair<Integer, Integer>> getInCol(int[][] arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = 0; i<arr.length; i++){
            if(arr[i][colIndex] == element) {
                if (rowIndex == i)
                    continue;
                list.add(new Pair<>(i, colIndex));
                list.add(new Pair<>(rowIndex, colIndex));
            }
        }
        return list;
    }

    public ArrayList<Pair<Integer, Integer>> getInRow(int[][] arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = 0; i<arr[rowIndex].length; i++){
            if(arr[rowIndex][i] == element) {
                if(colIndex==i)
                    continue;
                list.add(new Pair<>(rowIndex, i));
                list.add(new Pair<>(rowIndex, colIndex));
            }
        }
        return list;
    }

    public ArrayList<Pair<Integer, Integer>> getInFamily(int[][] arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = (rowIndex/3)*3; i<((rowIndex/3)*3)+3; i++){
            for(int j = (colIndex/3)*3; j<((colIndex/3)*3)+3; j++){
                if(arr[i][j] == element) {
                    if(rowIndex == i && colIndex == j)
                        continue;
                    list.add(new Pair<>(i, j));
                    list.add(new Pair<>(rowIndex, colIndex));
                }
            }
        }
        return list;
    }

    public OpponentSudoku getOpponentSudoku(){
        ArrayList<ArrayList<Integer>> opPartialBoard = new ArrayList<>();
        ArrayList<ArrayList<Integer>> opWorkingBoard = new ArrayList<>();
        for(int i = 0; i<9; i++){
            ArrayList<Integer> arrayList = new ArrayList<>();
            for(int j = 0; j<9; j++){
                arrayList.add(partialBoard[i][j]);
            }
            opPartialBoard.add(arrayList);
            arrayList = new ArrayList<>();
            for(int j = 0; j<9; j++){
                arrayList.add(workingBoard[i][j]);
            }
            opWorkingBoard.add(arrayList);
        }
        OpponentSudoku opponentSudoku = new OpponentSudoku(opPartialBoard, opWorkingBoard);
        opponentSudoku.setSelectedRow(selectedRow);
        opponentSudoku.setSelectedColumn(selectedColumn);
        opponentSudoku.setFinished(isFinished);
        return opponentSudoku;
    }

    public void finished(boolean isFinished){
        this.isFinished = isFinished;
        selectedRow = -1;
        selectedColumn = -1;
    }
}
