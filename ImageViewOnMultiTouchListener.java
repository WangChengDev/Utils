package com.view.ui;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageViewOnMultiTouchListener implements View.OnTouchListener {

	private final int NONE = 0;
	private final int MOVE = 1;
	private final int ZOOM = 2;
	private final int DRAG = 3;

	private int mode = NONE;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDistance = 0;
	
	float oldRotation = 0; 
	private boolean canRotate = false;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView view = (ImageView) v;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			matrix.set(view.getImageMatrix());
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());

			mode = DRAG;

			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			oldDistance = spacing(event);
			
			oldRotation = rotation(event); 
			
			if (oldDistance > 5f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);

				mode = ZOOM;
			}

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;

			break;

		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY()
						- start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				//注释了旋转的功能
				if (newDist > 5f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDistance;
					float rotation = rotation(event) - oldRotation;  
					
					matrix.postScale(scale, scale, mid.x, mid.y);
					if (isCanRotate()) {
						matrix.postRotate(rotation, mid.x, mid.y);// 旋轉  
					}
					
					
				}
			}

			break;
		}

		view.setImageMatrix(matrix);
		view.setScaleType(ImageView.ScaleType.MATRIX);
		view.setPadding(3, 5, 3, 5);

		return true;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);

		point.set(x / 2, y / 2);
	}
	
	
    public boolean isCanRotate() {
		return canRotate;
	}

	public void setCanRotate(boolean canRotate) {
		this.canRotate = canRotate;
	}

	// 取旋转角度 
    private float rotation(MotionEvent event) {  
        double delta_x = (event.getX(0) - event.getX(1));  
        double delta_y = (event.getY(0) - event.getY(1));  
        double radians = Math.atan2(delta_y, delta_x);  
        return (float) Math.toDegrees(radians);  
    } 
}