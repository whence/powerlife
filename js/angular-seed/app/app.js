'use strict';

// Declare app level module which depends on views, and components
angular.module('myApp', [
  'ngRoute',
  'myApp.view1',
  'myApp.view2',
  'ui.bootstrap'
])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/view1'});
}])

.factory('api', ['$http', function($http) {
  return {
    fetchSuperApraFunds: function() {
      return $http.get('mock_data/super_products.json');
    },
    fetchElectronicServiceAddresses: function() {
      return $http.get('mock_data/electronic_service_addresses.json');
    }
  };
}]);
