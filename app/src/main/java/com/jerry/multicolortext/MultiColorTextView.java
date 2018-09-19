package com.jerry.multicolortext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 多颜色文字（例如上半部分红色，下半部分黄色）
 *
 * @author xujierui
 * @date 2018/9/18
 */

public class MultiColorTextView extends View {
    private static final String TAG = "MultiColorTextView";
    public static final int SHAPE_TYPE_DEFAULT = 0, SHAPE_TYPE_RECT = 1, SHAPE_TYPE_CIRCLE = 2, SHAPE_TYPE_ROUND_RECT = 3;
    public static final int FILL_ORIENTATION_DEFAULT = 0, FILL_ORIENTATION_HOR = 1, FILL_ORIENTATION_VER = 2;

    /**
     * 背景画笔和前景画笔
     */
    private Paint bgPaint, fgPaint;

    /**
     * 文字内容
     */
    private String textContent;
    /**
     * 文字大小
     */
    private int textSize;
    /**
     * 背景色
     */
    private int bgColor;
    /**
     * 前景色
     */
    private int fgColor;
    /**
     * 形状类型
     * {@link MultiColorTextView#SHAPE_TYPE_RECT} 矩形
     * {@link MultiColorTextView#SHAPE_TYPE_CIRCLE} 圆形
     * {@link MultiColorTextView#SHAPE_TYPE_ROUND_RECT} 圆角矩形
     */
    private int shapeType;
    /**
     * 填充方向
     */
    private int fillOrientation;

    /**
     * 控件区域
     */
    private Rect viewRect;
    /**
     * 文字显示区域
     */
    private Rect textRect;
    /**
     * 填充区域
     */
    private Rect filledRect;
    /**
     * 未填充区域
     */
    private Rect unfilledRect;
    /**
     * 填充进度(0-1.0)
     */
    @FloatRange(from = 0, to = 1)
    private float fillProgress;

    public MultiColorTextView(Context context) {
        super(context);
        init(context, null);
    }

    public MultiColorTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiColorTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化操作
     *
     * @param context 上下文
     * @param attrs   xml属性
     */
    private void init(Context context, AttributeSet attrs) {
        // 提供默认值
        textContent = "我";
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, context.getResources().getDisplayMetrics());
        bgColor = ContextCompat.getColor(context, R.color.default_bg_color);
        fgColor = ContextCompat.getColor(context, R.color.default_fill_color);
        shapeType = SHAPE_TYPE_DEFAULT;
        fillProgress = 0;
        fillOrientation = FILL_ORIENTATION_DEFAULT;
        textRect = new Rect();
        filledRect = new Rect();
        unfilledRect = new Rect();
        viewRect = new Rect();

        // 获取xml中设置的属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiColorTextView);
        if (typedArray != null) {
            textContent = typedArray.getString(R.styleable.MultiColorTextView_android_text);
            textSize = typedArray.getDimensionPixelSize(R.styleable.MultiColorTextView_android_textSize, textSize);
            bgColor = typedArray.getInt(R.styleable.MultiColorTextView_background_color, bgColor);
            fgColor = typedArray.getInt(R.styleable.MultiColorTextView_foreground_color, fgColor);
            shapeType = typedArray.getInt(R.styleable.MultiColorTextView_shape_type, SHAPE_TYPE_RECT);
            fillProgress = typedArray.getFraction(R.styleable.MultiColorTextView_fill_progress, 1, 1, fillProgress);
            fillOrientation = typedArray.getInt(R.styleable.MultiColorTextView_fill_orientation, fillOrientation);

            typedArray.recycle();
        }

        // 初始化两个画笔
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        bgPaint.setTextSize(textSize);

        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint.setStyle(Paint.Style.FILL);
        fgPaint.setColor(fgColor);
        fgPaint.setTextSize(textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        textRect.setEmpty();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidth = MeasureSpec.getSize(widthMeasureSpec), resultHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            // 如果宽度是wrap_content则获取文字宽度为最终宽度
            if (textRect.isEmpty()) {
                bgPaint.getTextBounds(textContent, 0, textContent.length(), textRect);
            }
            resultWidth = textRect.width();
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            // 如果高度是wrap_content则获取文字高度为最终高度
            if (textRect.isEmpty()) {
                bgPaint.getTextBounds(textContent, 0, textContent.length(), textRect);
            }
            resultHeight = textRect.height();
        }

