package com.strangelet.sudokuchallenge.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.utils.OpponentSudoku;

import java.util.ArrayList;

public class OpponentSudokuBoard extends View {

    private OpponentSudoku opSudoku;

    private final int gridColor;
    private final int mainCellColor;
    private final int secondaryCellColor;
    private final int initialDigitColor;
    private final int digitColor;
    private final int errorDigitCellColor;

    private final Paint gridPaint = new Paint();
    private final Paint mainCellPaint = new Paint();
    private final Paint secondaryCellPaint = new Paint();
    private final Paint digitPaint = new Paint();
    private final Paint errorDigitCellPaint = new Paint();

    private final Rect digitBounds = new Rect();//to find the width and height bounds of a digit

    private ArrayList<Pair<Integer, Integer>> errorList = new ArrayList<>();

    private int cellDimension;

    public OpponentSudokuBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray boardColorArray = context.obtainStyledAttributes(attrs, R.styleable.OpponentSudokuBoard);

        try{
            //extract the individual attributes and set them in variables here
            gridColor = boardColorArray.getInteger(R.styleable.OpponentSudokuBoard_opponentGridColor, 0);
            gridPaint.setColor(gridColor);

            mainCellColor = boardColorArray.getColor(R.styleable.OpponentSudokuBoard_opponentMainCellColor, 0);
            secondaryCellColor = boardColorArray.getColor(R.styleable.OpponentSudokuBoard_opponentSecondaryCellColor, 0);

            initialDigitColor = boardColorArray.getColor(R.styleable.OpponentSudokuBoard_opponentInitialDigitColor, 0);
            digitColor = boardColorArray.getColor(R.styleable.OpponentSudokuBoard_opponentDigitColor, 0);
            errorDigitCellColor = boardColorArray.getColor(R.styleable.OpponentSudokuBoard_opponentErrorDigitCellColor, 0);



        }finally {
            boardColorArray.recycle();
        }

        ArrayList<ArrayList<Integer>> initialOpponentSudokuList = new ArrayList<>();
        initialOpponentSudokuList = new ArrayList<>();
        for(int i = 0; i<9; i++){
            ArrayList<Integer> arrayList = new ArrayList<>();
            for(int j = 0; j<9; j++){
                arrayList.add(0);
            }
            initialOpponentSudokuList.add(arrayList);
        }

        opSudoku = new OpponentSudoku(initialOpponentSudokuList, initialOpponentSudokuList);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {    //override this method to measure the size of the screen based on the user's phone
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int dimension = Math.min(width, height);

        cellDimension = dimension/9;
        setMeasuredDimension(dimension, dimension);//defines the width and height of the view
    }

