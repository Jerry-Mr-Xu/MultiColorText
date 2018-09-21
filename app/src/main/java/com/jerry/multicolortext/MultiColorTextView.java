package com.jerry.multicolortext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
     * 背景区域
     */
    private Path backgroundPath;
    /**
     * 填充区域
     */
    private Path filledAreaPath;
    /**
     * 未填充区域
     */
    private Path unfilledAreaPath;

    /**
     * 填充进度(0-1.0)
     */
    @FloatRange(from = 0, to = 1)
    private float fillProgress;

    /**
     * 圆角半径（当形状是圆角矩形时{@link MultiColorTextView#SHAPE_TYPE_ROUND_RECT}）
     */
    private float roundCornerRadius;

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
        roundCornerRadius = 0;
        textRect = new Rect();
        backgroundPath = new Path();
        filledAreaPath = new Path();
        unfilledAreaPath = new Path();
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
        roundCornerRadius = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int paddingLeft = getPaddingLeft(), paddingTop = getPaddingTop(), paddingRight = getPaddingRight(), paddingBottom = getPaddingBottom();

        int resultWidth = MeasureSpec.getSize(widthMeasureSpec), resultHeight = MeasureSpec.getSize(heightMeasureSpec);

        bgPaint.getTextBounds(textContent, 0, textContent.length(), textRect);
        switch (shapeType) {
            case SHAPE_TYPE_CIRCLE: {
                int maxLength;
                // 如果是圆形
                if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                    // 如果宽高都是wrap_content，则取文字对角线为直径
                    int realWidth = textRect.width() + paddingLeft + paddingRight, realHeight = textRect.height() + paddingTop + paddingBottom;
                    maxLength = (int) Math.ceil(Math.sqrt(realWidth * realWidth + realHeight * realHeight));
                    resultWidth = resultHeight = maxLength;
                } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY) {
                    // 如果宽度是wrap_content高度是确定数值，则以高度为准
                    maxLength = resultHeight;
                    resultWidth = resultHeight = maxLength;
                } else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST) {
                    // 如果宽度是确定数值高度是wrap_content，则以宽度为准
                    maxLength = resultWidth;
                    resultWidth = resultHeight = maxLength;
                } else {
                    // 如果宽高都是确定数值则不做改变
                }
                break;
            }
            case SHAPE_TYPE_ROUND_RECT: {
                // 如果是圆角矩形
                if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                    // 如果宽高都是wrap_content，则取文字宽高
                    int realWidth = textRect.width() + paddingLeft + paddingRight, realHeight = textRect.height() + paddingTop + paddingBottom;
                    roundCornerRadius = Math.min(realWidth, realHeight) / 5.0f;
                    resultWidth = (int) (Math.ceil(roundCornerRadius + realWidth));
                    resultHeight = (int) (Math.ceil(roundCornerRadius + realHeight));
                } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY) {
                    // 如果宽度是wrap_content高度是确定数值，则以宽度为文字宽度
                    int realWidth = textRect.width() + paddingLeft + paddingRight;
                    roundCornerRadius = Math.min(realWidth, resultHeight) / 5.0f;
                    resultWidth = (int) (Math.ceil(roundCornerRadius + realWidth));
                } else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST) {
                    // 如果宽度是确定数值高度是wrap_content，则以高度为文字高度
                    int realHeight = textRect.height() + paddingTop + paddingBottom;
                    roundCornerRadius = Math.min(resultWidth, realHeight) / 5.0f;
                    resultHeight = (int) (Math.ceil(roundCornerRadius + realHeight));
                } else {
                    // 如果宽高都是确定数值则不做改变
                    roundCornerRadius = Math.min(resultWidth, resultHeight) / 5.0f;
                }
                break;
            }
            case SHAPE_TYPE_RECT:
            default: {
                // 如果是矩形
                if (widthMode == MeasureSpec.AT_MOST) {
                    // 如果宽度是wrap_content则获取文字宽度为最终宽度
                    resultWidth = textRect.width() + paddingLeft + paddingRight;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    // 如果高度是wrap_content则获取文字高度为最终高度
                    resultHeight = textRect.height() + paddingTop + paddingBottom;
                }
                break;
            }
        }

        viewRect.set(0, 0, resultWidth, resultHeight);
        setMeasuredDimension(resultWidth, resultHeight);
    }

    /**
     * 简要说一下绘画思路：
     * 1. 先得到背景Path、填充和非填充Path
     * 2. 分别将背景Path与填充Path和非填充Path相交最终得到的就是内容区域Path
     *
     * @param canvas 画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        backgroundPath.reset();
        filledAreaPath.reset();
        unfilledAreaPath.reset();

        switch (shapeType) {
            case SHAPE_TYPE_ROUND_RECT: {
                backgroundPath.addRoundRect(viewRect.left, viewRect.top, viewRect.right, viewRect.bottom, roundCornerRadius, roundCornerRadius, Path.Direction.CW);
                break;
            }
            case SHAPE_TYPE_CIRCLE: {
                backgroundPath.addCircle(viewRect.centerX(), viewRect.centerY(), Math.min(viewRect.width(), viewRect.height()) / 2.0f, Path.Direction.CW);
                break;
            }
            case SHAPE_TYPE_RECT:
            default: {
                backgroundPath.addRect(0, 0, viewRect.width(), viewRect.height(), Path.Direction.CW);
                break;
            }
        }
        switch (fillOrientation) {
            case FILL_ORIENTATION_HOR: {
                // 从左边开始填充
                int progressLineX = Math.min((int) (viewRect.width() * fillProgress), viewRect.width());
                filledAreaPath.addRect(0, 0, progressLineX, viewRect.height(), Path.Direction.CW);
                unfilledAreaPath.addRect(progressLineX, 0, viewRect.width(), viewRect.height(), Path.Direction.CW);
                break;
            }
            case FILL_ORIENTATION_VER: {
                // 从下边开始填充
                int progressLineY = Math.min((int) (viewRect.height() * (1 - fillProgress)), viewRect.height());
                unfilledAreaPath.addRect(0, 0, viewRect.width(), progressLineY, Path.Direction.CW);
                filledAreaPath.addRect(0, progressLineY, viewRect.width(), viewRect.height(), Path.Direction.CW);
                break;
            }
            default: {
                // 异常情况默认不填充
                unfilledAreaPath.addRect(0, 0, viewRect.width(), viewRect.height(), Path.Direction.CW);
                break;
            }
        }
        filledAreaPath.op(backgroundPath, Path.Op.INTERSECT);
        unfilledAreaPath.op(backgroundPath, Path.Op.INTERSECT);

        // 填充区域颜色相反
        bgPaint.setColor(fgColor);
        fgPaint.setColor(bgColor);
        drawContentInRect(canvas, filledAreaPath);

        bgPaint.setColor(bgColor);
        fgPaint.setColor(fgColor);
        drawContentInRect(canvas, unfilledAreaPath);
    }

    /**
     * 在给定矩形区域绘制内容
     *
     * @param canvas      画布
     * @param contentArea 内容区域
     */
    private void drawContentInRect(Canvas canvas, Path contentArea) {
        canvas.save();
        canvas.clipPath(contentArea);
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

        // 文字要去除Padding居中
        Rect contentRect = new Rect(viewRect);
        contentRect.set(viewRect.left + getPaddingLeft(), viewRect.top + getPaddingTop(), viewRect.right - getPaddingRight(), viewRect.bottom - getPaddingBottom());

        canvas.drawText(textContent, contentRect.centerX() - textWidth / 2, contentRect.centerY() + textHeight / 2 - fontMetrics.bottom, fgPaint);
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
                canvas.drawRoundRect(viewRect.left, viewRect.top, viewRect.right, viewRect.bottom, roundCornerRadius, roundCornerRadius, bgPaint);
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
        invalidate();
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
