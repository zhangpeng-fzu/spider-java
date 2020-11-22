package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.List;

@Service
public class MatchDataPanelFactory extends PaneFactory {
    private final LiveDataRepository liveDataRepository;

    public MatchDataPanelFactory(LiveDataRepository liveDataRepository) {
        this.liveDataRepository = liveDataRepository;
    }

    @Override
    public JScrollPane showMatchPaneByDate(JFrame frame, String date) {

        String[] columnNames = Constants.MATCH_COLUMNS;// 定义表格列名数组
        List<MatchBean> matchBeanList = liveDataRepository.findAllByLiveDate(date);

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            String result = matchBean.getCNResult();

            String status = matchBean.getStatus();
            switch (matchBean.getStatus()) {
                case MatchStatus.CANCELLED:
                    status = "取消";
                    break;
                case MatchStatus.PLAYING:
                    status = "未";
                    result = "";
                    break;
                case MatchStatus.FINISHED:
                    status = "完";
                    break;
                default:
                    result = "";
                    break;
            }
            rowData[i] = new String[]{matchBean.getWeekNum() + matchBean.getMatchNum(), matchBean.getLiveDate(), matchBean.getMatchGroup(), status, matchBean.getHostTeam(),
                    matchBean.getGuestTeam(), String.valueOf(matchBean.getOdds()[0]), String.valueOf(matchBean.getOdds()[1]), String.valueOf(matchBean.getOdds()[2]),
                    status.equals("完") ? String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum()) : "", result};
        }


        JTable table = new JTable(rowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table);
        return new JScrollPane(table);
    }

    @Override
    protected String[] calcMissValue(MatchBean matchBean, MatchBean nextMatch, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) throws ParseException {
        return new String[0];
    }

    @Override
    protected void fillTableRow(String[] tableDatum, String[] missValues, MatchBean matchBean) throws ParseException {

    }

    @Override
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        return new String[0];
    }
}
