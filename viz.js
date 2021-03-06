var monthFullNames = ["January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"
];
var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];
var titles = {
    negative: "Negative Tweet Count",
    positive: "Positive Tweet Count",
    neutral: "Neutral Tweet Count",
    impact: "Tweets Aggregated Impact",
}
var data = {};
var tweets;
var leftList = d3.select("#left-dimension .content");
var rightList = d3.select("#right-dimension .content");
var colorScale = d3.scale.category10();
var aggregation = "w";
var scaleType = "s";
var dotSize = 3;
var dotEmph = 4;
var strokeWidth = 2;
var left = "channels";
var right = "candidates";
var filteredData = [];
var expanded = true;
var yRanges = undefined;

var minWidth = 75;
var minHeight = 75;

var hidden = false;
var hideToolbar = d3.select("#hide")
    .on("click", function(e) {
        if (hidden) {
            d3.select("#top").style("display", "block");
            this.innerHTML = "HIDE TOOLBAR"
        }
        else {
            d3.select("#top").style("display", "none");
            this.innerHTML = "SHOW TOOLBAR"
        }
        hidden = !hidden;
    });

var l = leftList.append("label");
l.append("input")
    .attr("type", "button")
    .attr("value", "All")
    .attr("class", "all")
    .on("click", function() {
        leftList.selectAll("label.item input").each(function (d) {
            this.checked = true;
        });
        renderTable();
    });
l.append("input")
    .attr("type", "button")
    .attr("value", "None")
    .attr("class", "none")
    .on("click", function() {
        leftList.selectAll("label.item input").each(function (d) {
            this.checked = false;
        });
        renderTable();
    });

l = rightList.append("label");
    l.append("input")
        .attr("type", "button")
        .attr("value", "All")
        .attr("class", "all")
        .on("click", function() {
            rightList.selectAll("label.item input").each(function (d) {
                this.checked = true;
            });
            renderTable();
        });
    l.append("input")
        .attr("type", "button")
        .attr("value", "None")
        .attr("class", "none")
        .on("click", function() {
            rightList.selectAll("label.item input").each(function (d) {
                this.checked = false;
            });
            renderTable();
        });
    
function resize() {
//     d3.select("#center").style("padding-top", d3.select("#top").node().getBoundingClientRect().height);
    renderTable();
}

d3.select(window).on('resize', resize);

d3.json("data.json",
    function(error, result) {
    data = result;
    tweets = data.tweets;
    prepareAll();
//     console.log(filteredData);
    renderAll();
});

function prepareAll() {
    filteredData = [];
    var rows = getRowNames();
    var columns = getLineNames();
//     console.log(rows);
//     console.log(columns);
    yRanges = undefined;
    for (var i = 0; i < rows.length; i++) {
        yRanges = maxRanges(yRanges, prepareRowData(rows[i], columns));
//         console.log(yRanges);
    }
//     console.log(filteredData);
}

function maxRanges(r, s) {
    if (r == undefined) return s;
    else if (s == undefined) return r;
    else return {
        "impact": {
            "min": Math.min(r.impact.min, s.impact.min),
            "max": Math.max(r.impact.max, s.impact.max)
        },
        "sum": {
            "min": Math.min(r.sum.min, s.sum.min),
            "max": Math.max(r.sum.max, s.sum.max)
        }
    }
}

d3.select("#daily").on("change", function (d) {
    aggregation = "d";
    prepareAll();
    renderAll();
});

d3.select("#weekly").on("change", function (d) {
    aggregation = "w";
    prepareAll();
    renderAll();
});

d3.select("#monthly").on("change", function (d) {
    aggregation = "m";
    prepareAll();
    renderAll();
});

d3.select("#sum").on("change", function (d) {
    scaleType = "s";
    renderRows();
});

d3.select("#percent").on("change", function (d) {
    scaleType = "p";
    renderAll();
});

