package com.yuantuan.ytwebview.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * =============================================================================
 * [YTF] (C)2015-2099 Yuantuan Inc.
 * Link        http://www.ytframework.cn
 * =============================================================================
 *
 * @author Like<572919350@qq.com>
 * @created 2016/6/28.
 * @description 时间有关的工具类
 * =============================================================================
 */
public class TimeUtils {

    public static String getSystemTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

}
