package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCascadeBean {
    private Date liveDate;
    private String matchCascadeNum;
    private int ss;
    private int sp;
    private int sf;
    private int ps;
    private int pp;
    private int pf;
    private int fs;
    private int fp;
    private int ff;
    private String odds;


    public MatchCascadeBean(ResultSet rs) throws SQLException {
        this.setLiveDate(rs.getDate("live_date"));
        this.setMatchCascadeNum(rs.getString("match_cascade_num"));
        this.setSs(rs.getInt("ss"));
        this.setSp(rs.getInt("sp"));
        this.setSf(rs.getInt("sf"));
        this.setPs(rs.getInt("ps"));
        this.setPp(rs.getInt("pp"));
        this.setPf(rs.getInt("pf"));
        this.setFs(rs.getInt("fs"));
        this.setFp(rs.getInt("fp"));
        this.setFf(rs.getInt("ff"));
        this.setOdds(rs.getString("odds"));
    }

}
