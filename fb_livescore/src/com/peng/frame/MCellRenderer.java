package com.peng.frame;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MCellRenderer extends DefaultTableCellRenderer {


    private static final long serialVersionUID = 7153906866474195499L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (String.valueOf(value).equals("0")) {
            component.setBackground(Color.RED);
        } else {
            String columnName = String.valueOf(table.getColumnModel().getColumn(column).getHeaderValue());

            if (columnName.equals("1球3球") || columnName.equals("2球4球") || columnName.equals("5球6球7球")) {
                component.setBackground(Color.LIGHT_GRAY);
            } else {
                component.setBackground(Color.WHITE);

            }
        }

        return component;
    }

}
