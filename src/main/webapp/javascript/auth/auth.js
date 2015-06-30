
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
		if (!d.length) {
			d = $('<div id="login-popup">').css("display", "none").addClass("alertplusDialog");
		}
		var h = '<div><input id="login-username" placeholder="username" /></div><div><input id="login-password" type="password" placeholder="password" /></div>';
		h += '<div style="border-bottom: 2px solid black; margin-bottom: 10px; padding-bottom: 10px;"><input type="button" value="login" /></div>';
		h += '<div><input type="button" value="login using facebook" /><input type="button" value="login using google" /></div>';
		d.html(h);
		d.dialog({
			modal: true,
			title: "login",
			width: 600,
			appendTo: "body",
			resizable: false
		});
	},

	loginAjax: function(username, password, args) {
		if (!args) args = {};
		args.url = wildbookGlobals.baseUrl + '/LoginUser?json=true&username=' + username + '&password=' + password;  //TODO https!
		args.type = 'GET';
		args.dataType = 'json';
		if (!args.complete) args.complete = function(a,b,c) { console.info('loginAjax return: %o %o %o',a,b,c); };
		return $.ajax(args);
	},

};

