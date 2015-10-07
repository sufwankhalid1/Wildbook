wildbook.app.controller("EncounterEditController", function($scope, $http, $exceptionHandler) {
    //
    // Empty encounter for when creating a new encounter.
    //
    //$scope.encounter = {individual: {species: $scope.config.species[0].code}};

    $scope.save = function() {
        $http.post('obj/encounter/save', $scope.encounter)
        .then(function() {
            $scope.$emit('encounter_edit_done', $scope.encounter);
        }, $exceptionHandler);
    };
});
