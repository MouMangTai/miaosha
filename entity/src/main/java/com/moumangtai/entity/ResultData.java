package com.moumangtai.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ResultData<T> implements Serializable {

    private  int code = Code.SUCCESS;
    private String msg;
    private T data;

    public static interface Code{
        int SUCCESS = 0;
        int ERROR = -1;
    }
}
