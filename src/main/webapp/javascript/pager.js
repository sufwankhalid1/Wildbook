// Global module
// There is no need to register a page to get it to show, only if you
// want to create a special onShow function that runs when the page
// is shown.
//
var pager = (function () {
    // find the name of the war file
    var warfile = this.location.pathname.split('/').filter(function(el) {return el.length != 0})[0];
    
    var pages = {};
    var lastPage = null;
    
    function display(pageElement, pageName) {
        pageElement.style.display = 'block';
        var page = pages[pageName];
        if (page) {
            if (page.onShow) {
                try {
                    page.onShow();
                } catch (ex) {
                    alert(ex.stack);
                }
            }
            lastPage = page;
        } else {
            lastPage = {'name': pageName};
        }
    }
    
    function show(pageName) {
        if (lastPage) {
            document.getElementById('page_' + lastPage.name).style.display = 'none';
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
        
        var pagekey = 'page_' + pageName;
        var pageElement = document.getElementById(pagekey);
        if (! pageElement) {
            var newPage = $('<div/>', {'id': pagekey});
            $('#pages').append(newPage);
            
            newPage.load('/' + warfile + '/html/pages/' + pageName + '.html',
                         function() {
                display(document.getElementById(pagekey), pageName);
            });
        } else {
            display(pageElement, pageName);
        }
    }
    
    function register(pageName, onShow, onHide) {
        // already registered
        if (pages[pageName]) {
            return;
        }
        
        var page = {
            'name': pageName,
            'onShow': onShow,
            'onHide': onHide
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
