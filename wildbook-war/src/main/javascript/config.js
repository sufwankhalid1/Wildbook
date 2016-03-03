/* global angular */
'use strict';

angular.module('wildbook.config', [])
.factory("wbConfig", ["$http", "$exceptionHandler", "$q", function($http, $exceptionHandler, $q) {
    var config;

    function getVessels(orgs, org) {
        //
        // Let's find our master organization so that we can add the vessels to
        // it and thus cache the results for future occerences of the user picking
        // this organization again in the list.
        //
        var orgfilter = orgs.filter(function(value) {
            return (value.orgId === org.orgId);
        });

        var orgmaster;
        if (orgfilter.length > 0) {
            orgmaster = orgfilter[0];
        } else {
            //
            // Just set it to org so we don't have to check for null below
            // but it should *never* not be an array of size 1
            //
            orgmaster = org;
        }

        if (orgmaster.vessels) {
            return $q.resolve(orgmaster.vessels);
        }

        return $http({url: "api/survey/vessels/get", params: {orgid: org.orgId}})
        .then(function(results) {
            //
            // Set it here so that next time we ask for this org's vessels we
            // get the cached values.
            //
            orgmaster.vessels = results.data;
            return results.data;
        });
    }

    function getCachedConfig() {
        if (config) {
            return config;
        }

        getConfig();
    }

    function refreshConfig() {
        getConfig();
    }

    function getConfig() {
        config = $http({url:"admin/api/util/init"})
            .then(function(result) {
                return result.data;
            }, $exceptionHandler);

        return config;
    }

    //running this to kick it off incase it was never initialized when we need it
    getConfig();

    return {
        config: getCachedConfig,
        refreshConfig: refreshConfig,
        getVessels: function(org) {
            return getVessels(config.orgs, org);
        }
    };
}]);
