package com.peng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peng.bean.MatchBean;
import com.peng.bean.MatchResponse;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import com.peng.util.HttpClientUtil;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public class MatchDataService {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final LiveDataRepository liveDataRepository;
    private final TaskExecutor spiderTaskExecutor;
    private final Semaphore semaphore = new Semaphore(20);

    public MatchDataService(LiveDataRepository liveDataRepository, TaskExecutor spiderTaskExecutor) {
        this.liveDataRepository = liveDataRepository;
        this.spiderTaskExecutor = spiderTaskExecutor;
    }

    /**
     * 查询并解析今日数据
     *
     * @throws ParseException
     */
    public void syncTodayMatch() throws ParseException {
        String queryData = HttpClientUtil
                .doGet("https://i.sporttery.cn/odds_calculator/get_odds?i_format=json&i_callback=getData&poolcode[]=hhad&poolcode[]=had&_" + System.currentTimeMillis(),
                        StandardCharsets.UTF_8.displayName()).replace("getData(", "").replace(");", "");

        JSONObject matchList = JSON.parseObject(queryData).getJSONObject("data");
        for (String key : matchList.keySet()) {
            JSONObject match = (JSONObject) matchList.get(key);
            MatchBean insertMatchBean = transToday(match);
            if (insertMatchBean == null) {
                continue;
            }

            MatchBean matchBeanDB = liveDataRepository.findFirstByMatchNumAndLiveDate(insertMatchBean.getMatchNum(), insertMatchBean.getLiveDate());
            if (matchBeanDB != null) {
                insertMatchBean.setId(matchBeanDB.getId());
            }
            liveDataRepository.saveAndFlush(insertMatchBean);
        }

        Constants.MATCH_CACHE_MAP = liveDataRepository.findAll().stream().collect(Collectors.groupingBy(MatchBean::getMatchNum));

    }


    /**
     * 查询并解析历史数据
     *
     * @throws ParseException
     */
    public void loadHistoryMatch() throws ParseException, InterruptedException {

        MatchBean matchBean = liveDataRepository.findFirstByOrderByLiveDateDesc();

        String beginDateStr = matchBean == null ? "2010-01-01" : matchBean.getLiveDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT.parse(beginDateStr));
        //需覆盖前两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值
        calendar.add(Calendar.DATE, -5);

        //清除最近两天的数据，防止定时同步导致的最近一次抓取日期异常
        liveDataRepository.deleteByLiveDateGreaterThanEqual(DATE_FORMAT.format(calendar.getTime()));

        matchBean = liveDataRepository.findFirstByOrderByLiveDateDesc();
        String beginDate = matchBean == null ? "2010-01-01" : matchBean.getLiveDate();

        calendar.setTime(DATE_FORMAT.parse(beginDate));
        //加三个月

        calendar.add(Calendar.MONTH, 2);

        Date today = new Date();

        String endDate = calendar.getTime().after(today) ? DATE_FORMAT.format(today) : DATE_FORMAT.format(calendar.getTime());

        boolean isContinue = true;

        Map<String, SimpleDateFormat> dateFormatMap = new HashMap<>(20);
        while (isContinue) {
            //加锁，方式过多请求到服务端
            semaphore.acquire();
            String finalBeginDate = beginDate;
            String finalEndDate = endDate;
            spiderTaskExecutor.execute(() -> {
                try {
                    String key = Thread.currentThread().getName();
                    SimpleDateFormat dateFormat = dateFormatMap.get(key);
                    if (dateFormat == null) {
                        dateFormatMap.put(key, new SimpleDateFormat("yyyy-MM-dd"));
                        dateFormat = dateFormatMap.get(key);
                    }
                    queryHistoryByDate(finalBeginDate, finalEndDate, dateFormat);
                } finally {
                    semaphore.release();
                }
            });
            //更新开始和结束时间
            beginDate = endDate;
            try {
                calendar.setTime(DATE_FORMAT.parse(endDate));
            } catch (ParseException ignore) {
            }
            calendar.add(Calendar.MONTH, 2);
            endDate = calendar.getTime().after(today) ? DATE_FORMAT.format(today) : DATE_FORMAT.format(calendar.getTime());
            if (beginDate.equals(DATE_FORMAT.format(today))) {
                isContinue = false;
            }
        }
    }


    private void queryHistoryByDate(String beginDate, String endDate, SimpleDateFormat dateFormat) {
        log.info("正在抓取数据，开始时间：{}，结束时间：{}", beginDate, endDate);
        String response = HttpClientUtil.doGet(String.format("https://webapi.sporttery.cn/gateway/jc/football/getMatchResultV1.qry?matchPage=1&matchBeginDate=%s&matchEndDate=%s&pageNo=%s&isFix=0&pcOrWap=1",
                beginDate, endDate, 1), "utf-8");

        MatchResponse matchResponse = JSONObject.parseObject(response, MatchResponse.class);

        int page = matchResponse.getValue().getPages();
        if (page > 0) {

            saveMatchHistory(new SimpleDateFormat("yyyy-MM-dd"), matchResponse.getValue().getMatchResult());
            log.info("开始时间：{}，结束时间：{},已经抓取完成第1页", beginDate, endDate);
            for (int i = 2; i <= page; i++) {
                response = HttpClientUtil.doGet(String.format("https://webapi.sporttery.cn/gateway/jc/football/getMatchResultV1.qry?matchPage=1&matchBeginDate=%s&matchEndDate=%s&pageNo=%s&isFix=0&pcOrWap=1",
                        beginDate, endDate, i), "utf-8");
                matchResponse = JSONObject.parseObject(response, MatchResponse.class);
                saveMatchHistory(dateFormat, matchResponse.getValue().getMatchResult());
                log.info("开始时间：{}，结束时间：{},已经抓取完成第" + page + "页", beginDate, endDate);
            }
        }
    }


    /**
     * 转换成matchBean
     *
     * @param dateFormat
     * @param matchResultBeans
     */
    private void saveMatchHistory(SimpleDateFormat dateFormat, List<MatchResponse.ValueBean.MatchResultBean> matchResultBeans) {
        Calendar calendar = Calendar.getInstance();

        matchResultBeans.forEach(matchResultBean -> {
            MatchBean matchBean = new MatchBean();

            matchBean.setWeekNum(matchResultBean.getMatchNumStr().substring(0, 2));
            matchBean.setMatchNum(matchResultBean.getMatchNumStr().substring(2));
            try {
                Date liveDate = dateFormat.parse(matchResultBean.getMatchDate());
                calendar.setTime(liveDate);
                int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                //如果赛事编号的星期与实际日期的星期不一致，修改日期
                if (!matchBean.getWeekNum().contains(Constants.WEEK_DAYS.get(w))) {
                    calendar.add(Calendar.DAY_OF_WEEK, DateUtil.calculateDateOffset(Constants.WEEK_DAYS.indexOf(matchBean.getWeekNum()), w));
                }
                liveDate = calendar.getTime();
                matchBean.setLiveDate(dateFormat.format(liveDate));

            } catch (ParseException ignore) {
            }
            matchBean.setMatchGroup(matchResultBean.getLeagueName());
            matchBean.setHostTeam(matchResultBean.getHomeTeam());
            matchBean.setGuestTeam(matchResultBean.getAwayTeam());


            Float[] odds = new Float[]{0F,0F,0F};
            try {
                odds = new Float[]{Float.valueOf(matchResultBean.getH()), Float.valueOf(matchResultBean.getD()), Float.valueOf(matchResultBean.getA())};
            } catch (NumberFormatException ignore) {
//                log.error("获取赔率失败！",ignore);
            }
            matchBean.setOdds(odds);
            String status = "2".equals(matchResultBean.getMatchResultStatus()) ? MatchStatus.FINISHED : MatchStatus.PLAYING;

            try {
                if ("0".equals(matchResultBean.getMatchResultStatus()) || "无效场次".equals(matchResultBean.getSectionsNo999())
                        || "Refund".equals(matchResultBean.getPoolStatus()) || "OddsIn".equals(matchResultBean.getPoolStatus())) {
                    status = MatchStatus.CANCELLED;
                    matchBean.setHostNum(0);
                    matchBean.setGuestNum(0);
                } else {
                    //全场比分
                    String sectionsNo999 = matchResultBean.getSectionsNo999();
                    if (StringUtils.isNotBlank(sectionsNo999)) {
                        matchBean.setHostNum(Integer.parseInt(sectionsNo999.split(":")[0]));
                        matchBean.setGuestNum(Integer.parseInt(sectionsNo999.split(":")[1]));
                    }

                    //半场比分
                    String sectionsNo1 = matchResultBean.getSectionsNo1();
                    if (StringUtils.isNotBlank(sectionsNo1)) {
                        matchBean.setHalfHostNum(Integer.parseInt(sectionsNo1.split(":")[0]));
                        matchBean.setHalfGuestNum(Integer.parseInt(sectionsNo1.split(":")[1]));
                    }
                }
            } catch (NumberFormatException e) {
                log.error(JSON.toJSONString(matchResultBean), e);
                status = MatchStatus.CANCELLED;
                matchBean.setHostNum(0);
                matchBean.setGuestNum(0);
            }

            matchBean.setStatus(status);
            MatchBean matchBeanDB = liveDataRepository.findFirstByMatchNumAndLiveDate(matchBean.getMatchNum(), matchBean.getLiveDate());
            if (matchBeanDB != null) {
                Integer matchId = matchBeanDB.getId();
                BeanUtils.copyProperties(matchBean, matchBeanDB);
                matchBeanDB.setId(matchId);
            } else {
                matchBeanDB = matchBean;
            }
            liveDataRepository.saveAndFlush(matchBeanDB);
        });

    }

    /**
     * 转换数据
     *
     * @param match
     * @return
     * @throws ParseException
     */
    public MatchBean transToday(JSONObject match) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        MatchBean matchBean = new MatchBean();
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
        if ("Played".equals(matchBean.getStatus())) {
            matchBean.setHostNum(match.getInteger("fs_h"));
            matchBean.setGuestNum(match.getInteger("fs_a"));
            matchBean.setStatus("1");
        } else if ("Playing".equals(matchBean.getStatus())) {
            matchBean.setHostNum(0);
            matchBean.setGuestNum(0);
            matchBean.setStatus(match.getString("minute") + "分");
        } else {
            matchBean.setHostNum(0);
            matchBean.setGuestNum(0);
            matchBean.setStatus("0");
        }
        matchBean.setWeekNum(match.getString("num").substring(0, 2));
        matchBean.setMatchNum(match.getString("num").substring(2));

        Date liveDate = DATE_FORMAT.parse(matchBean.getLiveDate());
        calendar.setTime(liveDate);

        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        //如果赛事编号的星期与实际日期的星期不一致，修改日期
        if (!matchBean.getWeekNum().contains(Constants.WEEK_DAYS.get(w))) {
            int c = Constants.WEEK_DAYS.indexOf(matchBean.getWeekNum());
            int offset = DateUtil.calculateDateOffset(c, w);
            calendar.add(Calendar.DAY_OF_WEEK, offset);
        }
        liveDate = calendar.getTime();
        //超过今天的数据
        if (liveDate.after(DateUtil.getTomorrow())) {
            return null;
        }
        matchBean.setLiveDate(DATE_FORMAT.format(liveDate));

        return matchBean;
    }
}
