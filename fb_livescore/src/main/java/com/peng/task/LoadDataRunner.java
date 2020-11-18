package com.peng.task;

import com.peng.service.LoadHistoryData;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log
@Component
public class LoadDataRunner implements CommandLineRunner {

    private final LoadHistoryData loadHistoryData;

    public LoadDataRunner(LoadHistoryData loadHistoryData) {
        this.loadHistoryData = loadHistoryData;
    }

    @Override
    public void run(String... args) throws Exception {
        //加载所有场次数据
        log.info("正在加载所有场次数据");
        loadHistoryData.loadHistoryData();
    }
}
