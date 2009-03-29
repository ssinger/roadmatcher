package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import java.awt.*;
import java.util.Iterator;

import javax.swing.*;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class StatisticsPanel extends JPanel {
    private BorderLayout borderLayout = new BorderLayout();
    private JScrollPane scrollPane = new JScrollPane();
    private JTextPane textPane = new JTextPane();

    public StatisticsPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setPreferredSize(new Dimension(260, 100));
    }
    void jbInit() throws Exception {
        this.setLayout(borderLayout);
        textPane.setEditable(false);
        textPane.setText(
            "<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>Test\r\n    <p>\r\n      \r\n   "
                + " </p>\r\n  </body>\r\n</html>\r\n");
        textPane.setContentType("text/html");
        this.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getViewport().add(textPane, null);
    }
    public void update(MatchEngine engine) {
        StringBuffer b = new StringBuffer();
        b.append("<HTML>\n");
        b.append("    <HEAD></HEAD>\n");
        b.append("    <BODY><SMALL>\n");
        b.append("        <TABLE BORDER=\"1\">\n");
        write("", "A", "B", b);
        write(
            "Total Features",
            engine.getTargetFeatureCollection().size() + "",
            engine.getCandidateFeatureCollection().size() + "",
            b);
        write(
            "Matched Features",
            engine.getMatchedTargetsFeatureCollection().size()
                + " ("
                + displayPercent(
                    engine.getMatchedTargetsFeatureCollection().size(),
                    engine.getTargetFeatureCollection().size())
                + ")",
            engine.getMatchedCandidatesFeatureCollection().size()
                + " ("
                + displayPercent(
                    engine.getMatchedCandidatesFeatureCollection().size(),
                    engine.getCandidateFeatureCollection().size())
                + ")",
            b);
        write(
            "Unmatched Features",
            engine.getUnmatchedTargetsFeatureCollection().size()
                + " ("
                + displayPercent(
                    engine.getUnmatchedTargetsFeatureCollection().size(),
                    engine.getTargetFeatureCollection().size())
                + ")",
            engine.getUnmatchedCandidatesFeatureCollection().size()
                + " ("
                + displayPercent(
                    engine.getUnmatchedCandidatesFeatureCollection().size(),
                    engine.getCandidateFeatureCollection().size())
                + ")",
            b);
        writeScoresAboveThreshold(0.9, b, engine);
        writeScoresAboveThreshold(0.8, b, engine);
        writeScoresAboveThreshold(0.7, b, engine);            
        b.append("        </TABLE>\n");
        b.append("    </SMALL></BODY>\n");
        b.append("</HTML>\n");
        textPane.setText(b.toString());
    }
    private String displayPercent(int numerator, int denominator) {
        return (int) Math.round(numerator * 100.0 / denominator) + "%";
    }
    private void write(String column1, String column2, String column3, StringBuffer b) {
        b.append("            <TR>");
        b.append("<TD>" + GUIUtil.escapeHTML(column1, false, false) + "</TD>");
        b.append("<TD>" + GUIUtil.escapeHTML(column2, false, false) + "</TD>");
        b.append("<TD>" + GUIUtil.escapeHTML(column3, false, false) + "</TD>");
        b.append("</TR>\n");
    }
    private void writeScoresAboveThreshold(
        double threshold,
        StringBuffer b,
        MatchEngine engine) {
        int scoresAboveThreshold = scoresAboveThreshold(threshold, engine);
        write(
            "Scores > " + threshold,
            scoresAboveThreshold
                + " ("
                + displayPercent(
                    scoresAboveThreshold,
                    engine.getTargetFeatureCollection().size())
                + ")",
            scoresAboveThreshold
                + " ("
                + displayPercent(
                    scoresAboveThreshold,
                    engine.getCandidateFeatureCollection().size())
                + ")",
            b);
    }
    private int scoresAboveThreshold(double threshold, MatchEngine engine) {
        int scoresAboveThreshold = 0;
        for (Iterator i = engine.getMatchPairFeatureCollection().iterator();
            i.hasNext();
            ) {
            Feature feature = (Feature) i.next();
            if (((Double) feature.getAttribute(MatchEngine.SCORE_ATTRIBUTE))
                .doubleValue()
                > threshold) {
                scoresAboveThreshold++;
            }
        }
        return scoresAboveThreshold;
    }
}