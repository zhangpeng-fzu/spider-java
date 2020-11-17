package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataNRepository;
import com.peng.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
public class MatchDataPanelFactory extends PaneFactory {
    private final static MatchDataPanelFactory matchDataPanelFactory = new MatchDataPanelFactory();
    @Autowired
    private LiveDataNRepository liveDataNRepository;

    public static MatchDataPanelFactory getInstance() {
        return matchDataPanelFactory;
    }

    public JScrollPane showMatchDataPane(Date date) {

        String[] columnNames = Constants.MATCH_COLUMNS;// 定义表格列名数组
        List<MatchBean> matchBeanList = liveDataNRepository.findAllByLiveDate(DateUtil.getDateFormat().format(date));

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            //缓存比赛状态
            MatchStatus.MATCH_STATUS_MAP.put(matchBean.getMatchNum().replaceAll("周[一|二|三|四|五|六|日]", ""), matchBean.getStatus());
            String result = matchBean.getResult();

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
            rowData[i] = new String[]{matchBean.getMatchNum(), matchBean.getLiveDate(), matchBean.getMatchGroup(), status, matchBean.getHostTeam(),
                    matchBean.getGuestTeam(), String.valueOf(matchBean.getOdds()[0]), String.valueOf(matchBean.getOdds()[1]), String.valueOf(matchBean.getOdds()[2]),
                    status.equals("完") ? String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum()) : "", Constants.MATCH_RES_MAP.get(result)};
        }


        JTable table = new JTable(rowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table);
        return new JScrollPane(table);
    }

    @Override
    protected String[] calcMissValue(MatchBean matchBean, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) throws ParseException {
        return new String[0];
    }

    @Override
    protected void fillTableData(String[] tableDatum, String[] missValues, MatchBean matchBean) throws ParseException {

    }

    @Override
    protected void fillTodayData(String[] tableDatum, String[] columnNames, String[] curCompareData, int step, int offset) throws ParseException {

    }

    @Override
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        return new String[0];
    }
}
