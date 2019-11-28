package com.peng.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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


    public MatchNumBean() {
        this.zero = 0;
        this.oneThree = 0;
        this.twoFour = 0;
        this.five_ = 0;
        this.one = 0;
        this.two = 0;
        this.three = 0;
        this.four = 0;
        this.five = 0;
        this.six = 0;
        this.seven = 0;
        this.oneTwo = 0;
        this.twoThree = 0;
        this.threeFour = 0;

    }


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

    public Date getLiveDate() {
        return liveDate;
    }

    public void setLiveDate(Date liveDate) {
        this.liveDate = liveDate;
    }

    public String getMatchNum() {
        return matchNum;
    }

    public void setMatchNum(String matchNum) {
        this.matchNum = matchNum;
    }

    public int getZero() {
        return zero;
    }

    public void setZero(int zero) {
        this.zero = zero;
    }

    public int getOneThree() {
        return oneThree;
    }

    public void setOneThree(int oneThree) {
        this.oneThree = oneThree;
    }

    public int getTwoFour() {
        return twoFour;
    }

    public void setTwoFour(int twoFour) {
        this.twoFour = twoFour;
    }

    public int getFive_() {
        return five_;
    }

    public void setFive_(int five_) {
        this.five_ = five_;
    }

    public int getOne() {
        return one;
    }

    public void setOne(int one) {
        this.one = one;
    }

    public int getTwo() {
        return two;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public int getThree() {
        return three;
    }

    public void setThree(int three) {
        this.three = three;
    }

    public int getFour() {
        return four;
    }

    public void setFour(int four) {
        this.four = four;
    }

    public int getFive() {
        return five;
    }

    public void setFive(int five) {
        this.five = five;
    }

    public int getSix() {
        return six;
    }

    public void setSix(int six) {
        this.six = six;
    }

    public int getSeven() {
        return seven;
    }

    public void setSeven(int seven) {
        this.seven = seven;
    }

    public int getOneTwo() {
        return oneTwo;
    }

    public void setOneTwo(int oneTwo) {
        this.oneTwo = oneTwo;
    }

    public int getTwoThree() {
        return twoThree;
    }

    public void setTwoThree(int twoThree) {
        this.twoThree = twoThree;
    }

    public int getThreeFour() {
        return threeFour;
    }

    public void setThreeFour(int threeFour) {
        this.threeFour = threeFour;
    }
}
