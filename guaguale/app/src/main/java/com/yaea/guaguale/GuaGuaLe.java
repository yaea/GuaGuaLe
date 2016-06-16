package com.yaea.guaguale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by 慧客 on 2016/6/15.
 */
public class GuaGuaLe extends View {
    /**
     * 刮画笔
     */
    private Paint guaPaint;
    /**
     * 刮路径
     */
    private Path guaPath;
    /**
     * 画布
     */
    private Canvas mCanvas;
    private Bitmap bitmap;
    /**
     * 遮罩层bitmap
     */
    private Bitmap shadeBitmap;
    /**
     * 刮动时上一次坐标
     */
    private int lastX, lastY;
    /**
     * 信息
     */
    private String text;
    /**
     * 信息画笔
     */
    private Paint textPaint;
    /**
     * 信息区域
     */
    private Rect textBound;
    /**
     * 信息字体大小
     */
    private int textSize;
    /**
     * 信息字体颜色
     */
    private int textColor;
    /**
     * 刮完成监听
     */
    private CompleteListener listener;
    /**
     * 判断遮盖层区域是否消除达到阈值
     */
    private volatile boolean complete = false;

    public GuaGuaLe(Context context) {
        this(context, null);
    }

    public GuaGuaLe(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaGuaLe(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray ta = null;
        try {
            ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaLe, defStyleAttr, 0);
            int n = ta.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case R.styleable.GuaGuaLe_text:
                        text = ta.getString(attr);
                        break;
                    case R.styleable.GuaGuaLe_textColor:
                        textColor = ta.getColor(attr, 0x000000);
                        break;
                    case R.styleable.GuaGuaLe_textSize:
                        textSize = (int) ta.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
                        break;
                    default:
                        break;
                }
            }
        } finally {
            if (ta != null) {
                ta.recycle();
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        setShadePaint();
        setTextPaint();
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, guaPaint);
        mCanvas.drawBitmap(shadeBitmap, null, new Rect(0, 0, width, height), null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(text, getWidth() / 2 - textBound.width() / 2, getHeight() / 2 + textBound.height() / 2, textPaint);
        if (!complete) {
            drawPath();
            canvas.drawBitmap(bitmap, 0, 0, null);
        } else if (complete) {
            if (listener != null) {
                listener.complete();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                guaPath.moveTo(lastX, lastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - lastX);
                int dy = Math.abs(y - lastY);
                if (dx > 3 || dy > 3) {
                    guaPath.lineTo(x, y);
                }
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (!complete) {
                    new Thread(runnable).start();
                }
                break;
            default:
                break;
        }
        if (!complete) {
            invalidate();
        }
        return true;
    }

    /**
     * 初始化
     */
    private void init() {
        guaPaint = new Paint();
        guaPath = new Path();
        shadeBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.shade);
        text = "谢谢惠顾";
        textBound = new Rect();
        textPaint = new Paint();
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics());
    }

    public void setText(String text) {
        this.text = text;
        /**
         * 获取画笔绘制文字大宽和高
         */
        textPaint.getTextBounds(text, 0, text.length(), textBound);
    }

    public void setListener(CompleteListener listener) {
        this.listener = listener;
    }

    /**
     * 设置刮画笔
     */
    private void setShadePaint() {
        guaPaint.setColor(Color.parseColor("#c0c0c0"));
        guaPaint.setAntiAlias(true);//抗锯齿
        guaPaint.setDither(true);//防抖动
        guaPaint.setStrokeJoin(Paint.Join.ROUND);//设置结合处圆角
        guaPaint.setStrokeCap(Paint.Cap.ROUND);//画笔笔刷类型
        guaPaint.setStyle(Paint.Style.FILL);
        guaPaint.setStrokeWidth(20);
    }

    /**
     * 设置信息画笔
     */
    private void setTextPaint() {
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
        textPaint.getTextBounds(text, 0, text.length(), textBound);
    }

    private void drawPath() {
        guaPaint.setStyle(Paint.Style.STROKE);
        guaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(guaPath, guaPaint);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap tempBitmap = bitmap;
            int[] mPixels = new int[w * h];
            tempBitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                if (percent > 60) {
                    complete = true;
                    postInvalidate();
                }
            }
        }
    };

    public interface CompleteListener {
        public void complete();
    }
}
