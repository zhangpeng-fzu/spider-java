package com.peng.task;

import com.peng.service.MatchDataService;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log
@Component
public class LoadDataRunner implements CommandLineRunner {

    private final MatchDataService matchDataService;

    public LoadDataRunner(MatchDataService matchDataService) {
        this.matchDataService = matchDataService;
    }

    @Override
    public void run(String... args) throws Exception {
        //加载所有场次数据
        log.info("正在加载所有场次数据");
        matchDataService.loadHistoryMatch();
    }
}
