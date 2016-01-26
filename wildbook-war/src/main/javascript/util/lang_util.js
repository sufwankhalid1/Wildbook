/* global angular */
'use strict';

angular.module('wildbook.util')
.factory("wbLangUtils", function() {
    return {
        findInArray: function(array, compare) {
            var element = null;
            array.every(function(item) {
                if (compare(item)) {
                    element = item;
                    return false;
                } else {
                    return true;
                }
            });
            return element;
        },
        findIndexInArray: function(array, compare) {
            var idx = null;
            array.every(function(item, index) {
                if (compare(item)) {
                    idx = index;
                    return false;
                } else {
                    return true;
                }
            });
            return idx;
        },
        existsInArray: function(array, compare) {
            return(this.findIndexInArray(array, compare) !== null);
        }
    };
});
