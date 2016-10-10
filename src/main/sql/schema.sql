CREATE DATABASE seckill;

USE seckill;

CREATE TABLE seckill(
  seckill_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  name varchar(120) NOT NULL COMMENT '商品名称',
  number int NOT NULL COMMENT '库存数量',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  start_time DATETIME NOT NULL COMMENT '秒杀开始时间',
  end_time DATETIME NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';


INSERT  INTO
  seckill(name,number,start_time,end_time)
VALUES
  ('1000元秒杀iphone6s', 100, '2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('500元秒杀ipad2', 200, '2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('300元秒杀小米4', 300, '2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('200元秒杀红米note', 400, '2015-11-01 00:00:00','2015-11-02 00:00:00');


CREATE TABLE success_killid(
  seckill_id bigint NOT NULL COMMENT '秒杀商品id',
  user_phone bigint NOT NULL COMMENT '用户手机号',
  state TINYINT NOT NULL DEFAULT -1 COMMENT '状态标示：-1：无效 0：成功 1：已付款',
  create_time TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY(seckill_id,user_phone),/*联合主键*/
  key idx_create_time(create_time)
)ENGINE=INNODB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

mysql -uroot -p