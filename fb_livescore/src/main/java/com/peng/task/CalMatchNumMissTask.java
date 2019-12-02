package com.peng.task;

import com.peng.service.CalMatchNumMiss;
import lombok.extern.java.Log;

import java.text.ParseException;

@Log
public class CalMatchNumMissTask extends Thread implements Runnable {
    @Override
    public void run() {
        log.info("正在计算进球数据");
        try {
            CalMatchNumMiss.calculate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
