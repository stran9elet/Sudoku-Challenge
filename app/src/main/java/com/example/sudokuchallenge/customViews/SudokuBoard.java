package com.example.sudokuchallenge.customViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.utils.SudokuMaker;

import java.io.Serializable;
import java.util.ArrayList;

//the class name must be same as the attribute name you defined in the attrs file
public class SudokuBoard extends View implements Serializable {
    //ToDo: why is this serializable?

    private int difficulty = SudokuMaker.EASY;

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

    private SudokuMaker sudokuMaker;

    private ArrayList<Pair<Integer, Integer>> errorList = new ArrayList<>();

    private int cellDimension;

    public SudokuBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray boardColorArray = context.obtainStyledAttributes(attrs, R.styleable.SudokuBoard);

        try{
            //extract the individual attributes and set them in variables here
            gridColor = boardColorArray.getInteger(R.styleable.SudokuBoard_gridColor, 0);
            gridPaint.setColor(gridColor);

            mainCellColor = boardColorArray.getColor(R.styleable.SudokuBoard_mainCellColor, 0);
            secondaryCellColor = boardColorArray.getColor(R.styleable.SudokuBoard_secondaryCellColor, 0);

            initialDigitColor = boardColorArray.getColor(R.styleable.SudokuBoard_initialDigitColor, 0);
            digitColor = boardColorArray.getColor(R.styleable.SudokuBoard_digitColor, 0);
            errorDigitCellColor = boardColorArray.getColor(R.styleable.SudokuBoard_errorDigitCellColor, 0);



        }finally {
            boardColorArray.recycle();
        }
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
        gridPaint.setStrokeWidth(20);
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

        colorCell(canvas, sudokuMaker.getSelectedRow(), sudokuMaker.getSelectedColumn());
        for(int i = 0; i<sudokuMaker.getWorkingBoard().length; i++){
            for(int j = 0; j<sudokuMaker.getWorkingBoard()[i].length; j++){
                errorList.addAll(sudokuMaker.getInCol(sudokuMaker.getWorkingBoard(), i, j, sudokuMaker.getWorkingBoard()[i][j]));
                errorList.addAll(sudokuMaker.getInRow(sudokuMaker.getWorkingBoard(), i, j, sudokuMaker.getWorkingBoard()[i][j]));
                errorList.addAll(sudokuMaker.getInFamily(sudokuMaker.getWorkingBoard(), i, j, sudokuMaker.getWorkingBoard()[i][j]));
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

    //This method will draw lines on the board, both thick ones and the thin ones
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
        gridPaint.setStrokeWidth(10);
        gridPaint.setColor(gridColor);
        gridPaint.setAntiAlias(true);
    }

    private void setThinLinePaint(){
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(5);
        gridPaint.setColor(gridColor);
        gridPaint.setAntiAlias(true);
    }


//this method gives us data about touch events that happen on the user's screen.
//if the user swipes the screen, we can get data about that, or if the user taps the screen, we can also get data about that.
//we can also get the x and y coordinates of a user's tap
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //        return super.onTouchEvent(event);
        if(!sudokuMaker.isFinished) {

            float x = event.getX();
            float y = event.getY();
            //to get the x and y coordinates of the event that occurred in the user's screen

            int action = event.getAction();//variable to store the event that occured
            if (action == MotionEvent.ACTION_DOWN) { //if it was a click event
                int selectedRow = (int) Math.ceil(y / cellDimension);
                int selectedCol = (int) Math.ceil(x / cellDimension);
                if(selectedRow>9)
                    selectedRow = 9;
                if(selectedCol>9)
                    selectedCol = 9;
                if(selectedRow<1)
                    selectedRow = 1;
                if(selectedCol<1)
                    selectedCol = 1;
                sudokuMaker.setSelectedRow(selectedRow);
                sudokuMaker.setSelectedColumn(selectedCol);
                return true;
            }
        }
        return false;
    }

    //method to color the cell that user clicks
    private void colorCell(Canvas canvas, int row, int col){
        int familyRowStart = ((row-1)/3)*3;
        int familyColStart = ((col-1)/3)*3;

        if(sudokuMaker.getSelectedRow() != -1 && sudokuMaker.getSelectedColumn() != -1){
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
                if(sudokuMaker.getPartialBoard()[r][c]==0){
                    digitPaint.setColor(digitColor);
                } else{
                    digitPaint.setColor(initialDigitColor);
                }
                if(sudokuMaker.getWorkingBoard()[r][c] != 0){
                    String stringDigit = Integer.toString(sudokuMaker.getWorkingBoard()[r][c]);
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

    public SudokuMaker getSudokuMaker() {
        return this.sudokuMaker;
    }

    public void isResumedActivity(boolean isResumed, SudokuMaker sudokuMaker){
        if(isResumed){
            this.sudokuMaker = sudokuMaker;
        }
    }

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
        sudokuMaker = new SudokuMaker(this.difficulty);
    }

}
