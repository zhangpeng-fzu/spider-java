package com.peng.task;

import com.peng.frame.LiveScoreFrame;
import com.peng.service.LoadHistoryData;
import lombok.extern.java.Log;

@Log
public class LoadDataTask extends Thread implements Runnable {
    @Override
    public void run() {
        //加载所有场次数据
        log.info("正在加载所有场次数据");
        LoadHistoryData.loadHistoryData();
        LiveScoreFrame.syncMatchData(true);

        new CalMatchNumMissTask().start();
        new CalMatchCascadeMissTask().start();
    }
}
