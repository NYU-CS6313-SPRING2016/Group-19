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
