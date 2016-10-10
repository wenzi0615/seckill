package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yanjunwang on 16/10/1.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceImplTest {

    @Test
    public void executeSeckillProcedure() throws Exception {

        long seckillId = 1003;
        long phone = 1111111117L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(execution.getStateInfo());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {

        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list=",list);
    }

    @Test
    public void getById() throws Exception {
        long id=1000L;
        Seckill seckill=seckillService.getById(id);
        logger.info("seckill=",seckill);
    }

    @Test
    public void testSeckillLogic() throws Exception {
        long id=1000L;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long phone=43322221446L;
            String md5="635e31b499202cff3df7257814bf1c39";
            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
                logger.info("seckillExecution={}", seckillExecution);
            }catch(RepeatKillException e){
                logger.error(e.getMessage());
            }catch(SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }else{
            logger.warn("秒杀未开启");
        }
    }

}