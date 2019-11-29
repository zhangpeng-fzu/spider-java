package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchNumBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class CalMatchNumMiss {


    public static void calculate() throws ParseException {
        Date lastDate = MatchNumRepository.clearLastThreeDayData();
        if (lastDate == null) {
            lastDate = DateUtil.getDateFormat(3).parse("2019-01-01");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);
        calendar.add(Calendar.DATE, 0);
        lastDate = calendar.getTime();
        Date maxDate = LiveDataRepository.getMaxLiveDate();
        Map<String, MatchBean> matchBeans;
        List<MatchNumBean> matchNumBeans;
        while (lastDate.before(new Date())) {
            //获取当天所有的赛事
            matchBeans = LiveDataRepository.getMatchList(lastDate);
            //如果没有一场赛事，可能没有抓取数据 并且数据库最大数据日期小于当前日期时
            if (matchBeans.size() == 0 && maxDate.before(lastDate)) {
                break;
            }
            System.out.println(String.format("正在计算%s的进球数据", DateUtil.getDateFormat().format(lastDate)));
            matchNumBeans = new ArrayList<>();
            MatchNumBean matchNumBean;
            for (int i = 1; i <= 300; i++) {
                calendar.add(Calendar.DATE, -1);
                lastDate = calendar.getTime();
                matchNumBean = MatchNumRepository.findByLiveDateAndNum(lastDate, formatMatchNum(i));
                calendar.add(Calendar.DATE, 1);
                lastDate = calendar.getTime();
                matchNumBean.setLiveDate(lastDate);
                matchNumBean.setMatchNum(formatMatchNum(i));

                MatchBean matchBean = matchBeans.get(formatMatchNum(i));
                if (matchBean != null && matchBean.getStatus().equals("1")) {
                    //如果有比赛，未中加1，中改0
                    String[] fields = Constants.MATCH_NUM_FIELD_ARR;
                    for (int i1 = 0; i1 < fields.length; i1++) {
                        String filedName = fields[i1];
                        //zero重复，第一个zero不处理
                        if (i1 == 1) {
                            continue;
                        }
                        try {
                            Field field = MatchNumBean.class.getDeclaredField(filedName);
                            field.setAccessible(true);
                            field.set(matchNumBean, Integer.parseInt(String.valueOf(field.get(matchNumBean))) + 1);
                            if (filedName.contains(Constants.EN_NUM[matchBean.getNum()])) {
                                field.set(matchNumBean, 0);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (matchBean.getNum() >= 5) {
                        matchNumBean.setFive_(0);
                    }
                }
                matchNumBeans.add(matchNumBean);
            }
            try {
                MatchNumRepository.insert(matchNumBeans);
            } catch (SQLException e) {
                e.printStackTrace();
                break;
            }
            calendar.add(Calendar.DATE, 1);
            lastDate = calendar.getTime();
        }
        System.out.println("计算赛事场次数据已完成");
    }


    private static String formatMatchNum(int i) {
        if (i < 10) {
            return "00" + i;
        }
        if (i < 100) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

    public static void main(String[] args) {
        try {
            calculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
