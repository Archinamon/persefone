/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package mobi.anoda.archinamon.kernel.persefone.ui.widget.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.widget.photoview.PhotoViewAttacher.IOnZoomHandler;
import mobi.anoda.archinamon.kernel.persefone.ui.widget.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import mobi.anoda.archinamon.kernel.persefone.ui.widget.photoview.PhotoViewAttacher.OnPhotoTapListener;
import mobi.anoda.archinamon.kernel.persefone.ui.widget.photoview.PhotoViewAttacher.OnViewTapListener;

public class PhotoView extends ImageView implements IPhotoView {

    private final PhotoViewAttacher mAttacher;
    private       ScaleType         mPendingScaleType;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        mAttacher = new PhotoViewAttacher(this);

        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
    }

    public float getPhotoViewRotation() {
        return mAttacher.getLastRotation();
    }

    @Implement
    public void setPhotoViewRotation(float rotationDegree) {
        mAttacher.setPhotoViewRotation(rotationDegree);
    }

    @Implement
    public boolean canZoom() {
        return mAttacher.canZoom();
    }

    @Implement
    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Implement
    public Matrix getDisplayMatrix() {
        return mAttacher.getDrawMatrix();
    }

    @Implement
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return mAttacher.setDisplayMatrix(finalRectangle);
    }

    @Implement
    @Deprecated
    public float getMinScale() {
        return getMinimumScale();
    }

    @Implement
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Implement
    @Deprecated
    public float getMidScale() {
        return getMediumScale();
    }

    @Implement
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Implement
    @Deprecated
    public float getMaxScale() {
        return getMaximumScale();
    }

    @Implement
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Implement
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public ScaleType getScaleType() {
        return mAttacher.getScaleType();
    }

    @Implement
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Implement
    @Deprecated
    public void setMinScale(float minScale) {
        setMinimumScale(minScale);
    }

    @Implement
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Implement
    @Deprecated
    public void setMidScale(float midScale) {
        setMediumScale(midScale);
    }

    @Implement
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Implement
    @Deprecated
    public void setMaxScale(float maxScale) {
        setMaximumScale(maxScale);
    }

    @Implement
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Implement
    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mAttacher.setOnLongClickListener(l);
    }

    @Implement
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Implement
    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    public void setOnZoomHandler(IOnZoomHandler handler) {
        mAttacher.setOnZoomHandler(handler);
    }

    @Implement
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Implement
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Implement
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (null != mAttacher) {
            mAttacher.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    @Implement
    public void setZoomable(boolean zoomable) {
        mAttacher.setZoomable(zoomable);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.cleanup();
        super.onDetachedFromWindow();
    }

}