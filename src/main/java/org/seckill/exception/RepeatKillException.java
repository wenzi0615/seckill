package org.seckill.exception;

/**
 * 重复秒杀异常
 * Created by yanjunwang on 16/10/1.
 */
public class RepeatKillException extends SeckillException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message,cause);
    }

}
