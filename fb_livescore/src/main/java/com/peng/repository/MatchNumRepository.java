package com.peng.repository;

import com.peng.bean.MatchNumBean;
import com.peng.database.MysqlManager;
import com.peng.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MatchNumRepository {


    public static void insert(List<MatchNumBean> matchNumBeans) throws SQLException {
        PreparedStatement plsql;
        Connection connection = MysqlManager.getConnForNum();
        plsql = connection.prepareStatement("insert into match_num (live_date,match_num,zero,one,two,three,four,five,six,seven,one_three,two_four,five_,one_two,two_three,three_four) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        connection.setAutoCommit(false);
        for (MatchNumBean matchNumBean : matchNumBeans) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(3).format(matchNumBean.getLiveDate())));              //设置参数1，创建id为3212的数据
            plsql.setString(2, matchNumBean.getMatchNum());      //设置参数2，name 为王刚
            plsql.setInt(3, matchNumBean.getZero());
            plsql.setInt(4, matchNumBean.getOne());
            plsql.setInt(5, matchNumBean.getTwo());
            plsql.setInt(6, matchNumBean.getThree());
            plsql.setInt(7, matchNumBean.getFour());
            plsql.setInt(8, matchNumBean.getFive());
            plsql.setInt(9, matchNumBean.getSix());
            plsql.setInt(10, matchNumBean.getSeven());
            plsql.setInt(11, matchNumBean.getOneThree());
            plsql.setInt(12, matchNumBean.getTwoFour());
            plsql.setInt(13, matchNumBean.getFive_());
            plsql.setInt(14, matchNumBean.getOneTwo());
            plsql.setInt(15, matchNumBean.getTwoThree());
            plsql.setInt(16, matchNumBean.getThreeFour());
            plsql.addBatch();
        }
        plsql.executeBatch();
        plsql.clearBatch();
        connection.commit();
        plsql.close();
    }

    public static java.util.Date clearLastThreeDayData() {
        try (PreparedStatement plsql = MysqlManager.getConnForNum().prepareStatement("select * from match_num order by live_date desc limit 1")) {
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
        try (PreparedStatement plsql = MysqlManager.getConnForNum().prepareStatement("delete from match_num where live_date >= ?")) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(calendar.getTime())));
            plsql.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return calendar.getTime();
    }

    public static MatchNumBean findByLiveDateAndNum(java.util.Date date, String matchNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        java.util.Date preDate = calendar.getTime();
        MatchNumBean matchNumBean = new MatchNumBean();
        try (PreparedStatement plsql = MysqlManager.getConnForNum().prepareStatement("select * from match_num where live_date = ? and match_num = ?")) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(3).format(preDate)));
            plsql.setString(2, matchNum);
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                matchNumBean = new MatchNumBean(rs);
            }

        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchNumBean;
    }

    public static List<MatchNumBean> getMatchNumData(java.util.Date date) {
        List<MatchNumBean> matchNumBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConnForNum().prepareStatement("select * from match_num where live_date = ?")) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(3).format(date)));
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchNumBean matchNumBean = new MatchNumBean(rs);
                matchNumBeans.add(matchNumBean);
            }

        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchNumBeans;
    }

    public static List<MatchNumBean> getMatchNumDataByNum(String matchNum) {
        List<MatchNumBean> matchNumBeans = new ArrayList<>();
        try (PreparedStatement plsql = MysqlManager.getConnForNum().prepareStatement("select * from match_num where match_num = ?");) {
            plsql.setString(1, matchNum);
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchNumBean matchNumBean = new MatchNumBean(rs);
                matchNumBeans.add(matchNumBean);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchNumBeans;
    }
}
