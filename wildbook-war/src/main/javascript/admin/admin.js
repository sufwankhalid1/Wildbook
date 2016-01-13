/* global angular */
'use strict';

angular.module('wildbook.admin', []);

require("../pages/encounterSearchPage.js");
require("../pages/individualSearchPage.js");
require("../pages/mediaSubmitAdmin.js");
require("../pages/userAdmin.js");
require("../pages/siteAdmin.js");
require("../pages/myAccountPage.js");

//search fields
require('../encounters/encounter_search_fields.js');
require('../encounters/individual_search_fields.js');
require('../user/user_search_fields.js');
