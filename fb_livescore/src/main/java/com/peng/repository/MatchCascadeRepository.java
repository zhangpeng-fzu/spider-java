package com.peng.repository;

import com.peng.bean.MatchCascadeBean;
import com.peng.database.MysqlManager;
import com.peng.util.ArrayUtil;
import com.peng.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MatchCascadeRepository {

    public static void insert(List<MatchCascadeBean> matchCascadeBeans) throws SQLException {
        PreparedStatement plsql;
        Connection connection = MysqlManager.getConnForCascade();
        connection.setAutoCommit(false);
        plsql = connection.prepareStatement("insert into match_cascade (live_date,match_cascade_num,miss_values,odds) "
                + "values(?,?,?,?)");
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(matchCascadeBean.getLiveDate())));              //设置参数1，创建id为3212的数据
            plsql.setString(2, matchCascadeBean.getMatchCascadeNum());      //设置参数2，name 为王刚
            plsql.setString(3, Arrays.toString(matchCascadeBean.getMissValues()));
            plsql.setString(4, matchCascadeBean.getOdds());
            plsql.addBatch();
        }
        plsql.executeBatch();
        plsql.clearBatch();
        connection.commit();
        plsql.close();
    }

    public static java.util.Date clearLastThreeDayData() {
        Calendar calendar = Calendar.getInstance();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade order by live_date desc limit 1");

            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                Date lastDate = rs.getDate("live_date");
                calendar.setTime(lastDate);
                //需删除签两天的数据，由于当天可能会获取到前天的数据，导致计算不准，需重新计算前2天的遗漏值
                calendar.add(Calendar.DATE, -2);

                plsql = MysqlManager.getConnForCascade().prepareStatement("delete from match_cascade where live_date >= ?");
                plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(calendar.getTime())));
                plsql.execute();
            } else {
                return null;
            }
            plsql.close();
        } catch (Exception se) {
            se.printStackTrace();
            return null;
        }

        return calendar.getTime();
    }

    public static MatchCascadeBean findByLiveDateAndCascadeNum(java.util.Date lastDate, String matchCascadeNum) {
        MatchCascadeBean matchCascadeBean = new MatchCascadeBean();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where live_date = ? and match_cascade_num = ?");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(lastDate)));
            plsql.setString(2, matchCascadeNum);
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                matchCascadeBean.setLiveDate(rs.getDate("live_date"));
                matchCascadeBean.setMatchCascadeNum(rs.getString("match_cascade_num"));
                matchCascadeBean.setMissValues(ArrayUtil.string2IntArray(rs.getString("miss_values"), 9));
            }

            plsql.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBean;
    }


    public static List<MatchCascadeBean> getMatchCascadeData(java.util.Date date) {
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where live_date = ? ");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(date)));
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchCascadeBean matchCascadeBean = new MatchCascadeBean();
                matchCascadeBean.setLiveDate(rs.getDate("live_date"));
                matchCascadeBean.setMatchCascadeNum(rs.getString("match_cascade_num"));
                matchCascadeBean.setMissValues(ArrayUtil.string2IntArray(rs.getString("miss_values"), 9));
                matchCascadeBeans.add(matchCascadeBean);
            }

            plsql.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBeans;
    }

    public static List<MatchCascadeBean> getMatchCascadeDataByNum(String matchCascadeNum) {
        List<MatchCascadeBean> matchCascadeBeans = new ArrayList<>();
        try {
            PreparedStatement plsql;
            plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where match_cascade_num = ? ");
            plsql.setString(1, matchCascadeNum);
            ResultSet rs = plsql.executeQuery();
            while (rs.next()) {
                MatchCascadeBean matchCascadeBean = new MatchCascadeBean();
                matchCascadeBean.setLiveDate(rs.getDate("live_date"));
                matchCascadeBean.setMatchCascadeNum(rs.getString("match_cascade_num"));
                matchCascadeBean.setMissValues(ArrayUtil.string2IntArray(rs.getString("miss_values"), 9));
                matchCascadeBean.setOdds(rs.getString("odds"));
                matchCascadeBeans.add(matchCascadeBean);
            }
            plsql.close();

        } catch (Exception se) {
            se.printStackTrace();
        }
        return matchCascadeBeans;
    }
}
