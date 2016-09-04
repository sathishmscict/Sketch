/*
 * Copyright (C) 2016 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketchsample.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import me.xiaopan.sketch.SketchImageView;
import me.xiaopan.sketch.feature.large.LargeImageViewer;
import me.xiaopan.sketch.feature.large.Tile;
import me.xiaopan.sketchsample.util.DeviceUtils;

public class MappingView extends SketchImageView implements LargeImageViewer.OnTileChangedListener {

    private LargeImageViewer largeImageViewer;

    private Rect visibleRect;
    private Paint visiblePaint;
    private Paint drawTilesPaint;
    private Paint drawRectPaint;
    private Paint loadingTilePaint;

    private int cacheOriginImageWidth;
    private Rect cacheVisibleRect;

    public MappingView(Context context) {
        super(context);
        init(context);
    }

    public MappingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MappingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        visibleRect = new Rect();
        visiblePaint = new Paint();
        visiblePaint.setColor(Color.RED);
        visiblePaint.setStyle(Paint.Style.STROKE);
        visiblePaint.setStrokeWidth(DeviceUtils.dp2px(context, 1));

        setScaleType(ScaleType.FIT_XY);


        drawTilesPaint = new Paint();
        drawTilesPaint.setColor(Color.parseColor("#88A020F0"));
        drawTilesPaint.setStrokeWidth(DeviceUtils.dp2px(context, 1));
        drawTilesPaint.setStyle(Paint.Style.STROKE);

        loadingTilePaint = new Paint();
        loadingTilePaint.setColor(Color.parseColor("#880000CD"));
        loadingTilePaint.setStrokeWidth(DeviceUtils.dp2px(context, 1));
        loadingTilePaint.setStyle(Paint.Style.STROKE);

        drawRectPaint = new Paint();
        drawRectPaint.setColor(Color.parseColor("#8800CD00"));
        drawRectPaint.setStrokeWidth(DeviceUtils.dp2px(context, 1));
        drawRectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (largeImageViewer != null) {
            float scale = (float) largeImageViewer.getExecutor().getDecoder().getImageWidth() / getWidth();

            for (Tile tile : largeImageViewer.getTileManager().getTileList()) {
                if (!tile.isEmpty()) {
                    canvas.drawRect((tile.srcRect.left + 1) / scale,
                            (tile.srcRect.top + 1) / scale,
                            (tile.srcRect.right - 1) / scale,
                            (tile.srcRect.bottom - 1) / scale, drawTilesPaint);
                } else if (!tile.isDecodeParamEmpty()) {
                    canvas.drawRect((tile.srcRect.left + 1) / scale,
                            (tile.srcRect.top + 1) / scale,
                            (tile.srcRect.right - 1) / scale,
                            (tile.srcRect.bottom - 1) / scale, loadingTilePaint);
                }
            }

            Rect srcRect = largeImageViewer.getTileManager().getSrcRect();
            if (!srcRect.isEmpty()) {
                canvas.drawRect((srcRect.left) / scale,
                        (srcRect.top) / scale,
                        (srcRect.right) / scale,
                        (srcRect.bottom) / scale, drawRectPaint);
            }
        }

        if (!visibleRect.isEmpty()) {
            canvas.drawRect(visibleRect, visiblePaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (cacheOriginImageWidth != 0 && cacheVisibleRect != null && !cacheVisibleRect.isEmpty()) {
            update(cacheOriginImageWidth, cacheVisibleRect);
            cacheOriginImageWidth = 0;
            cacheVisibleRect.setEmpty();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        int maxWidth = getResources().getDisplayMetrics().widthPixels / 2;
        int maxHeight = getResources().getDisplayMetrics().heightPixels / 2;
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth > maxWidth || drawableHeight > maxHeight) {
            float finalScale = Math.min((float) maxWidth / drawableWidth, (float) maxHeight / drawableHeight);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = (int) (drawableWidth * finalScale);
            layoutParams.height = (int) (drawableHeight * finalScale);
            setLayoutParams(layoutParams);
        }

        super.setImageDrawable(drawable);
    }

    public void update(int originImageWidth, Rect newVisibleRect) {
        if (originImageWidth == 0 || newVisibleRect.isEmpty()) {
            if (!visibleRect.isEmpty()) {
                visibleRect.setEmpty();
                invalidate();
            }
            return;
        }

        if (getWidth() == 0 || getDrawable() == null) {
            if (!visibleRect.isEmpty()) {
                visibleRect.setEmpty();
                invalidate();
            }

            cacheOriginImageWidth = originImageWidth;
            if (cacheVisibleRect == null) {
                cacheVisibleRect = new Rect();
            }
            cacheVisibleRect.set(newVisibleRect);
            return;
        }

        int selfWidth = getWidth();
        float scale = (float) selfWidth / originImageWidth;
        this.visibleRect.set(
                Math.round(newVisibleRect.left * scale),
                Math.round(newVisibleRect.top * scale),
                Math.round(newVisibleRect.right * scale),
                Math.round(newVisibleRect.bottom * scale));
        invalidate();
    }

    @Override
    public void onTileChanged(LargeImageViewer largeImageViewer) {
        this.largeImageViewer = largeImageViewer;
        invalidate();
    }
}
