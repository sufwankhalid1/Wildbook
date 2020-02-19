//Abstract class defining funcitonality for all d3 graph types
class GraphAbstract { //See static attributes below class    
    constructor(individualID, containerId) {
	this.id = individualID;
	this.containerId = containerId;
	
	//Unique global id
	this.graphId = GraphAbstract.numGraphs++;

	//SVG Attributes
	this.svg;
	this.width = 1150;
	this.height = 500;
	this.margin = {top: 20, right: 120, bottom: 20, left: 120};

	//Top-level G Attrbiutes
	this.gWidth = this.width - this.margin.right - this.margin.left,
	this.gHeight = this.height - this.margin.top - this.margin.bottom;

	//Node Attributes
	this.numNodes;
	this.radius;
	this.maxRadius = 50;
	this.scalingFactor = 25; //TODO: Tune this value
	this.nodeMargin = 15;
	this.nodeSeparation;
	this.transitionDuration = 750;

	this.strokeWidth = 3.5;
	
	this.fontSize = 9;
	this.focusedScale = 1;

	this.alphaSymbSize = 200; //TODO: Figure out the units on this...

	//Node Style Attributes
	this.defGenderColor = "#7f7f7f"; 
	this.maleColor = "steelblue";
	this.femaleColor = "palevioletred";

	this.alphaColor = "#bf0000";

	this.defNodeColor = "#ffffff";
	this.fixedNodeColor = "#cccccc";	
	
	this.defLinkColor = "#a6a6a6";
	this.famLinkColor = "#a6a6a6"; //"#b59eda";
	this.maternalLinkColor = "#f3acd0";
	this.paternalLinkColor = "#8facc6";
	
	//Zoom Attributes
	this.zoomFactor = 1000;
	this.zoom = d3.zoom()
	    .scaleExtent([0.5, 5])
	    .translateExtent([
		[-this.gWidth * 2, -this.gHeight * 2],
		[this.gWidth * 2, this.gHeight * 2]
	    ])
	    .wheelDelta(() => this.wheelDelta());

	//Tooltip Attributes
	this.popup = false;
	this.fadeDuration = 200;
	this.tooltipOpacity = 0.9;
	this.maxDisplay = 4;
	
	//Legend Attributes
	this.legendMargin = {"top": 25, "right": 25, "bottom": 25, "left": 25}
        this.legendDimensions = {"width": 265, "height": 200};
	this.legendOffset = {"horiz": this.width - (this.legendDimensions.width + this.legendMargin.right),
			     "vert": this.legendMargin.top};
	this.legendNodeColors = [this.maleColor, this.femaleColor, this.defGenderColor];
	this.legendLinkColors = [this.paternalLinkColor, this.maternalLinkColor, this.famLinkColor];
	this.legendLabels = ["Male Sex", "Female Sex", "Unknown Sex", "Alpha Role",
			     "Organism", "Paternal Relationship", "Maternal Relationship",
			     "Familial Relationship", "Member Relationship"];
	this.legendIcons = {"size": 15, "margin": 8, "mSize": 23};
	this.legendStrokeWidth = 2;

	//Filter Attributes
	this.validFamilyFilters = ["selectFamily", "filterFamily"]
	this.validCheckFilters = ["male", "female", "unknownGender", "alpha", "unknownRole"];
	this.validFilters = this.validFamilyFilters.concat(this.validCheckFilters);	
    }

    
    // Render Methods //
    
   /**
     * Display the data table context of this graph
     * @param {diagramRef} [string] - HTML reference to the graph diagram
     * @param {tableRef} [string] - HTML reference to the graph table
     */	
    showTable(diagramRef, tableRef) {
	//Display tableRef and hide diagramRef
	$(diagramRef).hide();
	$(tableRef).show();
	$(diagramRef).removeClass("active");
	$(tableRef).addClass("active");

	//Report incomplete information
	this.incompleteInfoMessage(diagramRef);
    }
	
   /**
     * Perform all auxiliary functions necessary prior to graphing
     * @param {linkData} [Link array] - Relationship link list
     * @param {nodeData} [Node array] - MarkedIndividual node list
     */	
    setupGraph(linkData, nodeData) {
	//Establish link/node context
	this.linkData = linkData;
	this.nodeData = nodeData;

	//Add default elements
	this.addSvg();
	this.addLegend();
	this.addTooltip();

	//Assess graphical sizings
	this.setNodeRadius();

	//Initialize filter button functionalities
	this.updateFilterButtons();

	this.addHideButton();
    }
    
