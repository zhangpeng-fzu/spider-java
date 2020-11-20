package com.peng.service;

import com.peng.bean.MatchBean;
import com.peng.config.ApplicationProperties;
import com.peng.constant.Constants;
import com.peng.frame.LiveScoreFrame;
import com.peng.repository.LiveDataRepository;
import com.peng.util.AesUtil;
import com.peng.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainFrameService {
    private final LiveDataRepository liveDataRepository;
    private final ApplicationProperties applicationProperties;

    public MainFrameService(LiveDataRepository liveDataRepository, ApplicationProperties applicationProperties) {
        this.liveDataRepository = liveDataRepository;
        this.applicationProperties = applicationProperties;
    }


    private boolean checkLicense(Frame frame) {
        String expireMessage = "激活码已过期，请重新激活，软件授权联系QQ：470360567";
        String noActiveCodeMessage = "请输入激活码，软件授权联系QQ：470360567";
        String licencePath = applicationProperties.getLicencePath();

        try {
            String licence;
            String invalid_timestamp;
            if (new File(licencePath).exists()) {
                licence = FileUtils.readFileToString(new File(licencePath), StandardCharsets.UTF_8);
                invalid_timestamp = AesUtil.decrypt(licence, AesUtil.KEY);
                if (invalid_timestamp == null || DateUtil.getBjTime().getTime() > Long.parseLong(invalid_timestamp)) {
                    JOptionPane.showMessageDialog(frame, expireMessage, "提示", JOptionPane.WARNING_MESSAGE);
                    licence = JOptionPane.showInputDialog(frame, noActiveCodeMessage, "提示", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                licence = JOptionPane.showInputDialog(frame, noActiveCodeMessage, "提示", JOptionPane.WARNING_MESSAGE);
            }
            if (licence == null || licence.length() == 0) {
                return false;
            }
            invalid_timestamp = AesUtil.decrypt(licence, AesUtil.KEY);
            if (invalid_timestamp == null || DateUtil.getBjTime().getTime() > Long.parseLong(invalid_timestamp)) {
                JOptionPane.showMessageDialog(frame, expireMessage, "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            } else {
                FileUtils.writeStringToFile(new File(licencePath), licence, StandardCharsets.UTF_8);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, expireMessage, "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    public boolean initMainFrame() {
        LiveScoreFrame frame;
        try {
            List<MatchBean> matchBeans = liveDataRepository.findAll();
            Constants.MATCH_CACHE_MAP = matchBeans.stream().peek(matchBean -> matchBean.setMatchNum(matchBean.getMatchNum().substring(2))).collect(Collectors.groupingBy(MatchBean::getMatchNum));

            frame = new LiveScoreFrame();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        if (!this.checkLicense(frame)) {
            frame.dispose();

            return false;
        }

        frame.setVisible(true);
        return true;
    }
}
