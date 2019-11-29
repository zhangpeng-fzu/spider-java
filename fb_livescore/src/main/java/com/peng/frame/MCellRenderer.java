package com.peng.frame;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MCellRenderer extends DefaultTableCellRenderer {


    private static final long serialVersionUID = 7153906866474195499L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ((String.valueOf(value).equals("0") || String.valueOf(value).contains(":")) && !table.getColumnName(column).equals("比分")) {
            component.setBackground(Color.RED);
        } else {
            String columnName = String.valueOf(table.getColumnModel().getColumn(column).getHeaderValue());

            if (columnName.equals("零球") || columnName.equals("1球3球") || columnName.equals("2球4球") || columnName.equals("5球6球7球")) {
                component.setBackground(Color.LIGHT_GRAY);
            } else if (columnName.equals("1球2球") || columnName.equals("2球3球") || columnName.equals("3球4球")) {
                component.setBackground(new Color(104, 190, 210));
            } else {
                component.setBackground(Color.WHITE);
            }
        }
        if (row >= table.getRowCount() - 3 && (String.valueOf(table.getValueAt(row, 0)).equals("出现总次数")
                || String.valueOf(table.getValueAt(row, 0)).equals("平均遗漏值")
                || String.valueOf(table.getValueAt(row, 0)).equals("最大遗漏值"))
        ) {
            component.setFont(new Font("宋体", Font.BOLD, 12));
        }

        return component;
    }

}
