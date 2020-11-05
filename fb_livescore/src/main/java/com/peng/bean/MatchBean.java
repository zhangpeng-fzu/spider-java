package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "live_data")
public class MatchBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String matchNum;
    private String liveDate;
    private String groupName;
    private String status;
    private String hostTeam;
    private String guestTeam;
    private Float[] odds;
    private int hostNum;
    private int guestNum;
    private int halfHostNum;
    private int halfGuestNum;
    private String result;
    private String halfResult;
    private int num;


    public MatchBean(ResultSet rs) throws SQLException {
        this.setMatchNum(rs.getString("match_num"));
        this.setHostNum(rs.getInt("host_num"));
        this.setHalfHostNum(rs.getInt("half_host_num"));
        this.setGuestNum(rs.getInt("guest_num"));
        this.setHalfGuestNum(rs.getInt("half_guest_num"));
        this.setLiveDate(rs.getDate("live_date").toString());
        this.setGroupName(rs.getString("match_group"));
        this.setHostTeam(rs.getString("host_team"));
        this.setGuestTeam(rs.getString("guest_team"));
        this.setOdds(new Float[]{rs.getFloat("odds_s"), rs.getFloat("odds_p"), rs.getFloat("odds_f")});
        this.setStatus(rs.getString("status"));
    }

    public String getResult() {
        if (this.hostNum > this.guestNum) {
            this.result = "s";
        } else if (this.hostNum == this.guestNum) {
            this.result = "p";
        } else {
            this.result = "f";
        }
        return this.result;
    }

    public String getCNResult() {
        if (this.hostNum > this.guestNum) {
            return "胜";
        } else if (this.hostNum == this.guestNum) {
            return "平";
        } else {
            return "负";
        }
    }

    public String getCNHalfResult() {
        if (this.halfHostNum > this.halfGuestNum) {
            return "胜";
        } else if (this.halfHostNum == this.halfGuestNum) {
            return "平";
        } else {
            return "负";
        }
    }

    public int getNum() {
        return this.hostNum + this.guestNum;
    }

    public String getMatchStatus() {
        switch (this.getNum()) {
            case 1:
            case 3:
                return "单";
            case 2:
            case 4:
                return "双";
            default:
                return "爆";
        }
    }

}
