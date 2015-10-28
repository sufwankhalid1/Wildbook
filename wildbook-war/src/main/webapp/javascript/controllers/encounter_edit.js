wildbook.app.controller("EncounterEditController", function($scope, $http, $exceptionHandler) {
    var panelName = "encounter_edit";
    $scope.panelList.push(panelName);

    $scope.$on(panelName, function(event, data) {
        if (typeof data === "boolean") {
            $scope.panels[panelName] = false;
            return;
        }

        $scope.panels[panelName] = true;
        if (data) {
            $scope.encounter = data;
        } else {
            $scope.encounter = {individual: {species: $scope.main.config.species[0]}};
        }
    });

    $scope.save = function() {
        $http.post('obj/encounter/save', $scope.encounter)
        .then(function(result) {
            $scope.encounter.id = result.data;
            $scope.panels[panelName] = false;
            $scope.$emit(panelName + "_done", $scope.encounter);
        }, $exceptionHandler);
    };
});