d3.select("#start-date").on("change", function (d) {
    console.log(this.attributes.value.nodeValue);
    console.log(d3.select("#end-date").attr("value"));
    if (this.attributes.value.nodeValue <
        d3.select("#end-date").attr("value")) {
        
        prepareAll();
        renderAll();
    }
    else {
        alert("Start date needs to be before end date.");
    }
//     d3.select("#end-date);
//     prepareAll();
//     renderAll();
});

d3.select("#end-date").on("change", function (d) {
    if (this.attributes.value.nodeValue >
        d3.select("#start-date").attr("value")) {
        
        prepareAll();
        renderAll();
    }
    else {
        alert("Start date needs to be before end date.");
    }
    // TODO check date range!!!
//     prepareAll();
//     renderAll();
});

d3.select("#flip").on("click", function (d) {
    var tmp = left;
    left = right;
    right = tmp;
    prepareAll();
    renderAll();
});

function getRowNames() {
    var names = [];
    for (var i = 0; i < data[left].length; i++) {
        names.push(data[left][i].name);
    }
    return names.sort();
}

function getLineNames() {
    var names = [];
    for (var i = 0; i < data[right].length; i++) {
        names.push(data[right][i].name);
    }
    return names.sort();
}

function getVisibleColumnNames() {
    var names = [];
    rightList.selectAll("label.item input").each(function (d) {
        if (this.checked) names.push(d.name);
    });
    return names;
}

function getVisibleRowNames() {
    var names = [];
    leftList.selectAll("label.item input").each(function (d) {
        if (this.checked) names.push(d.name);
    });
    return names;
}

function renderAll() {
    renderLeftList();
    renderRightList();
    renderTable();
//     console.log(getLineNames());
}

function renderColumns(table) {
    var columns = getLineNames();
    var row = table.append("tr");
    row.append("td");
    for (var i = 0; i < columns.length; i++) {
        row.append("td")
            .text(columns[i])
            .classed(escape(columns[i]), true)
            .classed("header", true);
    }
}

function renderTable() {
    d3.select("#center .content").selectAll("table").remove();
    var table = d3.select("#center .content").append("table");
    renderColumns(table);
    renderRows(table);
}

function renderRows(table) {
//     showLoading();
//     d3.selectAll("#center .content div").remove();
    var names = getRowNames();
    for (var i = 0; i < names.length; i++) {
        renderRow(names[i], table);
    }
    displayRows();
    displayLines();
//     hideLoading();
}

function renderRow(rowName, table) {
    
    var a = rowName.split(/\s+/);
    var label = a[a.length - 1];
    
    var tr = table.append("tr")
        .attr("name", escape(rowName));
    tr.append("td")
        .style("transform", "rotate(-90deg)")
        .style("width", 25)
        .style("position", "relative")
//         .style("height", 10)
//         .style("background", "red")
//         .style("writing-mode", "vertical-rl")
        .classed("header", true)
        .append("div")
            .style("position", "absolute")
//             .style("top", -10)
//             .style("transform", "rotate(-90deg)")
            .text(label);
//     console.log(a[a.length - 1]);
    
    columns = getLineNames();
    
//     row = d3.select("#center .content")
//         .append("div")
//             .attr("class", "row")
//             .attr("name", rowName)
//             .style("display", "none");
//     row.append("h3")
//         .text(rowName);
    
    var rowData = filteredData[rowName];
//     console.log(yRange);
    
//     console.log(window.innerWidth);
    var width = Math.max(minWidth, (window.innerWidth - 100) / (getVisibleColumnNames().length));
    var height = Math.max(minHeight, (window.innerHeight - 100) / (getVisibleRowNames().length));
    var margin = { top: 5, left: 30, right: 5, bottom:25};
    
//     positive = row.append("svg").attr("class", "positive row-chart")
//         .append("g").attr("class", "chart");
//     renderChart(positive, rowData, lineNames, "positive", yRanges.sum, width,  height, margin);
//     negative = row.append("svg").attr("class", "negative row-chart")
//         .append("g").attr("class", "chart");
//     renderChart(negative, rowData, lineNames, "negative", yRanges.sum, width,  height, margin);
//     neutral = row.append("svg").attr("class", "neutral row-chart")
//         .append("g").attr("class", "chart");
//     renderChart(neutral, rowData, lineNames, "neutral", yRanges.sum, width,  height, margin);
    for (var i = 0; i < columns.length; i++) {
//         var td = tr.append("td").text("ddd");
        var impact = tr
            .append("td")
                .attr("class", escape(columns[i]))
                .append("svg")
                    .attr("class", "impact row-chart")
                    .style("width", width)
                    .style("height", height)
                    .append("g").attr("class", "chart");
//         var columnArray = [ columns[i] ];
        renderChart(impact, rowName, rowData, columns, i, "impact", yRanges.impact, width,  height, margin);
    }
}

