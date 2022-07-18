package com.zebra.model;

/**
 * A functional interface (callback) that accepts a single value.
 *
 * @param <T> the value type
 * @author naz
 * Email 961057759@qq.com
 * Date 2020/7/16
 */
public interface Consumer<T> {
    /**
     * Consume the given value.
     *
     * @param t the value
     * @throws Exception on error
     */
    void accept(T t) throws Exception;
}
