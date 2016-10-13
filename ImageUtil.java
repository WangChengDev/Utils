package com.yuantuan.ytwebview.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * =============================================================================
 * [YTF] (C)2015-2099 Yuantuan Inc.
 * Link        http://www.ytframework.cn
 * =============================================================================
 *
 * @author Like<572919350@qq.com>
 * @created 2016/8/17.
 * @description 图片处理工具类
 * =============================================================================
 */
public class ImageUtil {

    /**
     * 缩放图片
     * @param bitmap
     * @param dst_w
     * @param dst_h
     * @return
     */
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,true);
        return dstbmp;
    }

}
