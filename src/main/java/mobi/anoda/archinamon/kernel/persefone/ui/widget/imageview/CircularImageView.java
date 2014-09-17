package mobi.anoda.archinamon.kernel.persefone.ui.widget.imageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.R;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class CircularImageView extends ImageView {

    private int mBorderWidth = 4;
    private int     mViewWidth;
    private int     mViewHeight;
    private Bitmap  mImage;
    private Paint   mPaint;
    private Paint   mPaintBorder;

    public CircularImageView(Context context) {
        this(context, null);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();

        if (!isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle, 0);
            assert a != null;

            mBorderWidth = a.getDimensionPixelSize(R.styleable.CircularImageView_circleBorderWidth, 0);

            int color = a.getColor(R.styleable.CircularImageView_circleBorderColor, 0x0);
            mPaintBorder.setColor(color);

            a.recycle();
        }
    }

    private void setup() {
        // init paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaintBorder = new Paint();
        setBorderColor(Color.WHITE);
        mPaintBorder.setAntiAlias(true);
    }

    public void drawShadow() {
        this.setLayerType(LAYER_TYPE_SOFTWARE, mPaintBorder);
        mPaintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
    }

    public void setBorderWidth(int borderWidth) {
        this.mBorderWidth = borderWidth;
        this.invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (mPaintBorder != null) {
            mPaintBorder.setColor(borderColor);
        }

        this.invalidate();
    }

    private void loadBitmap() {
        BitmapDrawable bitmapDrawable = null;

        Drawable content = this.getDrawable();
        if (content instanceof BitmapDrawable) {
            bitmapDrawable = (BitmapDrawable) content;
        }

        if (bitmapDrawable != null) {
            mImage = bitmapDrawable.getBitmap();
        }
    }

    @Override
    @SuppressLint("DrawAllocation")
    public void onDraw(@NonNull Canvas canvas) {
        // load the bitmap
        loadBitmap();

        // init shader
        if (mImage != null) {
            BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(mImage, canvas.getWidth(), canvas.getHeight(), false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(shader);
            int circleCenter = mViewWidth / 2;

            // circleCenter is the x or y of the view's center
            // radius is the radius in pixels of the cirle to be drawn
            // paint contains the shader that will texture the shape
            canvas.drawCircle(circleCenter + mBorderWidth, circleCenter + mBorderWidth, circleCenter + mBorderWidth - 4.0f, mPaintBorder);
            canvas.drawCircle(circleCenter + mBorderWidth, circleCenter + mBorderWidth, circleCenter - 4.0f, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        mViewWidth = width - (mBorderWidth * 2);
        mViewHeight = height - (mBorderWidth * 2);

        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = mViewWidth;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);
        int result;

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = mViewHeight;
        }

        return (result + 2);
    }
}