    @Override
    protected void onDraw(Canvas canvas) { //we need to override this method to draw on the canvas(An area of the screen whose measurements you defined in the onMeasuredDimension method)
        super.onDraw(canvas);

        gridPaint.setStyle(Paint.Style.STROKE);
        //pass here STROKE to draw only the outlines, FILL to draw filled object, and FILL_AND_STROKE to fill color and draw the outlines too
        gridPaint.setStrokeWidth(5);
        gridPaint.setColor(gridColor);
        gridPaint.setAntiAlias(true); //to make lines sharp and avoid bleeding

        mainCellPaint.setStyle(Paint.Style.FILL);
        mainCellPaint.setColor(mainCellColor);
        mainCellPaint.setAntiAlias(true);

        secondaryCellPaint.setStyle(Paint.Style.FILL);
        secondaryCellPaint.setColor(secondaryCellColor);
        secondaryCellPaint.setAntiAlias(true);

        errorDigitCellPaint.setStyle(Paint.Style.FILL);
        errorDigitCellPaint.setColor(errorDigitCellColor);
        secondaryCellPaint.setAntiAlias(true);

        digitPaint.setStyle(Paint.Style.FILL);
        digitPaint.setAntiAlias(true);

        colorCell(canvas, opSudoku.getSelectedRow(), opSudoku.getSelectedColumn());
        for(int i = 0; i<opSudoku.getWorkingBoard().size(); i++){
            for(int j = 0; j<opSudoku.getWorkingBoard().get(i).size(); j++){
                errorList.addAll(opSudoku.getInCol(opSudoku.getWorkingBoard(), i, j, opSudoku.getWorkingBoard().get(i).get(j)));
                errorList.addAll(opSudoku.getInRow(opSudoku.getWorkingBoard(), i, j, opSudoku.getWorkingBoard().get(i).get(j)));
                errorList.addAll(opSudoku.getInFamily(opSudoku.getWorkingBoard(), i, j, opSudoku.getWorkingBoard().get(i).get(j)));
            }
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), gridPaint);
        for(int i = 0; i<errorList.size(); i++){
            Pair<Integer, Integer> pair = errorList.remove(errorList.size()-1-i);
            canvas.drawRect(pair.second*cellDimension, pair.first*cellDimension, (pair.second + 1)*cellDimension, (pair.first + 1)*cellDimension, errorDigitCellPaint);
        }
        drawBoard(canvas);
        drawNumbers(canvas);
    }

    private void drawBoard(Canvas canvas){

        for(int colLine = 0; colLine<9; colLine++){ //to draw 8 col lines, makes 9 cols
            if(colLine%3 == 0){
                setThickLinePaint();
            }else{
                setThinLinePaint();
            }
            canvas.drawLine(cellDimension*colLine, 0, cellDimension*colLine, getWidth(), gridPaint);

        }

        for(int rowLine = 0; rowLine<9; rowLine++){
            if(rowLine%3 == 0){
                setThickLinePaint();
            }else{
                setThinLinePaint();
            }
            canvas.drawLine(0, cellDimension*rowLine, getWidth(), cellDimension*rowLine, gridPaint);

        }

    }

    private void setThickLinePaint(){
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(5);
        gridPaint.setColor(gridColor);
        gridPaint.setAntiAlias(true);
    }

    private void setThinLinePaint(){
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(3);
        gridPaint.setColor(gridColor);
        gridPaint.setAntiAlias(true);
    }

    private void colorCell(Canvas canvas, int row, int col){
        int familyRowStart = ((row-1)/3)*3;
        int familyColStart = ((col-1)/3)*3;

        if(opSudoku.getSelectedRow() != -1 && opSudoku.getSelectedColumn() != -1){
            canvas.drawRect((col-1)*cellDimension, 0, col*cellDimension, familyRowStart*cellDimension, secondaryCellPaint);
            canvas.drawRect((col-1)*cellDimension, familyRowStart*cellDimension + 3*cellDimension, col*cellDimension, 9*cellDimension, secondaryCellPaint);
            //to highlight a column

            canvas.drawRect(0, (row-1)*cellDimension, familyColStart*cellDimension, row*cellDimension, secondaryCellPaint);
            canvas.drawRect(familyColStart*cellDimension + 3*cellDimension, (row-1)*cellDimension, 9*cellDimension, row*cellDimension, secondaryCellPaint);
            //to highlight a row

            canvas.drawRect((col-1)*cellDimension, (row-1)*cellDimension, col*cellDimension, row*cellDimension, mainCellPaint);
            //to highlight the mainCell

            canvas.drawRect(familyColStart*cellDimension, familyRowStart*cellDimension, familyColStart*cellDimension + (3*cellDimension), familyRowStart*cellDimension + (3*cellDimension), secondaryCellPaint);
            //to highlight a family of cells
        }
        invalidate(); //just to refresh our sudoku board
    }

    private void drawNumbers(Canvas canvas){
        digitPaint.setTextSize((int) (cellDimension-cellDimension/5));
        digitPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        for(int r = 0; r<9; r++){
            for(int c = 0; c<9; c++){
                if(opSudoku.getPartialBoard().get(r).get(c)==0){
                    digitPaint.setColor(digitColor);
                } else{
                    digitPaint.setColor(initialDigitColor);
                }
                if(opSudoku.getWorkingBoard().get(r).get(c) != 0){
                    String stringDigit = Integer.toString(opSudoku.getWorkingBoard().get(r).get(c));
                    float digitWidth, digitHeight;

                    digitPaint.getTextBounds(stringDigit, 0, stringDigit.length(), digitBounds);
                    //we can not use digitPaint.measure(stringDigit) here also because it will only return us the width and not the height of the digit

                    digitWidth = digitPaint.measureText(stringDigit);
                    digitHeight = digitBounds.height();

                    canvas.drawText(stringDigit, c*cellDimension + (cellDimension-digitWidth)/2, r*cellDimension + digitHeight + (cellDimension-digitHeight)/2, digitPaint);
                }                                                       //to center out digit
            }
        }
    }

    public OpponentSudoku getOpponentSudoku() {
        return this.opSudoku;
    }

    public void setOpponentSudoku(OpponentSudoku opSudoku){
        this.opSudoku = opSudoku;
    }

}
