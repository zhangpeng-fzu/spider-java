package com.peng.task;

import com.peng.service.LoadHistoryData;
import com.peng.service.SyncTodayData;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log
@Component
public class LoadDataTask implements CommandLineRunner {


    @Autowired
    private LoadHistoryData loadHistoryData;
    @Autowired
    private SyncTodayData syncMatchData;

    @Override
    public void run(String... args) throws Exception {
        //加载所有场次数据
        System.out.println("正在加载所有场次数据");
        loadHistoryData.loadHistoryData();
        syncMatchData.getMatchData();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        new CalMatchCascadeMissTask().start();
//        LiveDataRepository.cacheAllMatchData();
    }
}
