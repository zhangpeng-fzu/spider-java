package com.peng;

import com.peng.frame.LiveScoreFrame;
import com.peng.task.LoadDataTask;
import com.peng.util.AesUtil;
import com.peng.util.DateUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) {
        LiveScoreFrame frame;
        try {
            frame = new LiveScoreFrame();
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        if (!checkLicense(frame)) {
            frame.dispose();
            return;
        }

        frame.setVisible(true);
        //启动数据加载任务
        new LoadDataTask().start();

    }

    private static boolean checkLicense(Frame frame) {
        String expireMessage = "激活码已过期，请重新激活，软件授权联系QQ：470360567";
        String noActiveCodeMessage = "请输入激活码，软件授权联系QQ：470360567";
//        String licencePath = "/Users/zhangpeng/._licence";
        String licencePath = "D:/._licence";

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
}
