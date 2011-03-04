package cx.ath.strider.iidx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ChartView extends View {
	private int maxValue;
	private int xOffset = 10;
	private int yOffset = 10;
	private ScoreDetail[] scores;
	private int chartColor;
	
	public ChartView(Context context) {
		super(context);
		chartColor = 0xFFFFFFFF;
	}

	@Override
	public void onDraw(Canvas canvas) {
		int h = getHeight(), w = getWidth();
		Paint p = new Paint();		
		
		p.setColor(0xFFFFFFFF);		
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(1);
		
		Rect grid = new Rect(xOffset, yOffset, w - (xOffset+1), h - (yOffset+1));
		
		Paint p2 = new Paint();
		p2.setColor(chartColor);
			
		if(scores == null || scores.length == 0)
			return;
		
		int adjust = (grid.width() / scores.length) / 2;
		
		for(int i=1; i<scores.length+1; i++) {
            int newX = (((grid.width() / scores.length) * i) - adjust) + xOffset + 1;
            float acc = ((float)scores[i - 1].EXScore / (float)maxValue);
            int newY = (grid.height() - ((int)(acc * grid.height())));
            
            Rect bar = new Rect(newX, newY, newX+adjust, newY + (int)((acc * grid.height()) + yOffset));
            canvas.drawRect(bar, p2);
		}
		
		canvas.drawRect(grid, p);
				
		String[] grades = new String[] { "F", "E", "D", "C", "B", "A", "A A", "A A A" };
		int[] gradeValues = getGradeValues();
		for(int i = 0; i<gradeValues.length; i++) {
			float acc = ((float)gradeValues[i] / (float)maxValue);
			int Y = (grid.height() - ((int)(acc * grid.height())));
			p.setAntiAlias(true);
			canvas.drawText(grades[i+1], xOffset + 5, Y-1, p);
			p.setAntiAlias(false);
			canvas.drawLine(xOffset, Y, grid.width() + xOffset, Y, p);
		}
	}
	
	private int[] getGradeValues() {
		int[] retVal = new int[7];
		for(int i=8; i>1; i--)
			retVal[i-2] = ((maxValue * i) / 9);
		return retVal;
	}
	
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		this.invalidate();
	}
	public int getMaxValue() {
		return this.maxValue;
	}
	public void setScores(ScoreDetail[] scores) {
		this.scores = scores;
		this.invalidate();
	}
	public ScoreDetail[] getScores(){
		return this.scores;
	}
	public void setChartColor(int color) {
		this.chartColor = color;
		this.invalidate();
	}
	public int getChartColor() {
		return this.chartColor;
	}
}