    //TODO - Finish comment
    /**
     * Append top-level SVG containing all graph elements
     * @param {containerId} [String] - The HTML reference to append this SVG to
     */	
    addSvg(containerId=this.containerId) {
	this.svg = d3.select(containerId).append("svg")
	    .attr("class", "container")
	    .attr("width", this.width)
	    .attr("height", this.height)
	    .call(this.zoom.on("zoom", () => {
		this.svg.attr("transform", d3.event.transform)
	    }))
	    .on("dblclick.zoom", null) //Disable double click zoom
	    .append("g");
    }

    //TODO - Modularize
    /**
     * Append graph legend to top-level SVG
     * @param {containerId} [String] - The HTML reference to append this SVG to
     * @param {rowsPerCol} [int] - The number of rows to display per legend column
     * @param {rowSpacing} [int] - The spacing in px between rows
     */	
    addLegend(containerId=this.containerId, rowsPerCol=5, rowSpacing=120) {
	//Append the legend group
	let legendRef = d3.select(containerId + " svg").append("g")
	    .attr("class", "legend")
	    .attr("id", "legendRef")
	    .attr("height", this.legendHeight)
	    .attr("width", this.legendWidth)
	    .attr("transform", "translate(" + this.legendOffset.horiz  + "," + this.legendOffset.vert + ")")

	//Initialize position references
	let xIdx = 0, yIdx = 0;

	//TODO - Consider adding strong repulsive force to legend
	//Add legend gender color-key
	legendRef.selectAll("rect")
	    .data(this.legendNodeColors).enter()
            .append("rect")
            .attr("x", () => Math.floor(xIdx++ / rowsPerCol) * rowSpacing)
            .attr("y", () => (yIdx++ % rowsPerCol) * this.legendIcons.mSize)
            .attr("width", this.legendIcons.size)
            .attr("height", this.legendIcons.size)
            .attr("fill", d => d);

	//Add alpha symbol example
	legendRef.append("circle")
            .attr("cx", (Math.floor(xIdx++ / rowsPerCol) * rowSpacing) + this.legendIcons.size / 2)
            .attr("cy", ((yIdx++ % rowsPerCol) * this.legendIcons.mSize) + this.legendIcons.size / 2)
            .attr("r", this.legendIcons.size / 2)
            .attr("fill", this.alphaColor)
	
	//Add organism circle example
	legendRef.append("circle")
            .attr("cx", (Math.floor(xIdx++ / rowsPerCol) * rowSpacing) + this.legendIcons.size / 2)
            .attr("cy", ((yIdx++ % rowsPerCol) * this.legendIcons.mSize) + this.legendIcons.size / 2)
            .attr("r", this.legendIcons.size / 2)
            .attr("fill", this.defNodeColor)
            .attr("stroke", this.defGenderColor)
	    .attr('stroke-width', this.legendStrokeWidth);
	
	//Add familial relationship links
	this.legendLinkColors.forEach(color => {
	    let x = Math.floor(xIdx++ / rowsPerCol) * rowSpacing;
	    let y = (yIdx++ % rowsPerCol) * this.legendIcons.mSize;
	    	this.drawLegendArrow(legendRef, x, y, color);
	});

	//Add member-member relationship link
	legendRef.append("line")
	    .attr("x1", Math.floor(xIdx / rowsPerCol) * rowSpacing)
	    .attr("x2", (Math.floor(xIdx++ / rowsPerCol) * rowSpacing) + this.legendIcons.size)
	    .attr("y1", (yIdx % rowsPerCol) * this.legendIcons.mSize + (this.legendIcons.size / 2))
	    .attr("y2", (yIdx++ % rowsPerCol) * this.legendIcons.mSize + (this.legendIcons.size / 2))
	    .attr("stroke", this.defLinkColor)
	    .attr("stroke-width", this.legendStrokeWidth);

	//Add legend labels
	legendRef.selectAll("text")
            .data(this.legendLabels).enter()
            .append("text")
            .attr("x", (d, i) => (Math.floor(i / rowsPerCol) * rowSpacing) + this.legendIcons.mSize)
            .attr("y", (d, i) => ((i % rowsPerCol) * this.legendIcons.mSize) + this.legendIcons.size)
            .text(d => d);
    }
    
