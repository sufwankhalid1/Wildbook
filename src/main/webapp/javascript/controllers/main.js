wildbook.app = angular.module('appWildbook', ["angularGrid"]);
wildbook.app.factory('$exceptionHandler', function() {
    return function(ex, cause) {
        //
        // TODO: Make this configurable so that if on production the errors
        // are sent to the console instead of a dialog?
        //
        alertplus.error(ex);
      };
});

wildbook.app.controller("MainController", function($scope, $http, $exceptionHandler) {
    //
    // TODO: Make a single call to get all the "global" data that we might need throughout the app
    // rather than piecemeal?
    //
    $scope.maindata = {};
    return $http({url:"obj/user/orgs/get"})
    .then(function(result) {
        $scope.maindata.organizations = result.data;
    }, $exceptionHandler);
});
