package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;

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
            matchCascadeBeans = new ArrayList<>();
            MatchCascadeBean matchCascadeBean;
            for (int i = 2; i <= 300; i++) {

                matchCascadeBean = MatchCascadeRepository.findByLiveDateAndCascadeNum(lastDate, formatMatchNum(i - 1) + "串" + formatMatchNum(i));

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
                        matchCascadeBean.setSs(matchCascadeBean.getSs() + 1);
                        matchCascadeBean.setSp(matchCascadeBean.getSp() + 1);
                        matchCascadeBean.setSf(matchCascadeBean.getSf() + 1);
                        matchCascadeBean.setPs(matchCascadeBean.getPs() + 1);
                        matchCascadeBean.setPp(matchCascadeBean.getPp() + 1);
                        matchCascadeBean.setPf(matchCascadeBean.getPf() + 1);
                        matchCascadeBean.setFs(matchCascadeBean.getFs() + 1);
                        matchCascadeBean.setFp(matchCascadeBean.getFp() + 1);
                        matchCascadeBean.setFf(matchCascadeBean.getFf() + 1);
                        if (pre.getHostNum() - pre.getGuestNum() > 0 && cur.getHostNum() - cur.getGuestNum() > 0) {
                            matchCascadeBean.setSs(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() > 0 && cur.getHostNum() - cur.getGuestNum() == 0) {
                            matchCascadeBean.setSp(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() > 0 && cur.getHostNum() - cur.getGuestNum() < 0) {
                            matchCascadeBean.setSf(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() == 0 && cur.getHostNum() - cur.getGuestNum() > 0) {
                            matchCascadeBean.setPs(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() == 0 && cur.getHostNum() - cur.getGuestNum() == 0) {
                            matchCascadeBean.setPp(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() == 0 && cur.getHostNum() - cur.getGuestNum() < 0) {
                            matchCascadeBean.setPf(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() < 0 && cur.getHostNum() - cur.getGuestNum() > 0) {
                            matchCascadeBean.setFs(0);
                        } else if (pre.getHostNum() - pre.getGuestNum() < 0 && cur.getHostNum() - cur.getGuestNum() == 0) {
                            matchCascadeBean.setFp(0);
                        } else {
                            matchCascadeBean.setFf(0);
                        }
                        matchCascadeBeans.add(matchCascadeBean);
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
