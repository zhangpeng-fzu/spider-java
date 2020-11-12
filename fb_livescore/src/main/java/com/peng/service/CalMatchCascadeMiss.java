package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;
import com.peng.util.MyUtil;
import lombok.extern.java.Log;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Log
public class CalMatchCascadeMiss {

    /**
     * 计算串关数据
     *
     * @throws ParseException
     */
    public static void calculate() throws ParseException {
        //清除最近三天的数据，避免脏数据影响
        Date lastDate = MatchCascadeRepository.clearLastThreeDayData();
        lastDate = lastDate == null ? DateUtil.getDateFormat(2).parse("2018-01-01") : lastDate;
        Date maxDate = LiveDataRepository.getMaxLiveDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);

        while (lastDate.before(new Date())) {
            calculateByDate(lastDate, maxDate);
            calendar.add(Calendar.DATE, 1);
            lastDate = calendar.getTime();
        }
        System.out.println("计算串关场次数据已完成");
    }

    private static void calculateByDate(Date date, Date maxDate) {
        //获取当天所有的赛事
        Map<String, MatchBean> matchBeanMap = LiveDataRepository.getMatchMap(date);

        //如果没有一场赛事，可能没有抓取数据 并且数据库最大数据日期小于当前日期时
        if (matchBeanMap.size() == 0 && maxDate.before(date)) {
            return;
        }
        System.out.println(String.format("正在计算%s的串关数据", DateUtil.getDateFormat().format(date)));
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        Map<String, MatchCascadeBean> cascadeBeanMap = MatchCascadeRepository.findLatestCascadeData(date).stream().collect(Collectors.toMap(MatchCascadeBean::getMatchCascadeNum, matchCascadeBean -> matchCascadeBean));

        for (int i = 2; i <= 300; i++) {
            calculateByDateAndNum(date, i, matchBeanMap, cascadeBeanMap, matchCascadeBeans);
        }
        try {
            MatchCascadeRepository.insert(matchCascadeBeans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void calculateByDateAndNum(Date date, int num, Map<String, MatchBean> matchBeans, Map<String, MatchCascadeBean> cascadeBeanMap, List<MatchCascadeBean> matchCascadeBeans) {
        String preNum = MyUtil.formatMatchNum(num - 1);

        for (int i = num; i <= 300; i++) {

            String curNum = MyUtil.formatMatchNum(i);

            //获取昨天的记录
            String matchCascadeNum = preNum + "串" + curNum;
            MatchCascadeBean matchCascadeBean = cascadeBeanMap.getOrDefault(matchCascadeNum, new MatchCascadeBean());
            matchCascadeBean.setLiveDate(date);
            matchCascadeBean.setMatchCascadeNum(matchCascadeNum);

            //只有i和i-1有一个场次不存在，直接使用昨天的
            if (!matchBeans.containsKey(preNum) || !matchBeans.containsKey(curNum)) {
                continue;
            }

            MatchBean pre = matchBeans.get(preNum);
            MatchBean cur = matchBeans.get(curNum);

            //计算赔率
            List<Float> odds = new ArrayList<>();
            for (int j = 0; j < pre.getOdds().length; j++) {
                for (int k = 0; k < cur.getOdds().length; k++) {
                    odds.add(pre.getOdds()[j] * cur.getOdds()[k]);
                }
            }
            matchCascadeBean.setOdds(Arrays.toString(new List[]{odds}));
            //如果i和i-1有一场未完成，使用昨天的数据
            if (!matchBeans.get(preNum).getStatus().equals(MatchStatus.FINISHED) ||
                    !matchBeans.get(curNum).getStatus().equals(MatchStatus.FINISHED)) {
                continue;
            }
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
            matchCascadeBeans.add(matchCascadeBean);
        }
    }


    public static void main(String[] args) {
        try {
            calculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
