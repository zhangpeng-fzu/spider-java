package com.peng.repository;

import com.peng.bean.MatchCascadeBean;
import com.peng.database.MysqlManager;
import com.peng.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MatchCascadeRepository {

    public static void insert(List<MatchCascadeBean> matchCascadeBeans) throws SQLException {
        PreparedStatement plsql;
        Connection connection = MysqlManager.getConnForCascade();
        connection.setAutoCommit(false);
        plsql = connection.prepareStatement("insert into match_cascade (live_date,match_cascade_num,ss,sp,sf,ps,pp,pf,fs,fp,ff,odds) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?)");
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(matchCascadeBean.getLiveDate())));              //设置参数1，创建id为3212的数据
            plsql.setString(2, matchCascadeBean.getMatchCascadeNum());      //设置参数2，name 为王刚
            plsql.setInt(3, matchCascadeBean.getSs());
            plsql.setInt(4, matchCascadeBean.getSp());
            plsql.setInt(5, matchCascadeBean.getSf());
            plsql.setInt(6, matchCascadeBean.getPs());
            plsql.setInt(7, matchCascadeBean.getPp());
            plsql.setInt(8, matchCascadeBean.getPf());
            plsql.setInt(9, matchCascadeBean.getFs());
            plsql.setInt(10, matchCascadeBean.getFp());
            plsql.setInt(11, matchCascadeBean.getFf());
            plsql.setString(12, matchCascadeBean.getOdds());
            plsql.addBatch();
        }
        plsql.executeBatch();
        plsql.clearBatch();
        connection.commit();
        plsql.close();
    }

    public static java.util.Date clearLastThreeDayData() {
        try (PreparedStatement plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade order by live_date desc limit 1")) {
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                Date lastDate = rs.getDate("live_date");
                //需删除签两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值

                return deleteLatestTwoDays(lastDate);
            }
            return null;
        } catch (Exception se) {
            se.printStackTrace();
            return null;
        }
    }


    public static java.util.Date deleteLatestTwoDays(Date lastDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);
        //需删除签两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值
        calendar.add(Calendar.DATE, -2);
        try (PreparedStatement plsql = MysqlManager.getConnForCascade().prepareStatement("delete from match_cascade where live_date >= ?")) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(calendar.getTime())));
            plsql.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return calendar.getTime();
    }

    public static List<MatchCascadeBean> findLatestCascadeData() {
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConnForCascade().prepareStatement("SELECT * FROM match_cascade WHERE id IN(SELECT MAX(id) FROM match_cascade GROUP BY match_cascade_num) ")) {
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                matchCascadeBeans.add(new MatchCascadeBean(rs));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBeans;
    }


    public static List<MatchCascadeBean> findByLiveDate(java.util.Date date) {
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where live_date = ? ")) {

            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(date)));
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                matchCascadeBeans.add(new MatchCascadeBean(rs));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBeans;
    }

    public static List<MatchCascadeBean> getMatchCascadeDataByNum(String matchCascadeNum) {
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where match_cascade_num = ? ")) {

            plsql.setString(1, matchCascadeNum);
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                matchCascadeBeans.add(new MatchCascadeBean(rs));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBeans;
    }
}
