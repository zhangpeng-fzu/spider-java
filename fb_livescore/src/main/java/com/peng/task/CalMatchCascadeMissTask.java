package com.peng.task;

import com.peng.service.CalMatchCascadeMiss;

import java.text.ParseException;

public class CalMatchCascadeMissTask extends Thread implements Runnable {

    @Override
    public void run() {
        try {
            System.out.println("正在计算串关数据");
            CalMatchCascadeMiss.calculate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
