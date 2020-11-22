package com.peng.task;

import com.peng.service.MainFrameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitFrameRunner implements CommandLineRunner {


    private final MainFrameService mainFrameService;

    public InitFrameRunner(MainFrameService mainFrameService) {
        this.mainFrameService = mainFrameService;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean initStatus = mainFrameService.initMainFrame();
        if (!initStatus) {
            System.exit(0);
        }
    }
}
