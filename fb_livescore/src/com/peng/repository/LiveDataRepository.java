package com.peng.repository;

import com.peng.bean.MatchBean;
import com.peng.database.MysqlManager;
import com.peng.util.DateUtil;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

public class LiveDataRepository {

    public static void insert(MatchBean matchBean) {
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConn().prepareStatement("insert into live_data (live_date,match_num,match_group,host_team,guest_team,host_num,guest_num,odds_s,odds_p,odds_f,status) "
                    + "values(?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE match_num=?,host_num=?,guest_num=?,odds_s=?,odds_p=?,odds_f=?,status=?");
            plsql.setDate(1, Date.valueOf(matchBean.getLiveDate()));              //设置参数1，创建id为3212的数据
            plsql.setString(2, matchBean.getMatchNum());      //设置参数2，name 为王刚
            plsql.setString(3, matchBean.getGroupName());
            plsql.setString(4, matchBean.getHostTeam());
            plsql.setString(5, matchBean.getGuestTeam());
            plsql.setInt(6, matchBean.getHostNum());
            plsql.setInt(7, matchBean.getGuestNum());
            plsql.setFloat(8, matchBean.getOdds()[0] != null ? matchBean.getOdds()[0] : 0);
            plsql.setFloat(9, matchBean.getOdds()[1] != null ? matchBean.getOdds()[1] : 0);
            plsql.setFloat(10, matchBean.getOdds()[2] != null ? matchBean.getOdds()[2] : 0);
            plsql.setString(11, matchBean.getStatus());
            plsql.setString(12, matchBean.getMatchNum());
            plsql.setInt(13, matchBean.getHostNum());
            plsql.setInt(14, matchBean.getGuestNum());
            plsql.setFloat(15, matchBean.getOdds()[0] != null ? matchBean.getOdds()[0] : 0);
            plsql.setFloat(16, matchBean.getOdds()[1] != null ? matchBean.getOdds()[1] : 0);
            plsql.setFloat(17, matchBean.getOdds()[2] != null ? matchBean.getOdds()[2] : 0);
            plsql.setString(18, matchBean.getStatus());
            plsql.executeUpdate();
        } catch (Exception se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        }
    }

    public static String clearLastThreeDayData() {
        Date lastDate = Date.valueOf("2019-01-01");
        Calendar calendar = Calendar.getInstance();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConn().prepareStatement("select distinct live_date from live_data order by live_date desc limit 1");
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                lastDate = rs.getDate("live_date");
                calendar.setTime(lastDate);
                //需删除签两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值
                calendar.add(Calendar.DATE, -2);
                plsql = MysqlManager.getConn().prepareStatement("delete from live_data where live_date >= ?");
                plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(calendar.getTime())));
                plsql.execute();
                return DateUtil.getDateFormat().format(calendar.getTime());
            }
        } catch (Exception se) {
            se.printStackTrace();
        }

        return lastDate.toString();
    }

    public static java.util.Date getMaxLiveDate() throws ParseException {
        Date maxDate = Date.valueOf("2019-01-01");
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConn().prepareStatement("select distinct live_date from live_data order by live_date desc limit 1");
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                maxDate = rs.getDate("live_date");
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return DateUtil.getDateFormat().parse(maxDate.toString());
    }

    public static Map<String, MatchBean> getMatchList(java.util.Date date) {
        Map<String, MatchBean> matchBeans = new HashMap<>();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConn().prepareStatement("select * from live_data where live_date = ? and status != 2 order by live_date asc ");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));


            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchBean matchBean = new MatchBean();
                matchBean.setMatchNum(rs.getString("match_num").replaceAll("周[一|二|三|四|五|六|日]", ""));
                matchBean.setHostNum(rs.getInt("host_num"));
                matchBean.setGuestNum(rs.getInt("guest_num"));
                matchBean.setLiveDate(rs.getDate("live_date").toString());
                matchBean.setGroupName(rs.getString("match_group"));
                matchBean.setHostTeam(rs.getString("host_team"));
                matchBean.setGuestTeam(rs.getString("guest_team"));
                matchBean.setOdds(new Float[]{rs.getFloat("odds_s"), rs.getFloat("odds_p"), rs.getFloat("odds_f")});
                matchBean.setStatus(rs.getString("status"));
                matchBeans.put(matchBean.getMatchNum(), matchBean);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchBeans;
    }


    public static List<MatchBean> getMatchData(java.util.Date date) {
        List<MatchBean> matchBeans = new ArrayList<>();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConn().prepareStatement("select * from live_data where live_date = ? order by live_date asc ");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));


            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchBean matchBean = new MatchBean();
                matchBean.setMatchNum(rs.getString("match_num"));
                matchBean.setHostNum(rs.getInt("host_num"));
                matchBean.setGuestNum(rs.getInt("guest_num"));
                matchBean.setLiveDate(rs.getDate("live_date").toString());
                matchBean.setGroupName(rs.getString("match_group"));
                matchBean.setHostTeam(rs.getString("host_team"));
                matchBean.setGuestTeam(rs.getString("guest_team"));
                matchBean.setOdds(new Float[]{rs.getFloat("odds_s"), rs.getFloat("odds_p"), rs.getFloat("odds_f")});
                matchBean.setStatus(rs.getString("status"));
                matchBeans.add(matchBean);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchBeans;
    }

    public static void delete(java.util.Date date) {
        PreparedStatement plsql;
        try {
            plsql = MysqlManager.getConn().prepareStatement("delete from live_data where live_date = ? and (status != '1' and status != '2')");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));
            plsql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