        if (shapeType == SHAPE_TYPE_CIRCLE) {
            // 如果是圆形则取宽高的最大值
            int maxLength = Math.max(resultWidth, resultHeight);
            resultWidth = resultHeight = maxLength;
        }

        viewRect.set(0, 0, resultWidth, resultHeight);
        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        filledRect.setEmpty();
        unfilledRect.setEmpty();

        switch (fillOrientation) {
            case FILL_ORIENTATION_HOR: {
                // 从左边开始填充
                filledRect.set(0, 0, Math.min((int) (viewRect.width() * fillProgress), viewRect.width()), viewRect.height());
                unfilledRect.set(filledRect.right, 0, viewRect.width(), viewRect.height());
                break;
            }
            case FILL_ORIENTATION_VER: {
                // 从下边开始填充
                unfilledRect.set(0, 0, viewRect.width(), Math.min((int) (viewRect.height() * (1 - fillProgress)), viewRect.height()));
                filledRect.set(0, unfilledRect.bottom, viewRect.width(), viewRect.height());
                break;
            }
            default: {
                // 异常情况默认不填充
                unfilledRect.set(0, 0, viewRect.width(), viewRect.height());
                break;
            }
        }

        // 填充区域颜色相反
        bgPaint.setColor(fgColor);
        fgPaint.setColor(bgColor);
        drawContentInRect(canvas, filledRect);

        bgPaint.setColor(bgColor);
        fgPaint.setColor(fgColor);
        drawContentInRect(canvas, unfilledRect);
    }

    /**
     * 在给定矩形区域绘制内容
     *
     * @param canvas      画布
     * @param contentRect 内容区域
     */
    private void drawContentInRect(Canvas canvas, Rect contentRect) {
        canvas.save();
        canvas.clipRect(contentRect);
        drawBackground(canvas);
        drawForeground(canvas);
        canvas.restore();
    }

    /**
     * 绘制前景
     *
     * @param canvas 画布
     */
    private void drawForeground(Canvas canvas) {
        Paint.FontMetrics fontMetrics = fgPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float textWidth = fgPaint.measureText(textContent, 0, textContent.length());
        canvas.drawText(textContent, viewRect.centerX() - textWidth / 2, viewRect.centerY() + textHeight / 2 - fontMetrics.bottom, fgPaint);
    }

    /**
     * 绘制背景
     *
     * @param canvas 画布
     */
    private void drawBackground(Canvas canvas) {
        switch (shapeType) {
            case SHAPE_TYPE_CIRCLE: {
                canvas.drawCircle(viewRect.centerX(), viewRect.centerY(), Math.min(viewRect.width(), viewRect.height()) / 2.0f, bgPaint);
                break;
            }
            case SHAPE_TYPE_ROUND_RECT: {
                float radius = Math.min(viewRect.width(), viewRect.height()) / 5.0f;
                canvas.drawRoundRect(new RectF(viewRect), radius, radius, bgPaint);
                break;
            }
            case SHAPE_TYPE_RECT:
            default: {
                canvas.drawRect(0, 0, viewRect.width(), viewRect.height(), bgPaint);
                break;
            }
        }
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        invalidate();
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }

    public int getFgColor() {
        return fgColor;
    }

    public void setFgColor(int fgColor) {
        this.fgColor = fgColor;
        invalidate();
    }

    public int getShapeType() {
        return shapeType;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
        requestLayout();
    }

    public int getFillOrientation() {
        return fillOrientation;
    }

    public void setFillOrientation(int fillOrientation) {
        this.fillOrientation = fillOrientation;
        invalidate();
    }

    public float getFillProgress() {
        return fillProgress;
    }

    public void setFillProgress(float fillProgress) {
        this.fillProgress = fillProgress;
        invalidate();
    }
}
