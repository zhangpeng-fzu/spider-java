package com.peng.repository;

import com.peng.bean.MatchCascadeBean;
import com.peng.database.MysqlManager;
import com.peng.util.DateUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MatchCascadeRepository {

    public static void insert(List<MatchCascadeBean> matchCascadeBeans) throws SQLException {
        PreparedStatement plsql;
        plsql = MysqlManager.getConnForCascade().prepareStatement("insert into match_cascade (live_date,match_cascade_num,ss,sp,sf,ps,pp,pf,fs,fp,ff,odds) "
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
            plsql = MysqlManager.getConnForCascade().prepareStatement("select * from match_cascade where live_date < ? and match_cascade_num = ? order by live_date desc limit 1");
            plsql.setDate(1, Date.valueOf(DateUtil.getDateFormat(2).format(lastDate)));
            plsql.setString(2, matchCascadeNum);
            ResultSet rs = plsql.executeQuery();
            if (rs.next()) {
                matchCascadeBean.setLiveDate(rs.getDate("live_date"));
                matchCascadeBean.setMatchCascadeNum(rs.getString("match_cascade_num"));
                matchCascadeBean.setSs(rs.getInt("ss"));
                matchCascadeBean.setSp(rs.getInt("sp"));
                matchCascadeBean.setSf(rs.getInt("sf"));
                matchCascadeBean.setPs(rs.getInt("ps"));
                matchCascadeBean.setPp(rs.getInt("pp"));
                matchCascadeBean.setPf(rs.getInt("pf"));
                matchCascadeBean.setFs(rs.getInt("fs"));
                matchCascadeBean.setFp(rs.getInt("fp"));
                matchCascadeBean.setFf(rs.getInt("ff"));
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
                matchCascadeBean.setSs(rs.getInt("ss"));
                matchCascadeBean.setSp(rs.getInt("sp"));
                matchCascadeBean.setSf(rs.getInt("sf"));
                matchCascadeBean.setPs(rs.getInt("ps"));
                matchCascadeBean.setPp(rs.getInt("pp"));
                matchCascadeBean.setPf(rs.getInt("pf"));
                matchCascadeBean.setFs(rs.getInt("fs"));
                matchCascadeBean.setFp(rs.getInt("fp"));
                matchCascadeBean.setFf(rs.getInt("ff"));
                matchCascadeBean.setOdds(rs.getString("odds"));
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
                matchCascadeBean.setSs(rs.getInt("ss"));
                matchCascadeBean.setSp(rs.getInt("sp"));
                matchCascadeBean.setSf(rs.getInt("sf"));
                matchCascadeBean.setPs(rs.getInt("ps"));
                matchCascadeBean.setPp(rs.getInt("pp"));
                matchCascadeBean.setPf(rs.getInt("pf"));
                matchCascadeBean.setFs(rs.getInt("fs"));
                matchCascadeBean.setFp(rs.getInt("fp"));
                matchCascadeBean.setFf(rs.getInt("ff"));
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
