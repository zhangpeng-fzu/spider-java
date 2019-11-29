package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;
import com.peng.util.MyUtil;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class CalMatchCascadeMiss {

    /**
     * 计算串关数据
     *
     * @throws ParseException
     */
    public static void calculate() throws ParseException {
        //清除最近三天的数据，避免脏数据营销
        Date lastDate = MatchCascadeRepository.clearLastThreeDayData();
        if (lastDate == null) {
            lastDate = DateUtil.getDateFormat(2).parse("2019-01-01");
        }
        Date maxDate = LiveDataRepository.getMaxLiveDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);

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
                matchCascadeBean = MatchCascadeRepository.findByLiveDateAndCascadeNum(lastDate, MyUtil.formatMatchNum(i - 1) + "串" + MyUtil.formatMatchNum(i));
                matchCascadeBean.setLiveDate(lastDate);
                matchCascadeBean.setMatchCascadeNum(MyUtil.formatMatchNum(i - 1) + "串" + MyUtil.formatMatchNum(i));
                //只有i和i-1有一个场次不存在，直接使用昨天的
                if (!matchBeans.containsKey(MyUtil.formatMatchNum(i - 1)) || !matchBeans.containsKey(MyUtil.formatMatchNum(i))) {
                    matchCascadeBeans.add(matchCascadeBean);
                    continue;
                }
                MatchBean pre = matchBeans.get(MyUtil.formatMatchNum(i - 1));
                MatchBean cur = matchBeans.get(MyUtil.formatMatchNum(i));
                //计算赔率
                List<Float> odds = new ArrayList<>();
                for (int j = 0; j < pre.getOdds().length; j++) {
                    for (int k = 0; k < cur.getOdds().length; k++) {
                        odds.add(pre.getOdds()[j] * cur.getOdds()[k]);
                    }
                }
                matchCascadeBean.setOdds(Arrays.toString(new List[]{odds}));
                //如果i和i-1有一场未完成，使用昨天的数据
                if (!matchBeans.get(MyUtil.formatMatchNum(i - 1)).getStatus().equals(Constants.FINISHED) ||
                        !matchBeans.get(MyUtil.formatMatchNum(i)).getStatus().equals(Constants.FINISHED)) {
                    matchCascadeBeans.add(matchCascadeBean);
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

}
