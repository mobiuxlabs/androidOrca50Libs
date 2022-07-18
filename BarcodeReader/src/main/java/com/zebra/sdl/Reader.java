package com.zebra.sdl;

import android.content.Context;

import com.zebra.model.Consumer;

/**
 * @author naz
 * Email 961057759@qq.com
 * Date 2020/7/30
 */
public interface Reader {
    /**
     * 打开读写器或者二维头等设备
     *
     * @param context Context
     * @return bool
     */
    boolean open(Context context);

    /**
     * 设置结果回调
     *
     * @param onResult String类型的结果
     */
    void setResultCallback(Consumer<String> onResult);

    /**
     * 读取
     *
     * @param loop 是否循环读取(针对UHF)
     */
    void read(boolean loop);

    /**
     * 关闭设备
     */
    void close();
}
