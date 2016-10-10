package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yanjunwang on 16/10/1.
 * 配置spring和junit整合，为了junit启动时加载spring
 */

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件在哪
@ContextConfiguration("classpath:spring/spring-dao.xml")

public class SeckillDaoTest {

    //注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void testQueryById() throws Exception {
        long id=1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println("***************************************");
        System.out.println(seckill.getSeckillId());
        System.out.println(seckill.getName());
        System.out.println(seckill);

        /*
        1000元秒杀iphone6s
        Seckill{seckillId=1000,
        name='1000元秒杀iphone6s',
        number=150,
        startTime=Sun Nov 01 00:00:00 GMT 2015,
        endTime=Mon Nov 02 00:00:00 GMT 2015,
        createTime=Fri Sep 30 19:02:32 IST 2016}
         */
    }

    @Test
    public void testQueryAll() throws Exception {

        List<Seckill> seckills=seckillDao.queryAll(0,100);
        for(Seckill seckill:seckills){
            System.out.println(seckill);
        }
    }

    @Test
    public void testReduceNumber() throws Exception {

        Date killTime =new Date();
        int updateCount=seckillDao.reduceNumber(1000L,killTime);
        System.out.println(updateCount);
    }
}