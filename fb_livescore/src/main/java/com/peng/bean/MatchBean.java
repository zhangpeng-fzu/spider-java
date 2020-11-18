package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "live_data")
public class MatchBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String matchNum;
    private String liveDate;
    private String matchGroup;
    private String status;
    private String hostTeam;
    private String guestTeam;
    @Column(name = "odds_s")
    private Float oddsS;
    @Column(name = "odds_p")
    private Float oddsP;
    @Column(name = "odds_f")
    private Float oddsF;

    private int hostNum;
    private int guestNum;
    private int halfHostNum;
    private int halfGuestNum;
    @Transient
    private Float[] odds;

    @Transient
    private String result;
    @Transient
    private String halfResult;
    @Transient
    private int num;


    public Float[] getOdds() {
        return new Float[]{this.oddsS, this.oddsP, this.oddsF};
    }

    public void setOdds(Float[] odds) {
        this.oddsS = odds[0];
        this.oddsP = odds[1];
        this.oddsF = odds[2];
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
