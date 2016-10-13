package com.queen.rxjavaretrofitdemo.http;

import android.text.TextUtils;

import com.queen.rxjavaretrofitdemo.utils.L;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * =============================================================================
 * [YTF] (C)2015-2099 Yuantuan Inc.
 * Link        http://www.ytframework.cn
 * =============================================================================
 *
 * @author laker<lakerandroiddev@gmail.com>
 * @created 2016/6/22.
 * @description 日志拦截器
 * =============================================================================
 */
public class LoggerInterceptor implements Interceptor {
    public static final String TAG = "OkHttpLog";
    private String tag;
    private boolean showResponse;

    public LoggerInterceptor(String tag) {
        this(tag, false);
    }

    public LoggerInterceptor(String tag, boolean showResponse) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        this.showResponse = showResponse;
        this.tag = tag;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);

        return logForResponse(response);
    }

    private void logForRequest(Request request) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();

            L.d(tag, "---------------------request log start---------------------");
            L.d(tag, "method : " + request.method());
            L.d(tag, "url : " + url);
            if (headers != null && headers.size() > 0) {
                L.d(tag, "headers : \n");
                L.d(tag, headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    L.d(tag, "contentType : " + mediaType.toString());
                    if (isText(mediaType)) {
                        L.d(tag, "content : " + bodyToString(request));
                    } else {
                        L.d(tag, "content : " + " maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            L.d(tag, "---------------------request log end-----------------------");
        }
    }

    private Response logForResponse(Response response) {
        try {
            L.d(tag, "---------------------response log start---------------------");
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            L.d(tag, "headers : " + clone.headers().toString());
            L.d(tag, "url : " + clone.request().url());
            L.d(tag, "code : " + clone.code());
            L.d(tag, "protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message())) //L.d(tag, "message : " + clone.message());

            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        L.d(tag, "contentType : " + mediaType.toString());
                        if (isText(mediaType)) {
                            String resp = body.string();
                            L.d( "content : " + resp);
                            body = ResponseBody.create(mediaType, resp);
                            return response.newBuilder().body(body).build();
                        } else {
                            L.d(tag, "content : " + " maybe [file part] , too large too print , ignored!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            L.d(tag, "---------------------response log end-----------------------");
        }

        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("widget/html") ||
                    mediaType.subtype().equals("webviewhtml")) //
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
