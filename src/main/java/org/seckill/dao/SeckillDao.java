package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yanjunwang on 16/9/30.
 */
public interface SeckillDao {

    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return 如果影响行数te大于1，则表示更新的记录行数
     */

    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /**
     * 根据id查询秒杀对象
     * @param seckillId
     * @return
     */

    Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀记录列表
     * @param offset
     * @param limit
     * @return
     */

    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据存储过程查询秒杀对象
     * @param paramMap
     */

    void killByProcedure(Map<String,Object> paramMap);
}