function renderLeftList() {
    var items = data[left];
    items.sort(function(a,b) {
        return d3.ascending(a.name, b.name);
    });
    
    leftList.selectAll("label.item").remove();
    var changes = leftList.selectAll("label.item").data(items);
    var label = changes.enter().append("label").attr("class", "item");
    label.append("input")
        .attr("type", "checkbox")
        .attr("checked", true)
        .on("change", function(d) {
            renderTable();
        });
    label.append("span")
        .text(function(d) { return d.name; });
}

function renderRightList() {
    var items = data[right];
    items.sort(function(a,b) {
        return d3.ascending(a.name, b.name);
    });
    
    rightList.selectAll("label.item").remove();
    d3.selectAll("#expand").remove();
    d3.selectAll("#rightCharts").remove();
    var changes = rightList.selectAll("label.item").data(items);
    var label = changes.enter().append("label").attr("class", "item");
    label.append("input")
        .attr("type", "checkbox")
        .attr("checked", true)
        .on("change", function(d, i) {
            renderTable();
        });
    label.append("span")
        .text(function(d) { return d.name; })
        .attr("style", function(d) { return "color:" + colorScale(d.name); })
        .on("mouseenter", function(d, i) {
            highlightItem(d.name);
        })
        .on("mouseleave", function(d, i) {
            releaseItems(d.name);
        });
}

function expandClose() {
    var b = d3.select("#expand");
    if (!expanded) {
        b.attr("value", "Hide");
        expand();
    }
    else {
        b.attr("value", "Expand");
        close();
    }
    resize();
}

function expand() {
    d3.select("#rightCharts").style("display", "block");
    expanded = true;
}

function close() {
    d3.select("#rightCharts").style("display", "none");
    expanded = false;
}