    /**
     * Append a tooltip to the top-level SVG, used to visualize node info on hover
     * @param {containerId} [String] - The HTML reference to append this SVG to
     */	
    addTooltip(containerId=this.containerId) {
	//Define the tooltip div
	this.tooltip = d3.select(containerId).append("div")
	    .attr("class", "tooltip")				
	    .style("opacity", 0);
    }

    /**
     * Draw each node with prescribed radius, fill, and outline
     * @param {newNodes} [Node list] - A list of newly created Node elements
     * @param {activeNodes} [Node list] - A list of previously existing, non-filtered Node elements
     */	
    updateNodeOutlines(newNodes, activeNodes) {
	//Create new node outlines
	newNodes.append("circle")
	    .attr("r", this.startingRadius)
	    .style("fill", this.defNodeColor)
	    .style("stroke", d => this.colorGender(d))
	    .style("stroke-width", d => this.strokeWidth * this.getSizeScalar(d));

	//Scale node radius and stroke width
	activeNodes.selectAll("circle").transition()
	    .duration(this.transitionDuration)
	    .attr("r", d => this.radius * this.getSizeScalar(d))
	    .style("stroke-width", d => this.strokeWidth * this.getSizeScalar(d) + "px");
    }

    /**
     * Return a color based upon the given node's gender
     * @param {node} [Node] - A Node element
     * @return {gender} - The gender of the contextual Node element
     */	
    colorGender(node) {
	try {
	    let gender = node.data.gender || "default";
	    switch (gender.toUpperCase()) {
	        case "FEMALE": return this.femaleColor; 
	        case "MALE": return this.maleColor; 
	        default: return this.defGenderColor; 
	    }
	}
	catch(error) {
	    console.error(error);
	}
    }

    /**
     * Draw alpha symbols for all given nodes with isAlpha attribute
     * @param {newNodes} [Node list] - A list of newly created Node elements
     * @param {activeNodes} [Node list] - A list of previously existing, non-filtered Node elements
     */	
    updateNodeSymbols(newNodes, activeNodes) {
	//Add new node symbols
	newNodes.append("path")
	    .attr("class", "symb")
	    .attr("d", d => {
		return d3.symbol().type(d3.symbolCircle)
		    .size(() => {
			if (d.data.role && d.data.role.toUpperCase() == "ALPHA")
			    return this.alphaSymbSize * this.getSizeScalar(d);
			else return 0;
		    })();
	    })
	    .style("fill", this.alphaColor)
	    .style("fill-opacity", 0);

	//Update node symbols
	activeNodes.selectAll(".symb").transition()
	    .duration(this.transitionDuration)
	    .style("fill-opacity", 1)
	    .attr("d", d => {
		return d3.symbol().type(d3.symbolCircle)
		    .size(() => {
			if (d.data.role && d.data.role.toUpperCase() == "ALPHA")
			    return this.alphaSymbSize * this.getSizeScalar(d);
			else return 0;
		    })();
	    })
	    .attr("transform", d => {
		let radialPos = Math.cos(Math.PI / 4);
		let pos = this.radius * this.getSizeScalar(d) * radialPos;
		return "translate(" + pos + "," + -pos + ")";
	    })
    }

    /**
     * Add text to the given nodes
     * @param {newNodes} [Node list] - A list of newly created Node elements
     * @param {activeNodes} [Node list] - A list of previously existing, non-filtered Node elements
     */	
    updateNodeText(newNodes, activeNodes) {
	//Add new node text
	newNodes.append("text")
	    .attr("class", "text")
	    .attr("dy", ".5em") //Vertically centered
	    .text(d => this.truncateText(d))
	    .style("font-size", d => (this.fontSize * this.getSizeScalar(d)) + "px")
	    .style("font-weight", d => d.data.isFocused ? "bold" : "normal")
	    .style("stroke-opacity", 0);

	//Update node text
	activeNodes.selectAll("text").transition()
	    .duration(this.transitionDuration)
	    .style("font-size", d => (this.fontSize * this.getSizeScalar(d)) + "px")
	    .style("font-weight", d => d.data.isFocused ? "bold" : "normal")
	    .style("stroke-opacity", 1);

    }

    // Helper Methods //

