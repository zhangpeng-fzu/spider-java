package com.peng;

import com.peng.frame.LiveScoreFrame;
import com.peng.service.CalMatchCascadeMiss;
import com.peng.service.CalMatchNumMiss;
import com.peng.service.LoadHistoryData;
import com.peng.service.ParseTodayData;
import com.peng.util.AesUtil;
import com.peng.util.DateUtil;
import org.apache.commons.io.FileUtils;
import sun.reflect.misc.FieldUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.SimpleFormatter;

public class Main {

    public static void main(String[] args) {
        LiveScoreFrame frame;
        try {
            frame = new LiveScoreFrame();
            String licencePath = "/Users/zhangpeng/._licence";
            String invalid_timestamp;

            try {
                if (new File(licencePath).exists()) {
                    String licence = FileUtils.readFileToString(new File(licencePath));
                    invalid_timestamp = AesUtil.decrypt(licence, AesUtil.KEY);
                    if (invalid_timestamp == null || System.currentTimeMillis() > Long.parseLong(invalid_timestamp)) {
                        JOptionPane.showMessageDialog(frame, "激活码已过期，请重新激活", "提示", JOptionPane.WARNING_MESSAGE);
                        frame.dispose();
                        return;
                    }
                } else {
                    String licence = JOptionPane.showInputDialog(frame, "请输入激活码", "提示", JOptionPane.WARNING_MESSAGE);
                    invalid_timestamp = AesUtil.decrypt(licence, AesUtil.KEY);
                    if (invalid_timestamp == null || System.currentTimeMillis() > Long.parseLong(invalid_timestamp)) {
                        JOptionPane.showMessageDialog(frame, "激活码已过期，请重新激活", "提示", JOptionPane.WARNING_MESSAGE);
                        frame.dispose();
                        return;
                    } else {
                        FileUtils.writeStringToFile(new File(licencePath), licence);
                    }
                }

                if (getBjTime().after(new Date(Long.parseLong(invalid_timestamp)))) {
                    JOptionPane.showMessageDialog(frame, "激活码已过期，请重新激活", "提示", JOptionPane.WARNING_MESSAGE);
                    frame.dispose();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "激活码已过期，请重新激活", "提示", JOptionPane.WARNING_MESSAGE);
                frame.dispose();
                return;
            }
            frame.setVisible(true);
            JOptionPane.showMessageDialog(frame, "激活码有效时间：" +
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(Long.parseLong(invalid_timestamp))), "提示", JOptionPane.WARNING_MESSAGE);
//            开启异步线程计算数据
            new Thread(() -> {
                //加载所有场次数据
                System.out.println("正在加载所有场次数据");
                LoadHistoryData.loadHistoryData();
                LiveScoreFrame.syncMatchData();

                new Thread(() -> {
                    System.out.println("正在计算赛事场次数据");
                    try {
                        CalMatchNumMiss.calculate();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }).start();
                try {
                    System.out.println("正在计算串关场次数据");
                    CalMatchCascadeMiss.calculate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }).start();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Date getBjTime() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8")); // 时区设置
        URL url = new URL("http://www.baidu.com");//取得资源对象
        URLConnection uc = url.openConnection();//生成连接对象
        uc.connect(); //发出连接
        long ld = uc.getDate(); //取得网站日期时间（时间戳）
        if (ld == 0) {
            return new Date();
        }
        return new Date(ld);
    }
}
