
if (typeof wildbook == 'undefined') wildbook = {};
wildbook.auth = {
	isLoggedIn: function() {
		return false;
	},

	user: function() {
		return null;
	},

	login: function() {
		if (this.isLoggedIn()) return true;
console.log('do login thing');
	},

	loginPopup: function() {
		var d = $('#login-popup');
		if (!d.length) d = $('<div id="login-popup" />').appendTo('body');
		var h = '<div><input id="login-username" placeholder="username" /></div><div><input id="login-password" type="password" placeholder="password" /></div>';
		h += '<div style="border-bottom: 2px solid black"><input type="button" value="login" /></div>';
		h += '<div><input type="button" value="login using facebook" /><input type="button" value="login using google" /></div>';
		h += '<div><input type="button" value="create account without fb/g+" /></div>';
		d.html(h);
	},

};

