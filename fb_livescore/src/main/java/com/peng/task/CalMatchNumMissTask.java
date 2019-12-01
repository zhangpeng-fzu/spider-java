package com.peng.task;

import com.peng.service.CalMatchNumMiss;

import java.text.ParseException;

public class CalMatchNumMissTask extends Thread implements Runnable {
    @Override
    public void run() {
        System.out.println("正在计算进球数据");
        try {
            CalMatchNumMiss.calculate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
