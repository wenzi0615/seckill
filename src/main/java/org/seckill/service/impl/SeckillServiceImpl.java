package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务接口；站在使用者角度设计接口
 * 三个方面：方法定义粒度，参数，返回类型
 * Created by yanjunwang on 16/10/1.
 */

@Service
public class SeckillServiceImpl implements SeckillService{

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //自动注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串
    private final String  salt="asfasdfhas8er7fdyhfa";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Transactional
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点:缓存优化 建立在超时的基础上维护一致性
        //秒杀单通常不改 有错废弃重建
        /**
         * get from cache
         * if null
         * get db
         * else
         * put cache
         * loc
         */
        //1.访问redis
        Seckill seckill= redisDao.getSeckill(seckillId);
//        Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill==null){
            //2.访问数据库
            seckill=seckillDao.queryById(seckillId);
            if (seckill==null){
                return new Exposer(false, seckillId);
            }else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
//        if(seckill == null) {
//            return new Exposer(false, seckillId);
//        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if(nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //转化特定字符串的过程，不可逆
        String md5=getMD5(seckillId);
        return new Exposer(true, md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base=seckillId+"/"+salt;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillException {

        if(md5==null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime=new Date();

        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一：seckillId,userPhone
            if (insertCount <= 0) {
                //重复秒杀
                System.out.println("insertCount: "+insertCount);
                throw new RepeatKillException("seckil repeated");
            } else {
                //减库存,热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录, 秒杀结束, rollback
                    //insert操作有回滚吗？
                    throw new SeckillCloseException("seckill is closed");
                    /*
                     * 持有行级锁是在update上,释放锁是在commit(spring控制)，
                     * 也就是锁持有时间是update和commit之间的时间。
                     * 这个过程网络请求越少，锁持有时间就越短。
                     */
                } else {
                    //秒杀成功 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }

            }


        }catch(SeckillCloseException e) {
            throw e;
        }catch(RepeatKillException e) {
            throw e;
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            //所有编译器异常，转化为运行期异常
            throw new SeckillCloseException("seckill inner error:" + e.getLocalizedMessage());
        }
    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)
    {

        if(md5==null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWITE);
        }
        Date killTime=new Date();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程, result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result=MapUtils.getInteger(map,"result",-2);
            if(result==1){
                SuccessKilled sk=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return  new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
        //
//        return null;
    }

}
