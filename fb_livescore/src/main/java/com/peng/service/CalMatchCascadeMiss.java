package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class CalMatchCascadeMiss {

    public static void calculate() throws ParseException {
        Date lastDate = MatchCascadeRepository.clearLastThreeDayData();
        if (lastDate == null) {
            lastDate = DateUtil.getDateFormat(2).parse("2019-01-01");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);
        calendar.add(Calendar.DATE, 0);
        lastDate = calendar.getTime();
        Date maxDate = LiveDataRepository.getMaxLiveDate();
        Map<String, MatchBean> matchBeans;
        List<MatchCascadeBean> matchCascadeBeans;
        while (lastDate.before(new Date())) {
            //获取当天所有的赛事
            matchBeans = LiveDataRepository.getMatchList(lastDate);

            //如果没有一场赛事，可能没有抓取数据 并且数据库最大数据日期小于当前日期时
            if (matchBeans.size() == 0 && maxDate.before(lastDate)) {
                break;
            }
            System.out.println(String.format("正在计算%s的串关数据", DateUtil.getDateFormat().format(lastDate)));
            matchCascadeBeans = new ArrayList<>();
            MatchCascadeBean matchCascadeBean;
            for (int i = 2; i <= 300; i++) {

                //获取昨天的记录
                calendar.add(Calendar.DATE, -1);
                lastDate = calendar.getTime();
                matchCascadeBean = MatchCascadeRepository.findByLiveDateAndCascadeNum(lastDate, formatMatchNum(i - 1) + "串" + formatMatchNum(i));
                calendar.add(Calendar.DATE, 1);
                lastDate = calendar.getTime();

                matchCascadeBean.setLiveDate(lastDate);
                matchCascadeBean.setMatchCascadeNum(formatMatchNum(i - 1) + "串" + formatMatchNum(i));

                if (matchBeans.containsKey(formatMatchNum(i - 1)) && matchBeans.containsKey(formatMatchNum(i))) {
                    MatchBean pre = matchBeans.get(formatMatchNum(i - 1));
                    MatchBean cur = matchBeans.get(formatMatchNum(i));
                    //计算赔率
                    List<Float> odds = new ArrayList<>();
                    for (int j = 0; j < pre.getOdds().length; j++) {
                        for (int k = 0; k < cur.getOdds().length; k++) {
                            odds.add(pre.getOdds()[j] * cur.getOdds()[k]);
                        }
                    }
                    matchCascadeBean.setOdds(Arrays.toString(new List[]{odds}));
                    if (matchBeans.get(formatMatchNum(i - 1)).getStatus().equals("1") &&
                            matchBeans.get(formatMatchNum(i)).getStatus().equals("1")) {
                        for (int j = 0; j < Constants.MATCH_CASCADE_FIELD_ARR.length; j++) {
                            try {
                                String fieldName = Constants.MATCH_CASCADE_FIELD_ARR[j];
                                Field field = MatchCascadeBean.class.getDeclaredField(fieldName);
                                field.setAccessible(true);
                                field.set(matchCascadeBean, Integer.parseInt(String.valueOf(field.get(matchCascadeBean))) + 1);
                                if ((pre.getResult() + cur.getResult()).equals(fieldName)) {
                                    field.set(matchCascadeBean, 0);
                                }
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                matchCascadeBeans.add(matchCascadeBean);
            }
            try {
                MatchCascadeRepository.insert(matchCascadeBeans);
            } catch (SQLException e) {
                e.printStackTrace();
                break;
            }

            calendar.add(Calendar.DATE, 1);
            lastDate = calendar.getTime();
        }
        System.out.println("计算串关场次数据已完成");
    }

    public static void main(String[] args) {
        try {
            calculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String formatMatchNum(int i) {
        if (i < 10) {
            return "00" + i;
        }
        if (i < 100) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

}
