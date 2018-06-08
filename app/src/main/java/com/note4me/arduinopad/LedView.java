package com.note4me.arduinopad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class LedView extends android.support.v7.widget.AppCompatImageView
{
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int brightness = 255;
	
	public LedView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);
	}
	
	public void setBrightness(int b)
	{
		brightness = b;
		invalidate();
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		super.draw(canvas);

		mPaint.setARGB(brightness, 255, 0, 0);
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mPaint);
	}
}
