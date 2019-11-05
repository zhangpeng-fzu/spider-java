package com.peng.frame;

import com.peng.util.AesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class RegisterFrame extends JFrame {


    private static final long serialVersionUID = 3218784607640603309L;

    public RegisterFrame() {
        super();
        String licence_day = JOptionPane.showInputDialog(this, "请输入生成的激活码有效时间（天）", "提示", JOptionPane.WARNING_MESSAGE);

        try {
            String encode = AesUtil.encrypt(String.valueOf(System.currentTimeMillis() + 86400L * Integer.parseInt(licence_day) * 1000), AesUtil.KEY);
            JOptionPane.showMessageDialog(this, "激活成功，点击确定后可直接复制，激活码：" + encode + "", "提示", JOptionPane.WARNING_MESSAGE);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(encode);
            clipboard.setContents(selection, null);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "激活失败", "提示", JOptionPane.WARNING_MESSAGE);
        }
        this.dispose();

    }


    public static void main(String[] args) {
        new RegisterFrame();
    }
}

