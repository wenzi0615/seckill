package org.seckill.exception;

/**
 * 秒杀相关业务异常
 * Created by yanjunwang on 16/10/1.
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message,cause);
    }

}
