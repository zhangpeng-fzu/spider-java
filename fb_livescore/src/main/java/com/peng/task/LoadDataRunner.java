package com.peng.task;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.service.MatchDataService;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Log
@Component
@Order(1)
public class LoadDataRunner implements CommandLineRunner {

    private final MatchDataService matchDataService;
    private final LiveDataRepository liveDataRepository;

    public LoadDataRunner(MatchDataService matchDataService, LiveDataRepository liveDataRepository) {
        this.matchDataService = matchDataService;
        this.liveDataRepository = liveDataRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        //加载所有场次数据
        log.info("正在加载所有场次数据");
        matchDataService.loadHistoryMatch();

        List<MatchBean> matchBeans = liveDataRepository.findAll();
        Constants.MATCH_CACHE_MAP = matchBeans.stream().collect(Collectors.groupingBy(MatchBean::getMatchNum));

    }
}
