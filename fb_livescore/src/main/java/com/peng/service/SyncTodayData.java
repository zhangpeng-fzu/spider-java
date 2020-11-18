package com.peng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import com.peng.util.HttpClientUtil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

@Service
@Transactional
public class SyncTodayData {

    private final LiveDataRepository liveDataRepository;

    public SyncTodayData(LiveDataRepository liveDataRepository) {
        this.liveDataRepository = liveDataRepository;
    }

    /**
     * 转换数据
     *
     * @param match
     * @return
     * @throws ParseException
     */
    public MatchBean transMatchBean(JSONObject match) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        MatchBean matchBean = new MatchBean();
        matchBean.setMatchNum(match.getString("num"));
        matchBean.setLiveDate(match.getString("b_date"));
        matchBean.setMatchGroup(match.getString("l_cn_abbr"));
        matchBean.setStatus(match.getString("status"));
        matchBean.setHostTeam(match.getString("h_cn"));
        matchBean.setGuestTeam(match.getString("a_cn"));

        if (match.getJSONObject("had") != null) {
            JSONObject had = match.getJSONObject("had");
            matchBean.setOdds(new Float[]{had.getFloat("h"), had.getFloat("d"), had.getFloat("a")});
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
        if (!matchBean.getMatchNum().contains(Constants.WEEK_DAYS.get(w))) {
            int c = Constants.WEEK_DAYS.indexOf(matchBean.getMatchNum().substring(0, 2));
            int offset = DateUtil.calculateDateOffset(c, w);
            calendar.add(Calendar.DAY_OF_WEEK, offset);
        }
        liveDate = calendar.getTime();
        //超过今天的数据
        if (liveDate.after(DateUtil.getTomorrow())) {
            return null;
        }
        matchBean.setLiveDate(DateUtil.getDateFormat().format(liveDate));

        return matchBean;
    }

    /**
     * 查询今日数据
     *
     * @return
     */
    String queryToday() {
        return HttpClientUtil
                .doGet("https://i.sporttery.cn/odds_calculator/get_odds?i_format=json&i_callback=getData&poolcode[]=hhad&poolcode[]=had&_" + System.currentTimeMillis(),
                        StandardCharsets.UTF_8.displayName()).replace("getData(", "").replace(");", "");

    }

    /**
     * 解析今日数据
     *
     * @throws ParseException
     */
    public void syncTodayMatch() throws ParseException {
        String queryData = this.queryToday();

        JSONObject matchList = JSON.parseObject(queryData).getJSONObject("data");
        for (String key : matchList.keySet()) {
            JSONObject match = (JSONObject) matchList.get(key);
            MatchBean insertMatchBean = transMatchBean(match);
            if (insertMatchBean == null) {
                continue;
            }

            MatchBean matchBeanDB = liveDataRepository.findFirstByMatchNumAndLiveDate(insertMatchBean.getMatchNum(), insertMatchBean.getLiveDate());
            if (matchBeanDB != null) {
                insertMatchBean.setId(matchBeanDB.getId());
            }
            liveDataRepository.saveAndFlush(insertMatchBean);
        }
    }
}
