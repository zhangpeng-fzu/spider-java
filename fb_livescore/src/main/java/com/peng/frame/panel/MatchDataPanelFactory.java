package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class MatchDataPanelFactory extends PaneFactory {
    private final static MatchDataPanelFactory matchDataPanelFactory;

    static {
        matchDataPanelFactory = new MatchDataPanelFactory();
    }

    public static MatchDataPanelFactory getInstance() {
        return matchDataPanelFactory;
    }


    public JScrollPane showMatchDataPane(Date date) {

        String[] columnNames = Constants.MATCH_COLUMNS;// 定义表格列名数组
        java.util.List<MatchBean> matchBeanList = LiveDataRepository.getMatchData(date);

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            //缓存比赛状态
            Constants.MATCH_STATUS_MAP.put(matchBean.getMatchNum().replaceAll("周[一|二|三|四|五|六|日]", ""), matchBean.getStatus());
            String result = matchBean.getResult();

            String status = matchBean.getStatus();
            switch (matchBean.getStatus()) {
                case Constants.CANCELLED:
                    status = "取消";
                    break;
                case Constants.PLAYING:
                    status = "未";
                    result = "";
                    break;
                case Constants.FINISHED:
                    status = "完";
                    break;
                default:
                    result = "";
                    break;
            }
            rowData[i] = new String[]{matchBean.getMatchNum(), matchBean.getLiveDate(), matchBean.getGroupName(), status, matchBean.getHostTeam(),
                    matchBean.getGuestTeam(), String.valueOf(matchBean.getOdds()[0]), String.valueOf(matchBean.getOdds()[1]), String.valueOf(matchBean.getOdds()[2]),
                    status.equals("完") ? String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum()) : "", Constants.MATCH_RES_MAP.get(result)};
        }


        JTable table = new JTable(rowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table);
        return new JScrollPane(table);
    }
}
