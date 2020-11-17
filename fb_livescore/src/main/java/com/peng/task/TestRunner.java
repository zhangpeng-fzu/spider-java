package com.peng.task;

import com.peng.service.MainFrameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {


    @Autowired
    private MainFrameService mainFrameService;

    @Override
    public void run(String... args) throws Exception {
        mainFrameService.initMainFrame();

    }
}
