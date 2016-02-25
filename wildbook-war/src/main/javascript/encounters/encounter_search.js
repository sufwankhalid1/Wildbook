/* global angular, document, window */
'use strict';

angular.module('wildbook.admin').directive(
    'wbEncounterSearch',
    ["$http", "$exceptionHandler", "$mdDialog", function($http, $exceptionHandler, $mdDialog) {
        return {
            restrict: 'E',
            scope: {
                searchEncounterDone: "&",
                resetSelectedResult:"&"
            },
            templateUrl: 'encounters/encounter_search.html',
            replace: true,
            controller: function($scope) {
                $scope.searchdata =  {
                    encounter: {},
                    individual: {},
                    contributor: {}
                };

                $scope.reset = function() {
                    $scope.searchdata =  {
                        encounter: {},
                        individual: {},
                        contributor: {}
                    };
                };

                $scope.selectedTabIndex = 0;

                $scope.search = function() {
                    $scope.resetSelectedResult({val: null});
                    $http.post("admin/api/search/encounter", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.numResults = result.data.length;
                        $scope.selectedTabIndex = 1;
                    },
                    $exceptionHandler);
                };

                function rowSelectedFunc(event) {
                    $scope.searchEncounterDone({encounter: event.node.data});
                }

                $scope.gridOptions = {
                    columnDefs:
                        [{headerName: "",
                            field: "individual",
                            cellRenderer: function(params) {
                                if (params.value && params.value.avatar) {
                                    return '<img width="*" height="32px" src="' + params.value.avatar + '"/>';
                                }
                                return null;
                            },
                            width: 32
                         },
                         {headerName: "Individual",
                             field: "individual",
                             cellRenderer: function(params) {
                                 if (params.value) {
                                     return params.value.displayName;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Species",
                             field: "individual",
                             cellRenderer: function(params) {
                                 if (params.value && params.value.species) {
                                     return params.value.species.name;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Date",
                             field: "formattedTime"
                         },
                         {headerName: "Location",
                             field: "location",
                             cellRenderer: function(params) {
                                 if (!params.value) {
                                     return null;
                                 }
                                 var value;

                                 if (params.value.locationid) {
                                     value = params.value.locationid;
                                 }

                                 if (params.value.verbatimLocation) {
                                     if (value) {
                                         value += ' - ';
                                     } else {
                                         value = '';
                                     }
                                     value += params.value.verbatimLocation;

                                     value = '<md-icon md-svg-icon="information-outline" title="'
                                         + params.value.verbatimLocation
                                         + '"></md-icon>&nbsp;'
                                         + value;
                                 }

                                 return value;
                             }
                         }],
                    rowData: null,
                    rowHeight: 32,
                    enableSorting: true,
                    rowSelection: 'single',
                    onRowSelected: rowSelectedFunc,
                    angularCompileRows: true
                };

                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.searchEncounterDone(null);
                };

                function exportDialogController($scope, $mdDialog, numResults, searchData) {
                    if (numResults) {
                        $scope.numResults = numResults;
                    } else {
                        $scope.numResults = numResults = 0;
                    }

                    $scope.exportEncounter = function() {
                        $http.post("admin/api/export/encounters", searchData)
                        .then(function(response) {
                            $scope.exportid = response.data;
                        });
                    };

                    $scope.closeDialog = function() {
                        $mdDialog.hide();
                    };

                    $scope.viewExports = function() {
                        window.location = window.location.pathname + "?j=page/myAccount";
                    };
                }

                $scope.exportDialog = function($event) {
                    var parentEl = angular.element(document.body);
                   $mdDialog.show({
                         parent: parentEl,
                         targetEvent: $event,
                         clickOutsideToClose:true,
                         template:
                           '<md-dialog class="export-dialog" aria-label="List dialog">'
                           +'    <md-toolbar>'
                           +'        <div class="md-toolbar-tools">'
                           +'            <h2>Export Encounter</h2>'
                           +'            <span flex></span>'
                           +'            <md-button class="md-icon-button" ng-click="closeDialog()">'
                           +'                <md-icon md-svg-icon="close" aria-label="Close dialog"></md-icon>'
                           +'            </md-button>'
                           +'        </div>'
                           +'    </md-toolbar>'
                           +'    <md-dialog-content layout-align="center center"  layout="row" layout-wrap>'
                           +'       <div layout="row" flex="100" class="mb-20" layout-align="center center">'
                           +'           <div ng-show="!exportid && numResults" class="mt-10">You are about to export {{numResults}} encounters</div>'
                           +'           <div ng-show="!numResults" class="mt-10">There are no encounters to export. <br/> Please check to make sure your search parameters are correct.</div>'
                           +'           <div ng-show="exportid" class="mt-10">'
                           +'               <div>Your Export is being processed</div>'
                           +'           </div>'
                           +'       </div>'
                           +'       <md-dialog-actions layout="row" layout-align="end center">'
                           +'           <md-button ng-show="!exportid" ng-disabled="!numResults" class="md-whiteframe-1dp" ng-click="exportEncounter()">'
                           +'               Export'
                           +'           </md-button>'
                           +'           <md-button ng-show="exportid" ng-click="viewExports()" class="md-whiteframe-1dp">'
                           +'               View Exports'
                           +'           </md-button>'
                           +'           <md-button ng-show="exportid" ng-disabled="!numResults" class="md-whiteframe-1dp" ng-click="closeDialog()">'
                           +'               Close'
                           +'           </md-button>'
                           +'       </md-dialog-actions>'
                           +'    </md-dialog-content>'
                           +'</md-dialog>',
                         locals: {
                            numResults: $scope.numResults,
                            searchData: $scope.searchdata
                         },
                         controller: exportDialogController
                    });
                };

                $scope.cmdEnter = function() {
                    $scope.search();
                };
            }
        };
    }]
);
