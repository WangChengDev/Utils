package com.yuantuan.ytwebview.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * =============================================================================
 * [YTF] (C)2015-2099 Yuantuan Inc.
 * Link        http://www.ytframework.cn
 * =============================================================================
 *
 * @author laker<lakerandroiddev@gmail.com>
 * @created 2016/6/23.
 * @description Json工具类
 * =============================================================================
 */
public class JsonUtil {

    private static JsonUtil ourInstance = new JsonUtil();

    public static JsonUtil getInstance() {
        return ourInstance;
    }
    private JSONObject jsonObjectParam;

    private JsonUtil() {
    }

    /**
     * 将JSONObjec对象转换成Map集合
     *
     * @param jsonObject
     * @return
     */
    public static Map<String, String> toMap(JSONObject jsonObject) {
        {
            Map<String, String> result = new HashMap<String, String>();
            Iterator<String> iterator = jsonObject.keys();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = iterator.next();
                try {
                    value = jsonObject.getString(key);
                    result.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return result;
        }
    }
    private CharacterIterator it;
    private char              c;
    private int               col;
    /**
     * 验证一个字符串是否是合法的JSON串
     *
     * @param input 要验证的字符串
     * @return true-合法 ，false-非法
     */
    public boolean validate(String input) {
        input = input.trim();
        boolean ret = valid(input);
        return ret;
    }

    private boolean valid(String input) {
        if ("".equals(input)) return true;

        boolean ret = true;
        it = new StringCharacterIterator(input);
        c = it.first();
        col = 1;
        if (!value()) {
            ret = error("value", 1);
        } else {
            skipWhiteSpace();
            if (c != CharacterIterator.DONE) {
                ret = error("end", col);
            }
        }

        return ret;
    }

    private boolean value() {
        return literal("true") || literal("false") || literal("null") || string() || number() || object() || array();
    }

    private boolean literal(String text) {
        CharacterIterator ci = new StringCharacterIterator(text);
        char t = ci.first();
        if (c != t) return false;

        int start = col;
        boolean ret = true;
        for (t = ci.next(); t != CharacterIterator.DONE; t = ci.next()) {
            if (t != nextCharacter()) {
                ret = false;
                break;
            }
        }
        nextCharacter();
        if (!ret) error("literal " + text, start);
        return ret;
    }

    private boolean array() {
        return aggregate('[', ']', false);
    }

    private boolean object() {
        return aggregate('{', '}', true);
    }

    private boolean aggregate(char entryCharacter, char exitCharacter, boolean prefix) {
        if (c != entryCharacter) return false;
        nextCharacter();
        skipWhiteSpace();
        if (c == exitCharacter) {
            nextCharacter();
            return true;
        }

        for (;;) {
            if (prefix) {
                int start = col;
                if (!string()) return error("string", start);
                skipWhiteSpace();
                if (c != ':') return error("colon", col);
                nextCharacter();
                skipWhiteSpace();
            }
            if (value()) {
                skipWhiteSpace();
                if (c == ',') {
                    nextCharacter();
                } else if (c == exitCharacter) {
                    break;
                } else {
                    return error("comma or " + exitCharacter, col);
                }
            } else {
                return error("value", col);
            }
            skipWhiteSpace();
        }

        nextCharacter();
        return true;
    }

    private boolean number() {
        if (!Character.isDigit(c) && c != '-') return false;
        int start = col;
        if (c == '-') nextCharacter();
        if (c == '0') {
            nextCharacter();
        } else if (Character.isDigit(c)) {
            while (Character.isDigit(c))
                nextCharacter();
        } else {
            return error("number", start);
        }
        if (c == '.') {
            nextCharacter();
            if (Character.isDigit(c)) {
                while (Character.isDigit(c))
                    nextCharacter();
            } else {
                return error("number", start);
            }
        }
        if (c == 'e' || c == 'E') {
            nextCharacter();
            if (c == '+' || c == '-') {
                nextCharacter();
            }
            if (Character.isDigit(c)) {
                while (Character.isDigit(c))
                    nextCharacter();
            } else {
                return error("number", start);
            }
        }
        return true;
    }

    private boolean string() {
        if (c != '"') return false;

        int start = col;
        boolean escaped = false;
        for (nextCharacter(); c != CharacterIterator.DONE; nextCharacter()) {
            if (!escaped && c == '\\') {
                escaped = true;
            } else if (escaped) {
                if (!escape()) {
                    return false;
                }
                escaped = false;
            } else if (c == '"') {
                nextCharacter();
                return true;
            }
        }
        return error("quoted string", start);
    }

    private boolean escape() {
        int start = col - 1;
        if (" \\\"/bfnrtu".indexOf(c) < 0) {
            return error("escape sequence  \\\",\\\\,\\/,\\b,\\f,\\n,\\r,\\t  or  \\uxxxx ", start);
        }
        if (c == 'u') {
            if (!ishex(nextCharacter()) || !ishex(nextCharacter()) || !ishex(nextCharacter())
                    || !ishex(nextCharacter())) {
                return error("unicode escape sequence  \\uxxxx ", start);
            }
        }
        return true;
    }

    private boolean ishex(char d) {
        return "0123456789abcdefABCDEF".indexOf(c) >= 0;
    }

    private char nextCharacter() {
        c = it.next();
        ++col;
        return c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            nextCharacter();
        }
    }

    private boolean error(String type, int col) {
        System.out.printf("type: %s, col: %s%s", type, col, System.getProperty("line.separator"));
        return false;
    }

    public JSONObject getJsonObjectParam() {
        return jsonObjectParam;
    }

    public void setJsonObjectParam(JSONObject jsonObjectParam) {
        this.jsonObjectParam = jsonObjectParam;
    }

    public String optStr(String name, String defaultValue){
        return this.jsonObjectParam.optString(name,defaultValue);
    }

    public int optInt(String name, int defaultValue){
        return this.jsonObjectParam.optInt(name,defaultValue);
    }

    public boolean optInt(String name, boolean defaultValue){
        return this.jsonObjectParam.optBoolean(name,defaultValue);
    }

    public double optInt(String name, double defaultValue){
        return this.jsonObjectParam.optDouble(name,defaultValue);
    }

    public long optInt(String name, long defaultValue){
        return this.jsonObjectParam.optLong(name,defaultValue);
    }

    public JSONObject optInt(String name){
        return this.jsonObjectParam.optJSONObject(name);
    }

    public JSONArray optJSONArray(String name){
        return this.jsonObjectParam.optJSONArray(name);
    }




}
