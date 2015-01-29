// Global module
// There is no need to register a page to get it to show, only if you
// want to create a special onShow function that runs when the page
// is shown.
//
var pager = (function () {
    var pages = {};
    var lastPage = null;
    
    function show(pageName) {
        if (lastPage) {
            document.getElementById("page_" + lastPage.name).style.display = "none";
            if (lastPage.onHide) {
                try {
                    lastPage.onHide();
                } catch (ex) {
                    // Do *something with the exception. We have to catch it because
                    // if we don't it gets swallowed by the browser and the new
                    // page is not shown.
                    console.log(ex);
                }
            }
            lastPage = null;
        }
        
        var pageElement = document.getElementById("page_" + pageName);
        if (! pageElement) {
            return;
        }
        
    	pageElement.style.display = "block";
    	var page = pages[pageName];
    	if (page) {
    	    if (page.onShow) {
                try {
                    page.onShow();
                } catch (ex) {
                    alert(ex);
                }
    	    }
            lastPage = page;
    	} else {
    	    lastPage = {"name": pageName};
    	}
    }
    
    function register(pageName, onShow, onHide) {
        // already registered
        if (pages[pageName]) {
            return;
        }
        
        var page = {
            "name": pageName,
            "onShow": onShow,
            "onHide": onHide
        };
        pages[pageName] = page;
    }
 
    return{
        register: function(pageName, onShow, onHide) {
            register(pageName, onShow, onHide);
        },
        show: function(pageName) {
            show(pageName);
        }
    };
})();
