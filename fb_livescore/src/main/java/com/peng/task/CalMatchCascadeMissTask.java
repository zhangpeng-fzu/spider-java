package com.peng.task;

import com.peng.service.CalMatchCascadeMiss;
import lombok.extern.java.Log;

import java.text.ParseException;

@Log
public class CalMatchCascadeMissTask extends Thread implements Runnable {

    @Override
    public void run() {
        try {
            log.info("正在计算串关数据");
            CalMatchCascadeMiss.calculate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
