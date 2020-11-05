package com.peng.repository;

import com.peng.bean.MatchBean;
import com.peng.database.MysqlManager;
import com.peng.util.DateUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class LiveDataRepository {
    private static final String INSERT_SQL = "insert into live_data (live_date,match_num,match_group,host_team,guest_team,host_num,guest_num,half_host_num,half_guest_num,odds_s,odds_p,odds_f,status) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE match_num=?,host_num=?,guest_num=?,odds_s=?,odds_p=?,odds_f=?,status=?";

    private static final String SELECT_MAX_DATE = "select distinct live_date from live_data order by live_date desc limit 1";
    private static final String DELETE_SQL = "delete from live_data where live_date >= ?";

    private static final String SELECT_ONE = "select distinct live_date from live_data order by live_date desc limit 1";
    private static final String SELECT_LIST = "select * from live_data where live_date = ? and status != 2 order by live_date asc";
    private static final String SELECT_ALL_LIST = "select * from live_data where live_date = ? order by live_date asc ";

    public static void insert(MatchBean matchBean) {
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(INSERT_SQL)) {
            plsql.setDate(1, Date.valueOf(matchBean.getLiveDate()));              //设置参数1，创建id为3212的数据
            plsql.setString(2, matchBean.getMatchNum());      //设置参数2，name 为王刚
            plsql.setString(3, matchBean.getGroupName());
            plsql.setString(4, matchBean.getHostTeam());
            plsql.setString(5, matchBean.getGuestTeam());
            plsql.setInt(6, matchBean.getHostNum());
            plsql.setInt(7, matchBean.getGuestNum());
            plsql.setInt(8, matchBean.getHalfHostNum());
            plsql.setInt(9, matchBean.getHalfGuestNum());
            plsql.setFloat(10, matchBean.getOdds()[0] != null ? matchBean.getOdds()[0] : 0);
            plsql.setFloat(11, matchBean.getOdds()[1] != null ? matchBean.getOdds()[1] : 0);
            plsql.setFloat(12, matchBean.getOdds()[2] != null ? matchBean.getOdds()[2] : 0);
            plsql.setString(13, matchBean.getStatus());
            plsql.setString(14, matchBean.getMatchNum());
            plsql.setInt(15, matchBean.getHostNum());
            plsql.setInt(16, matchBean.getGuestNum());
            plsql.setFloat(17, matchBean.getOdds()[0] != null ? matchBean.getOdds()[0] : 0);
            plsql.setFloat(18, matchBean.getOdds()[1] != null ? matchBean.getOdds()[1] : 0);
            plsql.setFloat(19, matchBean.getOdds()[2] != null ? matchBean.getOdds()[2] : 0);
            plsql.setString(20, matchBean.getStatus());
            plsql.executeUpdate();
        } catch (Exception se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        }
    }

    public static String clearLastThreeDayData() {
        Date lastDate = Date.valueOf("2010-01-01");
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(SELECT_MAX_DATE)) {
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                lastDate = rs.getDate("live_date");
                return DateUtil.getDateFormat().format(deleteLatestTwoDays(lastDate));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return lastDate.toString();
    }


    public static java.util.Date deleteLatestTwoDays(Date lastDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);
        //需删除签两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值
        calendar.add(Calendar.DATE, -2);
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(DELETE_SQL)) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(calendar.getTime())));
            plsql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return calendar.getTime();
    }

    public static java.util.Date getMaxLiveDate() throws ParseException {
        Date maxDate = Date.valueOf("2010-01-01");
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(SELECT_ONE)) {
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                maxDate = rs.getDate("live_date");
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return DateUtil.getDateFormat().parse(maxDate.toString());
    }

    public static Map<String, MatchBean> getMatchMap(java.util.Date date) {
        Map<String, MatchBean> matchBeans = new HashMap<>();
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(SELECT_LIST)) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchBean matchBean = new MatchBean(rs);
                matchBean.setMatchNum(rs.getString("match_num").replaceAll("周[一|二|三|四|五|六|日]", ""));
                matchBeans.put(matchBean.getMatchNum(), matchBean);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchBeans;
    }


    public static List<MatchBean> getMatchData(java.util.Date date) {
        List<MatchBean> matchBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement(SELECT_ALL_LIST)) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                matchBeans.add(new MatchBean(rs));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchBeans;
    }

    public static void delete(java.util.Date date) {
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement("delete from live_data where live_date = ? and (status != '1' and status != '2')")) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat().format(date)));
            plsql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<MatchBean> getMatchListByNum(String matchNum) {
        List<MatchBean> matchBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConn().prepareStatement("select * from live_data where match_num like ? order by live_date")) {
            plsql.setString(1, "%" + matchNum);
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                matchBeans.add(new MatchBean(rs));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchBeans;
    }
}