function escape(name) {
    return name.replace(/ /g, "_").replace(/'/g, "_");
}

function highlightItem(name) {
    rightList.selectAll("label.item").classed("bold", function(e, j) {
        return e.name == name; });
    d3.selectAll(".dot_" + escape(name))
        .selectAll("circle")
        .attr("opacity", 1);
    d3.selectAll(".line_" + escape(name))
        .selectAll("path")
        .attr("opacity", 1);
}

function releaseItems(name) {
    rightList.selectAll("label.item").classed("bold", false);
    d3.selectAll(".dot_" + escape(name))
        .selectAll("circle")
        .attr("opacity", 0.5);
    d3.selectAll(".line_" + escape(name))
        .selectAll("path")
        .attr("opacity", 0.5);
}

function formatDay(date) {
    return monthNames[date.getMonth()] + " " + date.getDate() + " " + date.getFullYear();
}

function formatDate(date) {
    if (aggregation == "d") {
        return formatDay(date);
    }
    else if (aggregation == "w") {
        return formatDay(date) + " — " + formatDay(previousDay(nextWeek(date)));
    }
    else {
        return monthFullNames[date.getMonth()];
    }
}

function formatValue(s, count, type) {
    if (type == "impact") return s;
    else if (scaleType == "s") return s + "/" + count;
    else return Math.round(s / count * 100) + "%";
}

function resetDay(date) {
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    return date;
}

function resetMonth(date) {
    date = resetDay(date);
    date.setDate(1);
    return date;
}

function nextDay(date) {
    next = resetDay(new Date(date.getTime()));
    next.setDate(date.getDate() + 1);
    return next;
}

function previousDay(date) {
    prev = resetDay(new Date(date.getTime()));
    prev.setDate(date.getDate() - 1);
    return prev;
}

function nextWeek(date) {
    date = nextDay(date);
    while (date.getDay() != 1) {
        date = nextDay(date);
    }
    return date;
}

function nextMonth(date) {
    next = resetMonth(new Date(date.getTime()));
    next.setMonth(date.getMonth() + 1);
    return next;
}

function nextPeriod(date) {
    if (aggregation == "d") return nextDay(date);
    else if (aggregation == "w") return nextWeek(date);
    else return nextMonth(date);
}

function resetPeriod(date) {
    if (aggregation == "d") return resetDay(date);
    else if (aggregation == "w") return resetWeek(date);
    else return resetMonth(date);
}

function prepareData(tweets) {
    var startDate = new Date(d3.select("#start-date").node().value);
    var endDate = new Date(d3.select("#end-date").node().value);
    var date = startDate;
    
    var data = [];
    
    var i = 0;
    // Skip older than startDate
    while (i < tweets.length && new Date(tweets[i]['time']) < startDate) {
        i++;
    }
    
    var limit = nextPeriod(startDate);
    var period = startDate;
    while (i < tweets.length && new Date(tweets[i]['time']) <= endDate) {
        var datum = {};
        datum.count = datum.positive = datum.negative = datum.neutral = datum.impact = 0;
        while (i < tweets.length && new Date(tweets[i]['time']) < limit) {
            if (tweets[i]['sentiment'] < 0) datum.negative++;
            else if (tweets[i]['sentiment'] > 0) datum.positive++;
            else datum.neutral++;
            datum.count++;
            
            datum.impact += tweets[i]['impact'];
            i++;
        }
        datum.period = period;
        data.push(datum);
        period = limit;
        limit = nextPeriod(limit);
    }
    return data;
}

function prepareRowData(rowName, lines, sum) {
    
    var rowData;
    if (sum) {
        rowData = data[rowName];
    }
    else {
        rowData = data[left].filter(function(d) {
            return d.name == rowName;
        })[0];
    }
    
    var max = 0;
    var min = 0;
    var impactMax = 0;
    var impactMin = 0;
    filteredData[rowName] = []
    for (c = 0; c < lines.length; c++) {
        pd = prepareData(rowData[lines[c]]);
        filteredData[rowName].push(pd);
        var dmax = d3.max(pd, function(d){ return d.impact });
        var dmin = d3.min(pd, function(d){ return d.impact });
        impactMax = Math.max(impactMax, dmax == undefined ? impactMax : dmax);
        impactMin = Math.min(impactMin, dmin == undefined ? impactMin : dmin);
        if (scaleType == "s") {
            max = Math.max(max,
                    d3.max(pd, function(d){ return d.positive }),
                    d3.max(pd, function(d){ return d.negative }),
                    d3.max(pd, function(d){ return d.neutral })
                    );
        }
    }
    if (scaleType == "p") {
        min = 0;
        max = 1;
    }
    
    return {
        "impact": {
            "min": impactMin, "max": impactMax
        },
        "sum": {
            "min": min, "max": max
        }
    }
}

function renderChart(chart, rowName, rowData, lines, c, type, yRange, width, height, margin) {
    
    var innerWidth = width - margin.left - margin.right,
        innerHeight = height - margin.top - margin.bottom;

    var xScale = d3.time.scale().range([0, innerWidth]),
        yScale = d3.scale.linear().range([innerHeight, 0]);
    
//     chart.append("text")
//         .attr("text-anchor", "middle")
//         .text(titles[type])
//         .attr("x", function(d) {
//             return margin.left + (innerWidth / 2);
//         })
//         .attr("y", function(d) {
//             return margin.top - 20;
//         })
//         .attr("fill", "black")
//         .attr("font-family", "Lato")
//         .attr("font-size", "14px")
//         .attr("font-weight", "bold")
//         .attr("opacity", 0.75);
    
    var xAxisGroup = chart.append("g");
    var yAxisGroup = chart.append("g");
    
    xAxisGroup.attr("transform", "translate(" + margin.left + "," + (innerHeight + margin.top) + ")");
    yAxisGroup.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    
    xScale.domain([new Date(d3.select("#start-date").node().value), new Date(d3.select("#end-date").node().value)]);
    yScale.domain([yRange.min, yRange.max]);
    
    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("bottom")
        .ticks(d3.time.months, 1)
        .tickFormat(d3.time.format("%b"));
    xAxisGroup.call(xAxis);

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left")
        .ticks(3)
        .tickFormat(d3.format("s"));
    if (type != "impact" && scaleType == "p") yAxis.tickFormat(d3.format("%"));
    yAxisGroup.call(yAxis);
    
//     for (var c = 0; c < lines.length; c++) {
//         var line = lines[c];
//     console.log(filteredData);
    renderLine(chart, rowName,
               lines[c], c, type,
               rowData[c], xScale, yScale, margin);
//     }
    
}

function displayRows() {
    d3.selectAll("#left-dimension .content label.item input")
        .each(function (d) {
            var row = d3.select("tr[name=\"" + escape(d.name) + "\"]");
            row.classed("hidden", !this.checked);
        });
}

function displayLines() {
    d3.selectAll("#right-dimension .content label.item input")
        .each(function (d) {
            var column = d3.selectAll("td." + escape(d.name));
//             console.log(!this.checked);
            column.classed("hidden", !this.checked);
        });
}

function changeDisplay(line, v) {
    var chart = d3.selectAll("svg.row-chart");
//     chart.select("g." + "dot_" + line).style("opacity", v);
//     chart.select("g." + "line_" + line).style("opacity", v);
    chart.select("g." + "dot_" + escape(line)).style("display", v);
    chart.select("g." + "line_" + escape(line)).style("display", v);
}

function renderPie(chart, rowName, columnName, lineNum, period, width, height, margin) {
    var innerWidth = width - margin.left - margin.right,
        innerHeight = height - margin.top - margin.bottom;

    var color = d3.scale.ordinal()
        .range(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);

    var radius = 100;
        
    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    var labelArc = d3.svg.arc()
        .outerRadius(radius - 40)
        .innerRadius(radius - 40);
    
    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.count; });
