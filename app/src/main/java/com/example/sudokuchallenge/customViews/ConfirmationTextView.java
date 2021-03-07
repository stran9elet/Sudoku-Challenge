package com.example.sudokuchallenge.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sudokuchallenge.R;

public class ConfirmationTextView extends androidx.appcompat.widget.AppCompatTextView {

    private final int innerTextColor;
    private final int outlineColor;

    private final Paint textPaint = new Paint();


    public ConfirmationTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationTextView);

        try{
            innerTextColor = typedArray.getColor(R.styleable.ConfirmationTextView_innerTextColor, 0);
            outlineColor = typedArray.getColor(R.styleable.ConfirmationTextView_outlineColor, 0);
        }finally {
            typedArray.recycle();
        }
    }

    private void setPaintToOutline(){
        Paint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(18);
        super.setTextColor(outlineColor);
    }

    private void setPaintToRegular() {
        Paint paint = getPaint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        super.setTextColor(innerTextColor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        setPaintToOutline();
        super.onDraw(canvas);
        setPaintToRegular();
        super.onDraw(canvas);
    }
}
