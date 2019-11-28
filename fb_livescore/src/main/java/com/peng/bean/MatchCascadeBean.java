package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCascadeBean {
    private Date liveDate;
    private String matchCascadeNum;
    private int[] missValues = new int[9];
    private String odds;

}
