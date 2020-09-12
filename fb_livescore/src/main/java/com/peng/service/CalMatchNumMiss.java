package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchNumBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;
import com.peng.util.MyUtil;
import lombok.extern.java.Log;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

@Log
public class CalMatchNumMiss {

    /**
     * 计算进球数据
     *
     * @throws ParseException
     */
    public static void calculate() throws ParseException {
        Date lastDate = MatchNumRepository.clearLastThreeDayData();
        lastDate = lastDate == null ? DateUtil.getDateFormat(3).parse("2010-01-01") : lastDate;

        Date maxDate = LiveDataRepository.getMaxLiveDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);

        while (lastDate.before(new Date())) {
            calculateMissByDate(lastDate, maxDate);
            calendar.add(Calendar.DATE, 1);
            lastDate = calendar.getTime();
        }
        System.out.println("计算赛事场次数据已完成");
    }

    /**
     * 计算x日所有场次的遗漏值
     *
     * @param date
     * @param maxDate
     */
    private static void calculateMissByDate(Date date, Date maxDate) {
        Map<String, MatchBean> matchBeans;
        List<MatchNumBean> matchNumBeans;
        //获取当天所有的赛事
        matchBeans = LiveDataRepository.getMatchList(date);

        //如果没有一场赛事，可能没有抓取数据 并且数据库最大数据日期小于当前日期时
        if (matchBeans.size() == 0 && maxDate.before(date)) {
            return;
        }
        System.out.println(String.format("正在计算%s的进球数据", DateUtil.getDateFormat().format(date)));

        matchNumBeans = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            calculateMissByDateAndNum(date, MyUtil.formatMatchNum(i), matchBeans.get(MyUtil.formatMatchNum(i)), matchNumBeans);
        }
        try {
            MatchNumRepository.insert(matchNumBeans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        matchBeans = null;
        matchNumBeans = null;
    }

    /**
     * 计算x日xx场次的遗漏值
     *
     * @param date
     * @param matchNum
     * @param matchBean
     * @param matchNumBeans
     */
    private static void calculateMissByDateAndNum(Date date, String matchNum, MatchBean matchBean, List<MatchNumBean> matchNumBeans) {
        //获取昨天的数据
        MatchNumBean matchNumBean = MatchNumRepository.findByLiveDateAndNum(date, matchNum);
        matchNumBean.setLiveDate(date);
        matchNumBean.setMatchNum(matchNum);

        if (matchBean == null || !matchBean.getStatus().equals(Constants.FINISHED)) {
            matchNumBeans.add(matchNumBean);
            return;
        }
        //如果有比赛，未中加1，中改0
        String[] fields = Constants.MATCH_NUM_FIELD_ARR;
        for (int i1 = 0; i1 < fields.length; i1++) {
            String filedName = fields[i1];
            //zero重复，第一个zero不处理
            if (i1 == 0) {
                continue;
            }
            try {
                Field field = MatchNumBean.class.getDeclaredField(filedName);
                field.setAccessible(true);
                field.set(matchNumBean, Integer.parseInt(String.valueOf(field.get(matchNumBean))) + 1);
                if (matchBean.getNum() <= 10 && filedName.toLowerCase().contains(Constants.EN_NUM[matchBean.getNum()])) {
                    field.set(matchNumBean, 0);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (matchBean.getNum() >= 5) {
            matchNumBean.setFive_(0);
        }
        if (matchBean.getNum() >= 7) {
            matchNumBean.setSeven(0);
        }
        matchNumBeans.add(matchNumBean);
    }


    public static void main(String[] args) {
        try {
            calculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
