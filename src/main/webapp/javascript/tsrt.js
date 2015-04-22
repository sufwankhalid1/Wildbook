

function SortTable(opts) {
	this.opts = opts;

	//some defaults
	if (!this.opts.start) this.opts.start = 0;
	if (!this.opts.howMany) this.opts.howMany = 10;
	if (!this.opts.reverse) this.opts.reverse = false;

	var me = this;

	this.results = [];

	this.sorts = [];
	this.sortsInd = [];
	this.values = [];
	this.searchValues = [];

	this.matchesFilter = [];

	this._sortCache = [];
	this._sortCacheRev = [];

	this.sortCol = -1;
	this.sortReverse = false;

	this.counts = { total: 0 };

	this.computeCounts = function() {
		this.counts.total = this.matchesFilter.length;
	};

	this.displayCounts = function() {
		if (!this.opts.countClass) return;
		for (var w in this.counts) {
			$('.' + this.opts.countClass + '.count-' + w).html(this.counts[w]);
		}
	};

	this.displayPagePosition = function() {
		if (!this.opts.pageInfoEl) return;
		if (this.matchesFilter.length < 1) {
			this.opts.pageInfoEl.html('<b>no matches found</b>');
			return;
		}
		var max = this.opts.start + this.opts.howMany;
		if (this.matchesFilter.length < max) max = this.matchesFilter.length;
		this.opts.pageInfoEl.html((this.opts.start+1) + ' - ' + max + ' of ' + this.matchesFilter.length);
	};

	this.newSlice = function(col, reverse) {
///console.warn('newSlice() col %o | start %o | start + howMany %o | reverse %o', col, this.opts.start, this.opts.start + this.opts.howMany, reverse);
		this.results = this.slice(col, this.opts.start, this.opts.start + this.opts.howMany, reverse);
	};

	this.nudge = function(n) {
		this.opts.start += n;
		if ((this.opts.start + this.opts.howMany) > this.matchesFilter.length) this.opts.start = this.matchesFilter.length - this.opts.howMany;
		if (this.opts.start < 0) this.opts.start = 0;
console.log('nudge() start -> %d', this.opts.start);
		this.newSlice(this.sortCol, this.sortReverse);
		this.show();
	};

	this.tableDn = function() { return nudge(-1); }
	this.tableUp = function() { return nudge(1); }


	this.applyFilter = function(t) {
		//if (!this.opts.filterEl) return;
		//var t = filterEl.val();
console.log(t);
		this.filter(t);
		this.opts.start = 0;
		this.newSlice(1);
		this.show();
		this.computeCounts();
		this.displayCounts();
	};

	this.init = function() {
		this.opts.tableEl.addClass('tablesorter').addClass('pageableTable');
		var th = '<thead><tr>';
		for (var c = 0 ; c < this.opts.columns.length ; c++) {
			var cls = 'ptcol-' + this.opts.columns[c].key;
			if (!this.opts.columns[c].nosort) {
				if (this.sortCol < 0) { //init
					this.sortCol = c;
					cls += ' headerSortUp';
				}
				//cls += ' header" onClick="return headerClick(event, ' + c + ');';
				cls += ' header can-sort';
			}
			th += '<th class="' + cls + '" data-colnum="' + c + '">' + this.opts.columns[c].label + '</th>';
		}
		this.opts.tableEl.append(th + '</tr></thead>');
		for (var i = 0 ; i < this.opts.howMany ; i++) {
			var r = '<tr class="pageableTable-visible">';
			for (var c = 0 ; c < this.opts.columns.length ; c++) {
				r += '<td class="ptcol-' + this.opts.columns[c].key + '"></td>';
			}
			r += '</tr>';
			this.opts.tableEl.append(r);
		}

		this.initSort();
		this.initValues();
		this.newSlice(this.sortCol);

	//$('#progress').hide();
		this.sliderInit();
		this.show();
		this.computeCounts();
		this.displayCounts();

		this.opts.tableEl.find('th.can-sort').click(function(ev) { me.headerClick(ev, this); });

/*
	$('#results-table').on('mousewheel', function(ev) {  //firefox? DOMMouseScroll
		if (!sTable.opts.sliderEl) return;
		ev.preventDefault();
		var delta = Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail)));
		if (delta != 0) nudge(-delta);
	});
*/

	};

	this.initSort = function() {
		for (var c = 0 ; c < this.opts.columns.length ; c++) {
			var s = [];
			for (var i = 0 ; i < this.opts.data.length ; i++) {
				s.push(this.sortValueAt(this.opts.data[i], c) + '       ' + i);
			}
			s.sort(this.opts.columns[c].sortFunction);
			var si = [];
			for (var i = 0 ; i < s.length ; i++) {
				s[i] = s[i].slice(-7) - 0;
				si[s[i]] = i;
			}
			this.sorts[c] = s;
			this.sortsInd[c] = si;
		}
	};


	//TODO lazyloaded values
	this.initValues = function() {
		for (var i = 0 ; i < this.opts.data.length ; i++) {
			this.values[i] = [];
			this.searchValues[i] = '';
			this.matchesFilter.push(i);
			for (var c = 0 ; c < this.opts.columns.length ; c++) {
				var val = this.valueAt(this.opts.data[i], c);
				this.values[i].push(val);
				this.searchValues[i] += val + ' ';
			}
		}
	};


	this.headerClick = function(ev, el) {
		var c = parseInt(el.getAttribute('data-colnum'));
//console.info('c %o | ev %o | el %o', c, ev, el);
		if ((c == undefined) || isNaN(c)) return;
		this.opts.start = 0;
		ev.preventDefault();
		//console.log(c);
		if (this.sortCol == c) {
			this.sortReverse = !this.sortReverse;
		} else {
			this.sortReverse = false;
		}
		this.sortCol = c;

		this.opts.tableEl.find('th.headerSortDown').removeClass('headerSortDown');
		this.opts.tableEl.find('th.headerSortUp').removeClass('headerSortUp');
		if (this.sortReverse) {
			this.opts.tableEl.find('th.ptcol-' + this.opts.columns[c].key).addClass('headerSortUp');
		} else {
			this.opts.tableEl.find('th.ptcol-' + this.opts.columns[c].key).addClass('headerSortDown');
		}
//console.log('sortCol=%d sortReverse=%o', this.sortCol, this.sortReverse);
		this.newSlice(this.sortCol, this.sortReverse);
		this.show();
	};



//TODO cache the full slice until filter changes (per column)
//TODO when no filter, just return the sorts[col]
	this.slice = function(col, start, end, reverse) {
		if ((end == undefined) || (end > this.matchesFilter.length)) end = this.matchesFilter.length;
		if ((start == undefined) || (start > this.matchesFilter.length)) start = 0;
//console.log('col %o | start %o | end %o | reverse %o', col, start, end, reverse);
		var at = -1;
		var s = [];
/*
		var keys = [];
		var map = {};
		for (var i = start ; i <= end ; i++) {
//console.log('%d %d %d', i, this.matchesFilter[i], this.sortsInd[col][this.matchesFilter[i]]);
			var k = this.sortsInd[col][this.matchesFilter[i]];
			keys.push(k);
			map[k] = this.matchesFilter[i];
		}
		keys.sort(function(a,b) { return a - b; });
		for (var i = 0 ; i < keys.length ; i++) {
			s.push(map[keys[i]]);
		}
*/

		if (!this._sortCache[col]) {
			if (this.matchesFilter.length == this.opts.data.length) {  //we have not been filtered, so dont do too much work
				this._sortCache[col] = this.sorts[col].slice();
				this._sortCacheRev[col] = this.sorts[col].slice();
				this._sortCacheRev[col].reverse();
			} else {
				this._sortCache[col] = [];
				this._sortCacheRev[col] = [];
				for (var i = 0 ; i < this.opts.data.length ; i++) {
					if (this.matchesFilter.indexOf(this.sorts[col][i]) < 0) continue;
					this._sortCache[col].push(this.sorts[col][i]);
					this._sortCacheRev[col].unshift(this.sorts[col][i]);
				}
			}
console.log(this._sortCache[col]);
		}

		if (reverse) return this._sortCacheRev[col].slice(start, end);
		return this._sortCache[col].slice(start, end);
/*
		for (var i = 0 ; i < this.opts.data.length ; i++) {
			var offset = i;
			if (reverse) offset = this.opts.data.length - i - 1;
			if (this.matchesFilter.indexOf(this.sorts[col][offset]) < 0) continue;
			at++;
			if ((at < start) || (at > end)) continue;
			s.push(this.sorts[col][offset]);
		}
*/

		return s;
	};


	this.show = function() {
		this.opts.tableEl.find('td').html('');
		this.opts.tableEl.find('tbody tr').show();
		for (var i = 0 ; i < this.results.length ; i++) {
			//$('#results-table tbody tr')[i].title = 'Encounter ' + searchResults[results[i]].id;
			//this.opts.tableEl.find('tbody tr')[i].setAttribute('data-id', this.opts.data[this.results[i]].individualID);
			this.opts.tableEl.find('tbody tr')[i].setAttribute('data-i', i);
			for (var c = 0 ; c < this.opts.columns.length ; c++) {
				this.opts.tableEl.find('tbody tr')[i].children[c].innerHTML = '<div>' + this.values[this.results[i]][c] + '</div>';
			}
		}
		if (this.results.length < this.opts.howMany) {
			this.opts.sliderEl.hide();
			for (var i = 0 ; i < (this.opts.howMany - this.results.length) ; i++) {
				this.opts.tableEl.find('tbody tr')[i + this.results.length].style.display = 'none';
			}
		} else {
			$('#results-slider').show();
		}

		//if (sTable.opts.sliderEl) sTable.opts.sliderEl.slider('option', 'value', 100 - (start / (searchResults.length - howMany)) * 100);
		this.sliderSet(100 - (this.opts.start / (this.matchesFilter.length - this.opts.howMany)) * 100);
		this.displayPagePosition();
		if (this.opts.showCallback) this.opts.showCallback(this);
	}


	this.lastSliderStart = -1;
	this.sliderInit = function() {
		if (!this.opts.sliderEl) return;
		this.opts.sliderEl.addClass('pageableTable-slider');
		if (this.opts.data.length - this.opts.perPage < 1) return;
		this.opts.sliderEl.slider({
			orientation: 'vertical',
			value: 100,
//TODO generalize this function!
			slide: function(a, b) {
				//var s = Math.floor((100 - b.value) / 100 * (me.opts.data.length - me.opts.perPage) + 0.5);
				var s = Math.floor((100 - b.value) / 100 * (me.matchesFilter.length - me.opts.perPage) + 0.5);
				if (s == me.lastSliderStart) return;
				me.lastSliderStart = s;
				console.log(s);
				me.opts.start = s;
				me.newSlice(me.sortCol, me.sortReverse);
				me.show();
				//me.pageTable(start);
			}
		});
	};

	this.dump = function(ind) {
		var header = ['#', 'idx'];
		for (var c = 0 ; c < this.opts.columns.length ; c++) {
			header.push(this.opts.columns[c].label || this.opts.columns[c].key);
		}
		console.info(header.join(' | '));
		for (var i = 0 ; i < ind.length ; i++) {
			var row = [i, ind[i]];
			for (var c = 0 ; c < this.opts.columns.length ; c++) {
				row.push(this.values[ind[i]][c]);
			}
			console.log(row.join(' | '));
		}
	};

	this.sliderSet = function(percent) {
		if (!this.opts.sliderEl) return;
		if (this.matchesFilter.length - this.opts.perPage < 1) return;
		this.opts.sliderEl.slider('option', 'value', percent);
	};

	this.filter = function(s) {
		this._sortCache = [];
		this._sortCacheRev = [];
		if (s == undefined) {
			//TODO this is a kinda hacky trick to get an array of ints 0..LENGTH-1 ... but is it cross-browser enough?
			this.matchesFilter = Object.keys(this.values);
			return;
		}
		this.matchesFilter = [];
		var regex = new RegExp(s, 'i');
		for (var i = 0 ; i < this.opts.data.length ; i++) {
			if (regex.test(this.searchValues[i])) this.matchesFilter.push(i);
		}
	};


	this.valueAt = function(obj, colnum) {
		if (this.opts.columns[colnum].value) return this.opts.columns[colnum].value(obj, colnum);
		return keyValue(obj, this.opts.columns[colnum].key);
		//return obj[this.opts.columns[colnum].key];
	};


	this.sortValueAt = function(obj, colnum) {
		if (this.opts.columns[colnum].sortValue) return this.opts.columns[colnum].sortValue(obj, colnum);
		if (this.opts.columns[colnum].value) return this.opts.columns[colnum].value(obj, colnum);
		return keyValue(obj, this.opts.columns[colnum].key);
	};



}



//this allows us to use plain old hash/object or hacktacular backbone style
function keyValue(obj, key) {
	if (obj.attributes) return obj.get(key);
	return obj[key];
}

