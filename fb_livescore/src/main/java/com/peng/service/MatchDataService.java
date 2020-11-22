package com.peng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import com.peng.util.HttpClientUtil;
import lombok.extern.java.Log;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@Log
public class MatchDataService {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final LiveDataRepository liveDataRepository;
    private final TaskExecutor spiderTaskExecutor;
    private final Semaphore semaphore = new Semaphore(100);

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

        String totalResponse = HttpClientUtil.doGet(String.format("https://info.sporttery.cn/football/match_result.php?page=%s&search_league=0&start_date=%s&end_date=%s&dan=0",
                1, beginDate, DATE_FORMAT.format(new Date())), "gb2312");
        String total = totalResponse.substring(totalResponse.indexOf("查询结果：有"), totalResponse.indexOf("场赛事符合条件"))
                .replace("查询结果：有<span class=\"u-org\">", "").replace("</span>", "");

        int page = Integer.parseInt(total) / 30 + 1;

        Map<String, SimpleDateFormat> dateFormatMap = new HashMap<>();


        for (; page > 0; page--) {
            semaphore.acquire();

            int finalPage = page;
            spiderTaskExecutor.execute(() -> {


                String key = Thread.currentThread().getName();
                SimpleDateFormat dateFormat = dateFormatMap.get(key);
                if (dateFormat == null) {
                    dateFormatMap.put(key, new SimpleDateFormat("yyyy-MM-dd"));
                    dateFormat = dateFormatMap.get(key);
                }


                try {
                    String response = HttpClientUtil.doGet(String.format("https://info.sporttery.cn/football/match_result.php?page=%s&search_league=0&start_date=%s&end_date=%s&dan=0",
                            finalPage, beginDate, dateFormat.format(new Date())), "gb2312");

                    String matchListData = response.substring(response.indexOf("<div class=\"match_list\">"), response.indexOf("<div class=\"m-notice\">"));
                    String[] matchData = matchListData.split("</tr>");

                    for (String tr : matchData) {
                        String[] tds = tr.split("\r\n|>VS<");
                        StringBuilder tdData = new StringBuilder();
                        for (String td : tds) {
                            if (td.contains("class=\"u-detal\"")) {
                                break;
                            }
                            if (!td.contains("<td") && !td.contains("</td>")) {
                                continue;
                            }
                            Pattern pattern = Pattern.compile(">.*?</");
                            Matcher matcher = pattern.matcher(td);
                            if (!matcher.find()) {
                                continue;
                            }
                            String text = matcher.group(0).replace(">", " ").replace("</", "");
                            if (text.contains(" ")) {
                                text = text.substring(text.lastIndexOf(" ")).replaceAll("title=\"|class=\"blue\"|font-size:13px;\"|class=", "")
                                        .split("\"")[0];
                                if (text.length() == 0) {
                                    continue;
                                }
                                if (text.trim().equals("--")) {
                                    text = "0";
                                }
                            }
                            tdData.append(text.trim()).append(",");
                        }
                        if (tdData.length() < 10) {
                            continue;
                        }

                        MatchBean insertMatchBean = transHistory(dateFormat, tdData.toString());

                        MatchBean matchBeanDB = liveDataRepository.findFirstByMatchNumAndLiveDate(insertMatchBean.getMatchNum(), insertMatchBean.getLiveDate());
                        if (matchBeanDB != null) {
                            insertMatchBean.setId(matchBeanDB.getId());
                        }
                        liveDataRepository.saveAndFlush(insertMatchBean);
                    }
                    log.info("已经抓取完成第" + finalPage + "页");

                } finally {
                    semaphore.release();
                }
            });
        }
    }

    /**
     * 转换成matchBean
     *
     * @param dateFormat
     * @param tdData
     * @return
     */
    private MatchBean transHistory(SimpleDateFormat dateFormat, String tdData) {
        Calendar calendar = Calendar.getInstance();

        MatchBean matchBean = new MatchBean();
        String[] tdDataArr = tdData.split(",");

        matchBean.setWeekNum(tdDataArr[1].substring(0, 2));
        matchBean.setMatchNum(tdDataArr[1].substring(2));
        try {
            Date liveDate = dateFormat.parse(tdDataArr[0]);
            calendar.setTime(liveDate);
            int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            //如果赛事编号的星期与实际日期的星期不一致，修改日期
            if (!matchBean.getWeekNum().contains(Constants.WEEK_DAYS.get(w))) {
                calendar.add(Calendar.DAY_OF_WEEK, DateUtil.calculateDateOffset(Constants.WEEK_DAYS.indexOf(matchBean.getWeekNum()), w));
            }
            liveDate = calendar.getTime();
            matchBean.setLiveDate(dateFormat.format(liveDate));

        } catch (ParseException e) {
            e.printStackTrace();
            matchBean.setMatchGroup(tdDataArr[0]);
        }
        matchBean.setMatchGroup(tdDataArr[2]);
        matchBean.setHostTeam(tdDataArr[3]);
        matchBean.setGuestTeam(tdDataArr[4]);

        Float[] odds = new Float[]{Float.valueOf(tdDataArr[7]), Float.valueOf(tdDataArr[8]), Float.valueOf(tdDataArr[9])};
        matchBean.setOdds(odds);
        String status = tdDataArr[10].equals("已完成") ? MatchStatus.FINISHED : MatchStatus.PLAYING;

        try {
            if (tdDataArr[10].equals("取消") || tdDataArr[6].equals("无效场次") || tdDataArr[6].equals("取消")) {
                status = MatchStatus.CANCELLED;
            } else {
                if (tdDataArr[6].length() > 0) {
                    matchBean.setHostNum(Integer.parseInt(tdDataArr[6].split(":")[0]));
                    matchBean.setGuestNum(Integer.parseInt(tdDataArr[6].split(":")[1]));
                }

                if (tdDataArr[5].length() > 0) {
                    matchBean.setHalfHostNum(Integer.parseInt(tdDataArr[5].split(":")[0]));
                    matchBean.setHalfGuestNum(Integer.parseInt(tdDataArr[5].split(":")[1]));
                }
            }
        } catch (NumberFormatException e) {
            status = MatchStatus.CANCELLED;
            matchBean.setHostNum(0);
            matchBean.setGuestNum(0);
        }

        matchBean.setStatus(status);
        return matchBean;
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
