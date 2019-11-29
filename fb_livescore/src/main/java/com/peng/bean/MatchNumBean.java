package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchNumBean {
    private Date liveDate;
    private String matchNum;
    private int zero;
    private int one;
    private int two;
    private int three;
    private int four;
    private int five;
    private int six;
    private int seven;
    private int oneThree;
    private int twoFour;
    private int five_;
    private int oneTwo;
    private int twoThree;
    private int threeFour;


    public MatchNumBean(ResultSet rs) throws SQLException {
        this.setLiveDate(rs.getDate("live_date"));
        this.setMatchNum(rs.getString("match_num"));
        this.setZero(rs.getInt("zero"));
        this.setOne(rs.getInt("one"));
        this.setTwo(rs.getInt("two"));
        this.setThree(rs.getInt("three"));
        this.setFour(rs.getInt("four"));
        this.setFive(rs.getInt("five"));
        this.setSix(rs.getInt("six"));
        this.setSeven(rs.getInt("seven"));
        this.setOneThree(rs.getInt("one_three"));
        this.setTwoFour(rs.getInt("two_four"));
        this.setFive_(rs.getInt("five_"));
        this.setOneTwo(rs.getInt("one_two"));
        this.setTwoThree(rs.getInt("two_three"));
        this.setThreeFour(rs.getInt("three_four"));
    }


}
