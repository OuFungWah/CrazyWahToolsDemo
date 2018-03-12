package com.crazywah.crazywahtools.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * 用于方便使用SharePreferences对数据进行持久化的工具类
 * Created by FungWah on 2018/3/12.
 */

public class SPUtil {

    /**
     * 全局上下文对象
     */
    private static Context context = null;
    /**
     * SharePreferences对象
     */
    private static SharedPreferences sharedPreferences = null;
    /**
     * 本类的单例对象
     */
    private static SPUtil spUtil = null;

    private SPUtil() {

    }

    /**
     *  初始化SPUtil
     *  建议在自定义的Application类中调用本方法
     * @param context 上下文对象
     */
    public static void init(Context context) {
        SPUtil.context = context;
        if (spUtil == null) {
            synchronized (SPUtil.class) {
                if (spUtil == null) {
                    spUtil = new SPUtil();
                }
            }
        }
    }

    /**
     * 获取SPUtil对象
     *  默认为私有模式
     * @param name 存储SharePreferences数据的文件的文件名
     * @return 返回本类的单例对象
     * @throws Exception 如果还未进行初始化即抛出异常
     */
    public static SPUtil getInstance(String name) throws Exception {
        return getInstance(name,context.MODE_PRIVATE);
    }

    /**
     * 获取SPUtil对象
     *
     * 传入文件名和访问模式来获取SharePreferences对象
     * MODE_PRIVATE：即只允许本应用访问
     * MODE_WORLD_READABLE:所有应用都可以阅读该文件
     * MODE_WORLD_WRITEABLE:所有应用都可以写入
     *
     * @param name 存储SharePreferences数据的文件的文件名
     * @param mode 文件的访问模式
     * @return 返回本类的单例对象
     * @throws Exception 如果还未进行初始化即抛出异常
     */
    public static SPUtil getInstance(String name,int mode) throws Exception {
        if (spUtil != null) {
            sharedPreferences = context.getSharedPreferences(name, mode);
            return spUtil;
        } else {
            throw new Exception("请初始化本类");
        }
    }

    /**
     * 存放随便什么类型的对象
     *
     * @param key 键
     * @param object 值
     */
    public void putObject(String key, Object object) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (sharedPreferences != null) {
                if (object instanceof Integer) {
                    editor.putInt(key, (Integer) object);
                } else if (object instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) object);
                } else if (object instanceof Float) {
                    editor.putFloat(key, (Float) object);
                } else if (object instanceof Long) {
                    editor.putLong(key, (Long) object);
                } else if (object instanceof String) {
                    editor.putString(key, (String) object);
                } else if (object instanceof Set) {
                    editor.putStringSet(key, (Set<String>) object);
                }
            }
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放字符串类型
     *
     * @param key 存储的键
     * @param value
     */
    public void putString(String key, String value) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放整型
     *
     * @param key 存储的键
     * @param value
     */
    public void putInt(String key, int value) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放布尔值
     *
     * @param key 存储的键
     * @param value
     */
    public void putBoolean(String key, boolean value) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放浮点型
     *
     * @param key 存储的键
     * @param value
     */
    public void putFloat(String key, float value) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(key, value);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放长整形
     *
     * @param key 存储的键
     * @param value
     */
    public void putLong(String key, long value) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(key, value);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * 存放字符数组
     *
     * @param key 存储的键
     * @param values
     */
    public void putStringSet(String key, Set<String> values) {
        if (sharedPreferences != null) {
            //先获取SharedPreferences的editor对象以对文件进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(key, values);
            //提交修改操作
            editor.commit();
        }
    }

    /**
     * @param key 存储的键
     * @param defBack
     * @return
     */
    public String getString(String key, String defBack) {
        return sharedPreferences.getString(key, defBack);

    }

    /**
     * 获取整型数据
     *
     * @param key 存储的键
     * @param defBack
     * @return
     */
    public int getInt(String key, int defBack) {
        return sharedPreferences.getInt(key, defBack);

    }

    /**
     * 获取布尔值数据
     *
     * @param key 存储的键
     * @param defBack
     * @return
     */
    public boolean getBoolean(String key, boolean defBack) {
        return sharedPreferences.getBoolean(key, defBack);

    }

    /**
     * 获取浮点型数据
     *
     * @param key 存储的键
     * @param defBack
     * @return
     */
    public float getFloat(String key, float defBack) {
        return sharedPreferences.getFloat(key, defBack);

    }

    /**
     * 获取字符串集合数据
     *
     * @param key 存储的键
     * @param defBack
     * @return
     */
    public Set<String> getStringSet(String key, Set<String> defBack) {
        return sharedPreferences.getStringSet(key, defBack);

    }

    /**
     * 清除指定文件的数据
     * @param name 要清除数据的文件名
     * @return 返回本类单例对象
     */
    public SPUtil remove(String name) {
        sharedPreferences.edit().remove(name).apply();
        return spUtil;
    }

    /**
     * 清除本应用的所有sharedPreferences产生的存储文件
     *
     * @return 返回本类单例对象
     */
    public SPUtil clearAll() {
        sharedPreferences.edit().clear().apply();
        return spUtil;
    }

}