    /**
     * Truncate text to fit inside a given node
     * @param {node} [Node] - A Node element
     * @return {truncatedText} [String] - A truncated string, guarenteed to fit within the Node element
     */	
    truncateText(node) {
	let nodeLen = (this.radius * 2) / 5;
	let words = node.data.name.split(" ");

	let text = "";
	for (let word of words) {
	    text += word + " ";
	    if (text.length > nodeLen) {
		text = text.slice(0, nodeLen - 3) + "...";
		break;
	    }
	}

	return text.trim();
    }

    /**
     * Modify zoom wheel delta to smooth zooming
     */	
    wheelDelta() {
	return -d3.event.deltaY * (d3.event.deltaMode ? 120 : 1 ) / this.zoomFactor;
    }
    
    /**
     * Display message on missing data
     * @param {diagramId} [Node] - An HTML reference dictating where the info message should be appended
     */	
    incompleteInfoMessage(diagramId) {
	$(diagramId).html("<h4 id='incompleteInfoMsg'>There are currently no known " +
			  "relationships for this Marked Individual</h4>")
    }

    /**
     * Sets the radius attribute for a given node
     * @param {nodeData} [Node list] - A list of Node elements
     */	
    setNodeRadius(nodeData=this.nodeData) {
	this.calcNodeSize(nodeData);
	nodeData.forEach(d => {
	    d.data.r = this.radius * this.getSizeScalar(d);
	});
    }

    /**
     * Calculate node size s.t. all nodes can fit in the contextual SVG
     * @param {nodeData} [Node list] - A list of Node elements
     */	
    calcNodeSize(nodeData=this.nodeData) {
	try {
	    let numNodes = nodeData.length || 10; //Default node length is 10 
	    this.radius = this.maxRadius * Math.pow(Math.E, -(numNodes / this.scalingFactor));
	}
	catch(error) {
	    console.error(error);
	}
    }

    /**
     * Returns the color for a given link
     * @param {link} [Link] - A Link element
     */	
    getLinkColor(link) {
	if (link) {
	    switch(link.type) {
	    case "familial":
		return this.famLinkColor;
	    case "paternal":
		return this.paternalLinkColor;
	    case "maternal":
		return this.maternalLinkColor;
	    }
	}
	
	return this.defLinkColor;
    }

    /**
     * Return a size multiple if the given node is focused, defaults to 1 
     * @param {node} [Node] - A Node element
     */	
    getSizeScalar(node) {
	if (node && node.data && node.data.isFocused) return this.focusedScale;
	else return 1;
    }

    /**
     * Display the selected tooltip type
     * @param {d} [Node|Link] - A Node or Link element
     * @param {type} [String] - Determines whether a "node" or "link" tooltip should be displayed
     */	
    handleMouseOver(d, type) {
	if (!this.popup) {
	    if (type === "node") this.displayNodeTooltip(d);
	    else if (type === "link") this.displayLinkTooltip(d);
	    else return;

	    //Display opaque tooltip
	    this.tooltip.transition()
		.duration(this.fadeDuration)
		.style("opacity", this.tooltipOpacity);
	    
	    //Prevent future mouseOver events
	    this.popup = true;
	}
    }

    /**
     * Displays the node tooltip
     * @param {d} [Node] - A Node element
     */	
    displayNodeTooltip(d) {
	//Place tooltip offset to the upper right of the hovered node
	let text = this.generateNodeTooltipHtml(d);
	if (text) {
	    let halfRadius = (this.radius * this.getSizeScalar(d)) / 2;
	    this.tooltip
		.style("left", d3.event.layerX +  halfRadius + "px")	
		.style("top", d3.event.layerY - halfRadius + "px")
		.style("background-color", "#808080")
		.html(text);
	}
    }

    //TODO - Modularize this
    /**
     * Generate the tooltip description for a given node
     * @param {d} [Node] - A Node element
     * @return {tooltipHTML} [String] - An HTML string to use as a tooltip display
     */	
    generateNodeTooltipHtml(d) {
	let tooltipHtml = "<b>Name: </b>" + d.data.name + "<br/>";
	if (d.data.gender)
	    tooltipHtml += "<b>Sex: </b>" + d.data.gender + "<br/>";
	if (d.data.role)
	    tooltipHtml += "<b>Role: </b>" + d.data.role + "<br/>";
	if (d.data.currLifeStage)
	    tooltipHtml += "<b>Current Life Stage: </b>" + d.data.currLifeStage + "<br/>";
	if (d.data.isDead !== null) {
	    let livingStatus = (d.data.isDead ? "dead" : "alive");
	    tooltipHtml += "<b>Living Status: </b>" +  livingStatus + "<br/>";
	}
	if (d.data.firstSighting)
	    tooltipHtml += "<b>First Seen: </b>" + d.data.firstSighting + "<br/>";
	if (d.data.latestSighting)
	    tooltipHtml += "<b>Last Seen: </b>" + d.data.latestSighting + "<br/>";
	if (d.data.timeOfBirth)
	    tooltipHtml += "<b>Birth Date: </b>" + d.data.timeOfBirth + "<br/>";
	if (d.data.timeOfDeath)
	    tooltipHtml += "<b>Death Date: </b>" + d.data.timeOfDeath;
	return tooltipHtml;
    }
    