//     console.log(rowName);
//     console.log(columnName);
//     console.log(filteredData[rowName][lineNum]);
    var g = chart.selectAll(".arc")
            .data(pie([
                { "type": "positive", "count": filteredData[rowName][lineNum][period]["positive"]},
                { "type": "negative", "count": filteredData[rowName][lineNum][period]["negative"]},
                { "type": "neutral", "count": filteredData[rowName][lineNum][period]["neutral"]}
                      ]))
        .enter().append("g")
            .attr("class", "arc");

        g.attr("transform", function(d) { return "translate(" + width / 2 + ", " + height / 2 + ")"; })
        
        g.append("path")
            .attr("d", arc)
            .style("fill", function(d) {
                if (d.data.type == "positive") return "green";
                else if (d.data.type == "negative") return "red";
                else return "grey";
            });

        g.append("text")
            .attr("transform", function(d) { return "translate(" + labelArc.centroid(d) + ")"; })
            .attr("dy", ".35em")
            .text(function(d) { return d.data.count; });
}

function renderLine(chart, rowName, line, lineNum, type, data, xScale, yScale, margin) {
    
    var dotGroup = chart.append("g")
        .attr("class", "dot_" + escape(line))
        .attr("name", line);
    var lineGroup = chart.append("g")
        .attr("class", "line_" + escape(line))
        .attr("name", line);
    
    dotGroup.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    lineGroup.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    
    var dataPoints = dotGroup.selectAll("circle")
        .data(data, function(d) { return d.period; });

    dataPoints.enter()
        .append("circle")
        .attr("r", dotSize)
        .attr("cx", function(d, i) { return xScale(d.period); })
        .attr("cy", function(d, i) {
            if (type != "impact" && scaleType == "p") return d.count == 0 ? yScale(0) : yScale(d[type] / d.count);
            else return yScale(d[type]);
        })
        .attr("fill", function(d, i) { return colorScale(line) })
        .attr("opacity", 0.5)
        .on("mouseenter", function(d, i) {
            this.attributes.r.value = dotEmph;
            highlightItem(this.parentNode.attributes.name.nodeValue);
//             window.innerWidth;
            var y = d3.event.pageY + 15;
            var x = d3.event.pageX + 15;
            if (x + 300 > window.innerWidth) x -= 330;
            if (y + 200 > window.innerHeight) y -= (d.count > 0 ? 270 : 80);
            var tooltip = d3.select("#tooltip").style({
                visibility: "visible",
                top: y,
                left: x,
                opacity: 0.9
            });
            d3.select("#tooltip-period").text(formatDate(d.period));
            d3.select("#tooltip-count").text(formatValue(d[type], d.count, type));
            tooltip.select("svg").remove();
            tooltip.select(".nodata").remove();
            if (d.count > 0) {
                var pie = tooltip.append("svg").attr("width", 300).attr("height", 200);
                renderPie(pie, rowName, line, lineNum, i, 300, 200, { top: 20, left: 100, right: 20, bottom:20});
            }
        })
        .on("mouseleave", function(d, i) {
            this.attributes.r.value = dotSize;
            releaseItems(this.parentNode.attributes.name.nodeValue);
            d3.select("#tooltip").style({visibility: "hidden", opacity: 0});
        })
        .on("click", function(d, i) {
//             console.log(tweets);
//             var t = d3.select("#tweets .content");
            if (d.count > 0) {
                d3.select("#tweets").remove();
                var t = d3.select("body")
                    .append("div").attr("id", "tweets")
                        .append("div").attr("class", "content");
                t.selectAll("*").remove();
                t.append("a").text("CLOSE").on("click", function() {
                    d3.select("#tweets").remove();
                }).attr("class", "link");
                t.append("h2").text(rowName + " v. " + line);
                t.append("p").text(formatDate(d.period));
                renderTweets(t, "positive", rowName, line,
                             d.period);
                renderTweets(t, "negative", rowName, line,
                             d.period);
            }
        });
    var lineFunction = d3.svg.line()
        .x(function(d) { return xScale(d.period); })
        .y(function(d) {
            if (type != "impact" && scaleType == "p") return d.count == 0 ? yScale(0) : yScale(d[type] / d.count);
            else return yScale(d[type]);
        })
        .interpolate("linear");
    lineGroup.append("path")
        .attr("d", lineFunction(data))
        .attr("stroke", colorScale(line))
        .attr("stroke-width", strokeWidth)
        .attr("fill", "none")
        .attr("opacity", 0.5)
        .on("mouseenter", function(d, i) {
            highlightItem(this.parentNode.attributes.name.nodeValue);
        })
        .on("mouseleave", function(d, i) {
            releaseItems(this.parentNode.attributes.name.nodeValue);
        });
}

function renderTweets(t, type, row, column, period, impact) {
    var network = (left == "channels") ? row : column;
    var candidate = (left == "candidates") ? row : column;
    var info = t.append("div").selectAll(".tweet")
        .data(tweets)
        .enter().append("div")
            .attr("class", type)
            .filter(function(d) {
                return d.network == network &&
                    d.candidates.includes(candidate) &&
                    d.time >= period.getTime() &&
                    d.time < nextPeriod(period).getTime() &&
                    (type == "positive" ? d.sentiment > 0 : d.sentiment < 0);
            })
            .sort(function(a,b) {
                return d3.descending(a.retweets, b.retweets);
            })
            .filter(function(d, i) {
                return i < 3;
            })
            .classed("tweet", true)
            .text(function(d) { return d.text; })
            .append("div");
    info.append("div")
        .classed("info", true)
        .text(function(d) {
            console.log(d);
            return "Sentiment: " + d.sentiment;
        });
    info.append("div")
        .classed("info", true)
        .text(function(d) {
            console.log(d);
            return "RT: " + d.retweets;
        });
}
