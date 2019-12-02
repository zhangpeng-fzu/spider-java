package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private String result;
    private int num;


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


    public int getNum() {
        return this.hostNum + this.guestNum;
    }

}