    /**
     * Display the link tooltip
     * @param {d} [Link] - A Link element
     */	
    displayLinkTooltip(d) {
	//Place tooltip on the hovered link
	let text = this.generateLinkTooltipHtml(d);
	
	if (text) {
	    this.tooltip
		.style("left", d3.event.layerX +  "px")	
		.style("top", d3.event.layerY + "px")
		.style("background-color", "#7997a1")
		.html(text);
	}
    }
    
    /**
     * Generate the tooltip description for a given link
     * @param {link} [Link] - A Link element
     */	
    generateLinkTooltipHtml(link) {
	let target = (link.target.data.individualID === this.id) ? "source" : "target";

	let tooltipHtml = "";
	let totalLinks = link.count, numLinks = 0;
	for (let enc of link.validEncounters) {
	    let time = enc.time;
	    let loc = enc.location;

	    //Limit the total number of links displayed to {this.maxDisplay}
	    if (numLinks >= this.maxDisplay) {
		if (numLinks < totalLinks)
		    tooltipHtml += "... " + (totalLinks - numLinks) + " more ...";
		break;
	    }
	    
	    if (time)
		tooltipHtml += "<b>Date: </b>" + time.day + "/" + time.month + "/" + time.year + " ";
	    if (loc && typeof loc.lat == "number")
		tooltipHtml += "<b>Longitude: </b>" + loc.lon + " ";
	    if (loc && typeof loc.lat == "number")
		tooltipHtml += "<b>Latitude: </b>" + loc.lat + "<br/>";

	    numLinks++;
	}
	
	return tooltipHtml;
    }

    /**
     * Fade the tooltip from view when no longer hovering over a node
     */	
    handleMouseOut() {
	//Enable future mouseOver events
	this.popup = false;

	//Fade tooltip from view
	this.tooltip.transition()		
            .duration(this.fadeDuration)		
            .style("opacity", 0);
    }

    /**
     * Draws a simple arrow (used for the legend arrow icons)
     * @param {legendRef} [String] - The HTML element to append the legend arrow definitions
     * @param {x} [int] - The x position of the legend arrow
     * @param {y} [int] - The y position of the legend arrow
     * @param {color} [String] - The hex color to use for the legend arrow 
     */	
    drawLegendArrow(legendRef, x, y, color){
	//Calculate median line
	let yCenter = y + (this.legendIcons.size / 2)

	//Draw a horizontal line component
	legendRef.append("line")
	    .attr("x1", x)
	    .attr("x2", x + this.legendIcons.size)
	    .attr("y1", yCenter)
	    .attr("y2", yCenter)
	    .attr("stroke", color)
	    .attr('stroke-width', this.legendStrokeWidth);

	//Draw the top half of the chevron arrowhead
	legendRef.append("line")
	    .attr("x1", x + (this.legendIcons.size * 3 / 4))
	    .attr("x2", x + this.legendIcons.size)
	    .attr("y1", yCenter - 5)
	    .attr("y2", yCenter).attr("stroke", color)
	    .attr('stroke-width', this.legendStrokeWidth);

	//Draw the bottom half of the chevron arrowhead
	legendRef.append("line")
	    .attr("x1", x + (this.legendIcons.size * 3 / 4))
	    .attr("x2", x + this.legendIcons.size)
	    .attr("y1", yCenter + 5)
	    .attr("y2", yCenter)
	    .attr("stroke", color)
	    .attr('stroke-width', this.legendStrokeWidth);
    }
    
