wildbook.app.controller("EncounterEditController", function($scope, $http, $exceptionHandler) {
    var panelName = "encounter_edit";

    $scope.$on(panelName, function(event, data) {
        if (data) {
            $scope.encounter = data;
        } else {
            $scope.encounter = {individual: {species: $scope.main.config.species[0]}};
        }
    });
    
    function closePanel() {
        $scope.panels[panelName] = false;
        $scope.$emit(panelName + "_done", $scope.encounter);
    }

    $scope.save = function() {
        $http.post('obj/encounter/save', $scope.encounter)
        .then(function(result) {
            $scope.encounter.id = result.data;
            closePanel();
        }, $exceptionHandler);
    };
    
    //
    // wb-key-handler-form
    //
    $scope.cancel = function() {
        $scope.encounter = null;
        closePanel();
    }
    
    $scope.cmdEnter = function() {
        $scope.save();
    }
});
