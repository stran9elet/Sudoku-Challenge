package com.strangelet.sudokuchallenge.utils;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class OpponentSudoku implements Serializable {
    /**
     * getSelectedRow()
     * getSelectedColumn()
     * setSelectedRow()
     * setSelectedColumn()
     *
     * getInCol()
     * getInRow()
     * getInFamily()
     *
     * getWorkingBoard()
     * getPartialBoard()
     */

    private int selectedRow;
    private int selectedColumn;

    private ArrayList<ArrayList<Integer>> workingBoard;
    private ArrayList<ArrayList<Integer>> partialBoard;

    private boolean isFinished = false;

    public OpponentSudoku(){

    }

    public OpponentSudoku(ArrayList<ArrayList<Integer>> partialBoard, ArrayList<ArrayList<Integer>> workingBoard){
        selectedRow = -1;
        selectedColumn = -1;

        this.workingBoard = workingBoard;
        this.partialBoard = partialBoard;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    public ArrayList<ArrayList<Integer>> getWorkingBoard() {
        return workingBoard;
    }

    public ArrayList<ArrayList<Integer>> getPartialBoard() {
        return partialBoard;
    }

    public ArrayList<Pair<Integer, Integer>> getInRow(ArrayList<ArrayList<Integer>> arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = 0; i<arr.get(rowIndex).size(); i++){
            if(arr.get(rowIndex).get(i) == element) {
                if(colIndex==i)
                    continue;
                list.add(new Pair<>(rowIndex, i));
                list.add(new Pair<>(rowIndex, colIndex));
            }
        }
        return list;
    }

    public ArrayList<Pair<Integer, Integer>> getInCol(ArrayList<ArrayList<Integer>> arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = 0; i<arr.size(); i++){
            if(arr.get(i).get(colIndex) == element) {
                if (rowIndex == i)
                    continue;
                list.add(new Pair<>(i, colIndex));
                list.add(new Pair<>(rowIndex, colIndex));
            }
        }
        return list;
    }

    public ArrayList<Pair<Integer, Integer>> getInFamily(ArrayList<ArrayList<Integer>> arr, int rowIndex, int colIndex, int element){
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        if(element==0)
            return list;
        for(int i = (rowIndex/3)*3; i<((rowIndex/3)*3)+3; i++){
            for(int j = (colIndex/3)*3; j<((colIndex/3)*3)+3; j++){
                if(arr.get(i).get(j) == element) {
                    if(rowIndex == i && colIndex == j)
                        continue;
                    list.add(new Pair<>(i, j));
                    list.add(new Pair<>(rowIndex, colIndex));
                }
            }
        }
        return list;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
