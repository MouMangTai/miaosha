package com.moumangtai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Accessors(chain = true)
public class Orders implements Serializable {

  @TableId(type = IdType.AUTO)
  private int id;
  private String oid;
  private int gid;
  private int uid;
  private int gnumber;
  private BigDecimal allPrice;
  private Timestamp createTime;
  private int status;




}
