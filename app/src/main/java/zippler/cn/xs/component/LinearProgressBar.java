package zippler.cn.xs.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import java.util.Random;

import zippler.cn.xs.R;

/**
 * Created by Zipple on 2018/5/10.
 * to display the music progress
 */
public class LinearProgressBar extends ProgressBar {

    private static final String TAG = "ProgressView";
    private int paddingLT = 3;
    private int paddingRT = 3;
    private Paint paint;
    private Rect mbound;
    private int textWidth;
    private int textHeight;
    private int mRealWidth;
    private int heightMeasureSpe;
    private Paint rightPaint;
    private Paint textPaint;

    public LinearProgressBar(Context context) {
        this(context, null);
    }

    public LinearProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.MyView);
        int progressColor = a.getColor(R.styleable.MyView_color, 0Xff0000ff);
        int progressColor_r = a
                .getColor(R.styleable.MyView_color_r, 0Xff0000ff);

        a.recycle();
        int bar = getProgress();
        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setDither(true);
        textPaint.setColor(0xff00ff00);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightPaint.setColor(progressColor_r);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(progressColor);
        mbound = new Rect();
        measureText(bar + "");

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int w = 0;
        int h = 0;
        switch (modeWidth) {
            case MeasureSpec.EXACTLY:
                w = width;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                w = getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
                break;

        }
        w = modeWidth == MeasureSpec.AT_MOST ? Math.min(w, width) : w;
        switch (modeHeight) {
            case MeasureSpec.EXACTLY:
                h = height;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                h = (int) ((-paint.ascent() + paint.descent()) + getPaddingTop() + getPaddingBottom());
                break;
        }
        h = modeHeight == MeasureSpec.AT_MOST ? Math.min(h, height) : h;
        setMeasuredDimension(w, h);

    }

    /**
     * Measurement of progress font width
     * @param text the bar
     */
    private void measureText(String text) {
        textPaint.getTextBounds(text, 0, text.length(), mbound);
        textWidth = (int) textPaint.measureText(text);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        textHeight = (int) (fm.descent + fm.ascent);

    }

    private int mRight;
    private int mLeft;
    private int mRealText;
    private int mLineHeight;
    private int mTextHeight;

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 判断是否到达右末端
        boolean isFinishRight = false;
        canvas.save();
        // 获取进度
        int progress = getProgress();
        // 0~1 速率 *1.0f先 不然会都等于0
        float rate = progress * 1.0f / getMax();
        // 不断累加
        float start = (mRealWidth - textWidth - paddingLT * 2) * rate;
        String text = progress + "%";
        measureText(text);
        // 右边长度变化
        mRight = (int) (start + textWidth + paddingLT * 2 + getPaddingLeft());
        // 左边长度变化
        mLeft = (int) (start + getPaddingLeft());
        // text 加上两个gap的实际宽度
        mRealText = textWidth + paddingLT * 2;
        // 线的实际高度
//        mLineHeight = -textHeight / 2 + getPaddingTop();
        mLineHeight = -textHeight / 2;
        // 字体的高度
//        mTextHeight = -textHeight + getPaddingTop();
        mTextHeight = -textHeight;

        // 判断未画的线 是不是到达 到达就把最变量start值 赋给最大值
        if (start + textWidth + paddingLT * 2 > mRealWidth) {
            start = mRealWidth;
            // 控制是否继续画
            isFinishRight = true;
        }
        if (!isFinishRight) {
            // 画右边线条
            drawRight(canvas);

        }
        // 标志 是否大于等于实际宽度
        float endX = start + textWidth + paddingLT * 2;
        if (endX >= mRealWidth) {
            // left 的最点位子，不再继续画
            start = mRealWidth - textWidth - paddingLT * 2;
            drawLeft(canvas, start);

        } else {
            // 不满足前面条件 则不断加上去
            drawLeft(canvas, start);
        }

        // 画进度  要判断endX也行  有点麻烦了
        drawText(canvas, text);

        canvas.restore();
    }

    /**
     * @param canvas
     *            左侧进度
     */
    private void drawRight(Canvas canvas) {

        canvas.drawLine(mLeft + mRealText, mLineHeight, mRealWidth
                + getPaddingLeft(), mLineHeight, rightPaint);

    }

    private void drawLeft(Canvas canvas, float start) {

        canvas.drawLine(getPaddingLeft(), mLineHeight, mLeft, mLineHeight,
                paint);

    }

    private void drawText(Canvas canvas, String text) {

        canvas.drawText(text, mLeft + paddingLT, mTextHeight, textPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        // 实际长度
        mRealWidth = w - getPaddingLeft() - getPaddingRight();

    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }
    String[]  colors=new String[]{"#fe9d01",
            "#fe9d01",
            "#ffbb1c",
            "#eed205",
            "#ff8c05",
            "#ff6600",
            "#ffa500"
    };
    //改变画笔颜色
    public void updatePaintColors(){
        Random random=new Random();

        this.paint.setARGB(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        this.textPaint.setARGB(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        this.rightPaint.setARGB(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));


//      int i=random.nextInt(colors.length);
//      this.paint.setColor(Color.parseColor(colors[i]));
//      int j=  random.nextInt(colors.length);
//      this.rightPaint.setColor(Color.parseColor(colors[j]));
    }
}
