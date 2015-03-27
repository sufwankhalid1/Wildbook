
wildbook.pageInit = function(callback) {
	console.info('start wildbook.init()');

	wildbook.init(function() { wildbook.prepLocal(callback); });
/*
		encs = new wildbook.Collection.Encounters();
		encs.fetch({
			jdoql: jdoql,
			success: function() { searchResults = encs.models; doTable(); },
		});
	});
*/

}


wildbook.prepLocal = function(callback) {

	wildbook.local.initCache();

	callback;
}




wildbook.local = {

	status: 'unknown',

	classesToCache: [
		'org.ecocean.MarkedIndividual',
	],


	initCache: function() {
		this.status = 'checking';
console.warn('doing initCache in background?');

		for (var i = 0 ; i < this.classesToCache.length ; i++) {
			if (this.isClassStale(this.classesToCache[i])) {
console.log('%s is stale!!!', this.classesToCache[i]);
			}
		}
console.warn('end of initCache');
	},

	ready: function() {
		return (this.status == 'ready');
	},

	//these are utility functions really, so we get our own unique keys
	set: function(key, val) {
		return localStorage.setItem(wildbook.local._key(key), val);
	},
	get: function(key) {
		return localStorage.getItem(wildbook.local._key(key));
	},

	_key: function(k) {
		return '_wildbook:' + k;
	},


	isClassStale: function(cls) {
		var lm = this.get('lastModified:' + cls);
		//if (!lm) return true;
		var stale = false;
		$.ajax({
			method: 'HEAD',
			async: false,
			dataType: 'text',
			url: wildbookGlobals.baseUrl + '/jsonCache/' + cls + '.json',
			complete: function(xhr, s) {
				//if we cant get through, lets say we are NOT stale???  TODO
				if (s == 'success') {
					var smod = xhr.getResponseHeader('Last-Modified');
console.log('got lastmod %s (local marked = %s)', smod, lm);
					if (smod && (smod != lm)) stale = true;
				}
		////////////////wildbook.local.set('lastModified:' + cls, smod);
			},
		});
console.log('fell thru with stale=%o', stale);
		return stale;
	},

}

