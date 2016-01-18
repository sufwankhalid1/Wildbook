/* global angular, $, window */
'use strict';

angular.module('wildbook.util').directive(
    'wbSiteSearch',
    ["$http", function($http) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'util/site_search.html',
            replace: true,
            controller: function($scope) {

                function querySearch(query) {
                    return $http.get('search/site', query)
                    .then(function(data) {
                        return $.map(data, function(item) {
                            var label;
                            if ((item.type === "individual") && (item.species !== null)) {
                  //            label = item.species + ": ";
                            }
                            else if (item.type === "user") {
                                label = "User: ";
                            } else {
                                label = "";
                            }
                            return {label: label + item.label,
                                    value: item.value,
                                    type: item.type};
                        });
                    });
                }
                $scope.querySearch   = querySearch;

                $scope.searchTextChange = function(text) {
/*                    console.log('Text changed to ' + text);*/
                };

                $scope.selectedItemChange = function(item) {
                    if (item.type === "individual") {
                        window.location.replace("individuals.jsp?number=" + item.value);
                    } else if (item.type === "locationID") {
                        window.location.replace("/encounters/searchResultsAnalysis.jsp?locationCodeField=" + item.value);
                    }
                };
            }
        };
    }]
);
