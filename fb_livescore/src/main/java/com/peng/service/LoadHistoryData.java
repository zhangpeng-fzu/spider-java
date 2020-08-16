package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import com.peng.util.HttpClientUtil;
import lombok.extern.java.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class LoadHistoryData {


    public static void loadHistoryData() {

        String lastDate = LiveDataRepository.clearLastThreeDayData();

        String response = HttpClientUtil.doGet(String.format("https://info.sporttery.cn/football/match_result.php?page=%s&search_league=0&start_date=%s&end_date=%s&dan=0",
                1, lastDate, DateUtil.getDateFormat().format(new Date())), "gb2312");
        String total = response.substring(response.indexOf("查询结果：有"), response.indexOf("场赛事符合条件"))
                .replace("查询结果：有<span class=\"u-org\">", "").replace("</span>", "");

        int page = Integer.parseInt(total) / 30 + 1;

        for (; page > 0; page--) {
            response = HttpClientUtil.doGet(String.format("https://info.sporttery.cn/football/match_result.php?page=%s&search_league=0&start_date=%s&end_date=%s&dan=0",
                    page, lastDate, DateUtil.getDateFormat().format(new Date())), "gb2312");

            String matchListData = response.substring(response.indexOf("<div class=\"match_list\">"), response.indexOf("<div class=\"m-notice\">"));
            String[] matchData = matchListData.split("</tr>");

            for (String tr : matchData) {
                String[] tds = tr.split("\r\n|>VS<");
                StringBuilder tdData = new StringBuilder();
                for (String td : tds) {
                    if (td.contains("class=\"u-detal\"")) {
                        break;
                    }
                    if (td.contains("<td") || td.contains("</td>")) {
                        Pattern pattern = Pattern.compile(">.*?</");
                        Matcher matcher = pattern.matcher(td);
                        if (matcher.find()) {
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
                    }
                }
                if (tdData.length() < 10) {
                    continue;
                }

                LiveDataRepository.insert(transMatchBean(tdData.toString()));
            }

            System.out.println("正在抓取第" + page + "页");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换成matchBean
     *
     * @param tdData
     * @return
     */
    private static MatchBean transMatchBean(String tdData) {
        Calendar calendar = Calendar.getInstance();

        MatchBean matchBean = new MatchBean();
        String[] tdDataArr = tdData.split(",");

        matchBean.setMatchNum(tdDataArr[1]);
        try {
            Date liveDate = DateUtil.getDateFormat().parse(tdDataArr[0]);
            calendar.setTime(liveDate);
            int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            //如果赛事编号的星期与实际日期的星期不一致，修改日期
            if (!matchBean.getMatchNum().contains(Constants.WEEK_DAYS.get(w))) {
                calendar.add(Calendar.DAY_OF_WEEK, DateUtil.calculateDateOffset(Constants.WEEK_DAYS.indexOf(matchBean.getMatchNum().substring(0, 2)), w));
            }
            liveDate = calendar.getTime();
            matchBean.setLiveDate(DateUtil.getDateFormat().format(liveDate));

        } catch (ParseException e) {
            e.printStackTrace();
            matchBean.setGroupName(tdDataArr[0]);
        }
        matchBean.setGroupName(tdDataArr[2]);
        matchBean.setHostTeam(tdDataArr[3]);
        matchBean.setGuestTeam(tdDataArr[4]);

        Float[] odds = new Float[]{Float.valueOf(tdDataArr[7]), Float.valueOf(tdDataArr[8]), Float.valueOf(tdDataArr[9])};
        matchBean.setOdds(odds);
        String status = tdDataArr[10].equals("已完成") ? Constants.FINISHED : Constants.PLAYING;

        try {
            if (tdDataArr[10].equals("取消") || tdDataArr[6].equals("无效场次") || tdDataArr[6].equals("取消")) {
                status = Constants.CANCELLED;
                matchBean.setHostNum(0);
                matchBean.setGuestNum(0);
            } else {
                if (tdDataArr[6].length() > 0) {
                    matchBean.setHostNum(Integer.parseInt(tdDataArr[6].split(":")[0]));
                    matchBean.setGuestNum(Integer.parseInt(tdDataArr[6].split(":")[1]));
                } else {
                    matchBean.setHostNum(0);
                    matchBean.setGuestNum(0);
                }
            }
        } catch (NumberFormatException e) {
            status = Constants.CANCELLED;
            matchBean.setHostNum(0);
            matchBean.setGuestNum(0);
        }

        matchBean.setStatus(status);
        return matchBean;
    }
}
