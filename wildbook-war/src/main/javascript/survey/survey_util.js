/* global angular */
'use strict';

angular.module('wildbook.survey', [])
.factory("wbSurveyUtils", ["$http", "$q", "$exceptionHandler", function($http, $q, $exceptionHandler) {
    return {
        createNewSurveyData: function() {
            return $http.post('admin/api/survey/save', {})
                    .then(function(res){
                        return $q.resolve({
                            surveyid: res.surveyid
                        });
                    });
        }
    };
}]);
