package com.peng.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchBean {

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