    /**
     * Abstract funciton serving to update known filter buttons with relevant filters
     * @param {containerId} [String] - The HTML element to append the filter buttons
     */	
    updateFilterButtons(containerId=this.containerId) {
	//Select family filter
	$(containerId).find("#selectFamily").on("click", () => {
	    let groupNum = this.focusedNode.group;
	    let filter = (d) => d.group === groupNum;
	    this.filterGraph(groupNum, filter, filter, "selectFamily",
			     this.validFamilyFilters);
	});

	//Filter family filter
	$(containerId).find("#filterFamily").on("click", () => {
	    let groupNum = this.focusedNode.group;
	    let nodeFilter = (d) => d.group !== groupNum;
	    let linkFilter = (d) => (d.source.group !== groupNum &&
				     d.target.group !== groupNum);
	    this.filterGraph(groupNum, nodeFilter, linkFilter, "filterFamily",
			     this.validFamilyFilters);
	});

	//Reset filter
	$(containerId).find("#reset").on("click", () => this.resetGraph() && false);

	//Male filter
	this.createCheckBoxFilter(containerId, "male",
				  (d) => d.data.gender !== "male");

	//Female filter
	this.createCheckBoxFilter(containerId, "female",
				  (d) => d.data.gender !== "female");

	//Unknown gender filter
	this.createCheckBoxFilter(containerId, "unknownGender",
				  (d) => d.data.gender !== "unknown");

	//Alpha role filter
	this.createCheckBoxFilter(containerId, "alpha", (d) => d.data.role !== "alpha");

	//Unknown role filter
	this.createCheckBoxFilter(containerId, "unknownRole", (d) => d.data.role);

	//zoom in
	$(containerId).find("#ocZoomIn").on("click", () => this.zoomIn());


	//zoom out
	$(containerId).find("#ocZoomOut").on("click", () => this.zoomOut());


	//zoom in
	$(containerId).find("#reZoomIn").on("click", () => this.zoomIn());


	//zoom out
	$(containerId).find("#reZoomOut").on("click", () => this.zoomOut());
    }

    /**
     * Zoom in when button is pressed
     */	
    zoomIn(){
	console.log("in zoomIn()");
	this.zoom.scaleBy(this.svg.transition().duration(750), 1.3);
    }

    /**
     * Zoom out when button is pressed
     */	
    zoomOut(){
	console.log("in zoomOut()");
	this.zoom.scaleBy(this.svg.transition().duration(750), 1 / 1.3);
    }

    /**
     * Abstract funciton serving to update known filter buttons with relevant filters
     * @param {containerId} [String] - The HTML element to use as the root search node
     * @param {filterRef} [String] - The desired check box for which the onclick filter will be declared
     * @param {filter} [function] - The filter function to be applied
     */	
    createCheckBoxFilter(containerId, filterRef, filter) {
	$(containerId).find("#" + filterRef + "Box").on("click", (e) => {
	    let nodeRef = $(containerId).find("#" + filterRef + "Box");
	    if (nodeRef.is(":checked")) {
		nodeRef.closest("label").css("background", this.fixedNodeColor);
	    }
	    else {
		nodeRef.closest("label").css("background", this.defNodeColor);
	    }

	    this.filterGraph(0, filter, (d) => true, filterRef, this.validFilters);
	});
    }


    //TODO - Check CSS rules and speed up fades
    /**
     * Append a hide button to the graph
     */	
    addHideButton(){
	var shown = true;
        let hidebutton = document.createElement("button");
        hidebutton.innerText = 'Hide Legend';

	let legend = $(this.containerId).find(".legend")[0];
	if (!legend) {
	    this.addLegend();
	    legend = $(this.containerId).find(".legend")[0];
	}
	
        $(this.containerId).append(hidebutton);
        hidebutton.addEventListener("click", () => {
            if (shown) {
		legend.style.opacity = "0";
		hidebutton.innerText = "Show Legend";
            }
            else {
		legend.style.opacity = "1";
		hidebutton.innerText = "Hide Legend";
            }
	    shown = !shown;
        });
    }
		
    /**
     * Reset all filtered checkboxes
     * @param {containerId} [String] - The HTML element to use as the root search node
     */	
    uncheckBoxFilters(containerId) {
	this.validCheckFilters.forEach(filterRef => {
	    let nodeRef = $(containerId).find("#" + filterRef + "Box");
	    nodeRef.closest("label").css("background", this.defNodeColor);
	    nodeRef.prop("checked", false);
	});
    }
}

//Static attributes
GraphAbstract.numGraphs = 0;

