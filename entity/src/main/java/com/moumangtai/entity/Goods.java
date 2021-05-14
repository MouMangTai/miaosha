package com.moumangtai.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Goods implements Serializable {

  private long id;
  private String title;
  private String info;
  private BigDecimal price;
  private long save;
  private java.sql.Timestamp beginTime;
  private java.sql.Timestamp endTime;







  public long getSave() {
    return save;
  }

  public void setSave(long save) {
    this.save = save;
  }


  public java.sql.Timestamp getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(java.sql.Timestamp beginTime) {
    this.beginTime = beginTime;
  }


  public java.sql.Timestamp getEndTime() {
    return endTime;
  }

  public void setEndTime(java.sql.Timestamp endTime) {
    this.endTime = endTime;
  }

}
