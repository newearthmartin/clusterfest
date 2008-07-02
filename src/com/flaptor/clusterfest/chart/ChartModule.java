package com.flaptor.clusterfest.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flaptor.clusterfest.Module;
import com.flaptor.clusterfest.NodeDescriptor;
import com.flaptor.clusterfest.WebModule;
import com.flaptor.clusterfest.exceptions.NodeException;
import com.flaptor.util.Pair;
import com.flaptor.util.remote.WebServer;
import com.flaptor.util.timeplot.TimePlotUtils;

/**
 * Simple (web)module that displays a chart
 * @author Martin Massera
 */
abstract public class ChartModule implements Module, WebModule{

    private static final String[] COLORS = new String[]{"FF2233", "0785A9", "FFBF00", "CC99FF", "440373", "666666", "00CC66", "CC0033", "33FFFF", "FF6666"};

    private String pageName;
    
    public ChartModule(String pageName) {
        this.pageName = pageName;
    }
    
    public void nodeUnregistered(NodeDescriptor node) {}
    public void notifyNode(NodeDescriptor nodeDescriptor) throws NodeException {}

    public void setup(WebServer server) {}
    public List<String> getActions() {return null;}
    public ActionReturn action(String action, HttpServletRequest request) {return null;}
    public String getModuleHTML() {return null;}
    public String getNodeHTML(NodeDescriptor node, int nodeNum) {return null;}
    public List<Pair<String, String>> getSelectedNodesActions() {return null;}
    public ActionReturn selectedNodesAction(String action, List<NodeDescriptor> nodes, HttpServletRequest request) {return null;}

    public List<String> getPages() {
        return Arrays.asList(new String[]{pageName});
    }
    
    abstract public Pair<List<String>, Map<Date, List<Number>>> getChartData(HttpServletRequest request); 
    
    public String doPage(String page, HttpServletRequest request, HttpServletResponse response) {
        Pair<List<String>, Map<Date, List<Number>>> chartData = getChartData(request);
        
         
        List<LineInfo> lineInfos = new ArrayList<LineInfo>();
        int i = 1;
        for (String lineName : chartData.first()) {
            lineInfos.add(new LineInfo(i, lineName, COLORS[(i-1) % COLORS.length]));
            i++;
        }
        request.setAttribute("lineInfos", lineInfos);
        request.setAttribute("plot", TimePlotUtils.generateInputDate(chartData.last(), "\\n"));
        return "chart.vm";
    }
    
    public static class LineInfo {
        private int id;
        private String gram;
        private String color;
        public LineInfo(int id, String gram, String color) {
            this.id = id;
            this.gram = gram;
            this.color = color;
        }
        public int getId() {
            return id;
        }
        public String getGram() {
            return gram;
        }
        public String getColor() {
            return color;
        }
    }
}
