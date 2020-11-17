package com.peng.task;

import com.peng.service.MainFrameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class InitFrameRunner implements CommandLineRunner {


    private final MainFrameService mainFrameService;

    public InitFrameRunner(MainFrameService mainFrameService) {
        this.mainFrameService = mainFrameService;
    }

    @Override
    public void run(String... args) throws Exception {
        mainFrameService.initMainFrame();
    }
}
