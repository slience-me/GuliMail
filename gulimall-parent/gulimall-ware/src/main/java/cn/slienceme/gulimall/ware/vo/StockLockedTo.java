package cn.slienceme.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockLockedTo implements Serializable {
    private Long id;
    private StockDetailTo detailTo;
}
