function getEmbeddedChart (divId, lineInfos, plotData, enableValues) {
    var valueGeometry = new Timeplot.DefaultValueGeometry({
              gridColor: "#000000",
              axisLabelsPlacement: "left",
              min: 0
          });
    var timeGeometry = new Timeplot.DefaultTimeGeometry({
              gridColor: new Timeplot.Color("#000000"),
              axisLabelsPlacement: "top",
              gridSpacing: 7,
              gridStep: 2
          })
    var eventSource = new Timeplot.DefaultEventSource();
    var plotInfo = [];
    var labels = "";
    for (i = 0; i < lineInfos.length; i++) {
        plotInfo[i] = Timeplot.createPlotInfo({
            id: "plot"+lineInfos[i].id,
            lineColor: lineInfos[i].color,
            lineWidth: 2,
            dataSource: new Timeplot.ColumnSource(eventSource, lineInfos[i].id * 2),
            valueGeometry: valueGeometry,
            timeGeometry: timeGeometry,
            showValues: enableValues
        });
        labels += "<p style='font-weight: bold;color:" + lineInfos[i].color + "'>" + lineInfos[i].gram + "</p>";
    }
    
    //var styleSection = document.createElement("style");
    //styleSection.innerHTML = "#" + divId + " a img { border: 0;}";
    
    var labelsDiv = document.createElement("div");
    //var linkDiv = document.createElement("div");   
    //var timeplotLink = document.createElement("a"); 
    var timeplotDiv = document.createElement("div"); 
    
    
    labelsDiv.innerHTML = labels;
    //var linkUrl = "http://twist.flaptor.com/?gram="+gram;
    //linkDiv.innerHTML = "<a href='" + linkUrl + "'>twist - see trends in twitter</a>";
    //timeplotLink.href=linkUrl;
    //timeplotLink.style.textDecoration="none";
    //timeplotLink.style.color= "#222";
    //timeplotLink.style.border= "0";
    //timeplotDiv.style.backgroundColor= "white";
    
    //document.getElementById(divId).appendChild(styleSection);
    document.getElementById(divId).appendChild(labelsDiv);
    document.getElementById(divId).appendChild(timeplotDiv);//document.getElementById(divId).appendChild(timeplotLink);
    //timeplotLink.appendChild(timeplotDiv);
    //document.getElementById(divId).appendChild(linkDiv);
    document.getElementById(divId).style.fontSize = "10pt";
    //linkDiv.style.fontSize = "11pt";
    //linkDiv.style.fontWeight = "bold";
    //linkDiv.style.textAlign = "right";
    
    var ret = Timeplot.create(timeplotDiv, plotInfo);
    eventSource.loadText(plotData, ",", "/");
    return ret;
}

function preload () {
    var head = document.getElementsByTagName("head")[0];
    var script = document.createElement("script");
    script.id="timeplot";
    script.type="text/javascript";
    script.src="http://static.simile.mit.edu/timeplot/api/1.0/timeplot-api.js";
    head.appendChild(script);

    checkOnLoad ();
}


function checkOnLoad (){
    try {
        new Timeplot.DefaultEventSource();
    } catch (e) {
        window.setTimeout('checkOnLoad ()',1000);
        return;
    }
    onLoad ();
}

var resizeTimerID = null;
function onResize () {
    if (resizeTimerID == null) {
        resizeTimerID = window.setTimeout(function() {
            resizeTimerID = null;
            timeplot .repaint();
        }, 100);
    }
}

preload ();
