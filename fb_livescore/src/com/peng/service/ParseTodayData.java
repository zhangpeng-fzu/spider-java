package com.peng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peng.bean.MatchBean;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import com.peng.util.HttpClientUtil;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ParseTodayData {
    static String queryToday() {

        return HttpClientUtil
                .doGet("https://i.sporttery.cn/odds_calculator/get_odds?i_format=json&i_callback=getData&poolcode[]=hhad&poolcode[]=had&_" + "1573876793274", StandardCharsets.UTF_8.displayName());

    }

    public static void getMatchData() throws ParseException {

        LiveDataRepository.delete(new Date());

        List<String> weekDays = Arrays.asList("周日", "周一", "周二", "周三", "周四", "周五", "周六");
        String queryData = queryToday().replace("getData(", "").replace(");", "");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date tomorrow = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        JSONObject matchList = JSON.parseObject(queryData).getJSONObject("data");
        for (String key : matchList.keySet()) {

            JSONObject match = (JSONObject) matchList.get(key);

            //超过今天的数据
            if (DateUtil.getDateFormat().parse(String.valueOf(match.get("date"))).after(tomorrow)) {
                continue;
            }

            MatchBean matchBean = new MatchBean();
            matchBean.setMatchNum(match.getString("num"));
            matchBean.setLiveDate(match.getString("date"));
            matchBean.setGroupName(match.getString("l_cn_abbr"));
            matchBean.setStatus(match.getString("status"));
            matchBean.setHostTeam(match.getString("h_cn"));
            matchBean.setGuestTeam(match.getString("a_cn"));
            if (match.getJSONObject("had") != null) {
                matchBean.setOdds(new Float[]{match.getJSONObject("had").getFloat("h"), match.getJSONObject("had").getFloat("d"), match.getJSONObject("had").getFloat("a")});
            } else {
                matchBean.setOdds(new Float[]{0F, 0F, 0F});

            }
            if (matchBean.getStatus().equals("Played")) {
                matchBean.setHostNum(match.getInteger("fs_h"));
                matchBean.setGuestNum(match.getInteger("fs_a"));
                matchBean.setStatus("1");
            } else if (matchBean.getStatus().equals("Playing")) {
                matchBean.setHostNum(0);
                matchBean.setGuestNum(0);
                matchBean.setStatus(match.getString("minute") + "分");
            } else {
                matchBean.setHostNum(0);
                matchBean.setGuestNum(0);
                matchBean.setStatus("0");
            }

            Date liveDate = DateUtil.getDateFormat().parse(matchBean.getLiveDate());
            calendar.setTime(liveDate);
            int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            //如果赛事编号的星期与实际日期的星期不一致，修改日期
            if (!matchBean.getMatchNum().contains(weekDays.get(w))) {
                int c = weekDays.indexOf(matchBean.getMatchNum().substring(0, 2));
                int offset = c - w;
                if (c == 6 && w == 0) {
                    offset = -1;
                }
                if (c == 0 && w == 6) {
                    offset = 1;
                }
                if (c == 6 && w == 1) {
                    offset = -2;
                }
                if (c == 1 && w == 6) {
                    offset = 2;
                }
                if (c == 0 && w == 2) {
                    offset = -2;
                }
                if (c == 2 && w == 0) {
                    offset = 2;
                }
                if (c == 5 && w == 0) {
                    offset = -2;
                }
                if (c == 0 && w == 5) {
                    offset = 2;
                }
                calendar.add(Calendar.DAY_OF_WEEK, offset);
            }
            liveDate = calendar.getTime();
            matchBean.setLiveDate(DateUtil.getDateFormat().format(liveDate));

            LiveDataRepository.insert(matchBean);
        }
    }

    public static void main(String[] args) {
        try {
            getMatchData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
