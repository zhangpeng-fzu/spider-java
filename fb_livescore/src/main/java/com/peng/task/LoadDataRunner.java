package com.peng.task;

import com.peng.service.LoadHistoryData;
import com.peng.service.SyncTodayData;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log
@Component
public class LoadDataRunner implements CommandLineRunner {

    private final LoadHistoryData loadHistoryData;
    private final SyncTodayData syncMatchData;

    public LoadDataRunner(LoadHistoryData loadHistoryData, SyncTodayData syncMatchData) {
        this.loadHistoryData = loadHistoryData;
        this.syncMatchData = syncMatchData;
    }

    @Override
    public void run(String... args) throws Exception {
        //加载所有场次数据
        log.info("正在加载所有场次数据");
        loadHistoryData.loadHistoryData();
    }
}
