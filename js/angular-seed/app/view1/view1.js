'use strict';

angular.module('myApp.view1', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/view1', {
    templateUrl: 'view1/view1.html',
    controller: 'View1Ctrl',
    resolve: {
      superApraFunds: ['api', function(api) {
        return api.fetchSuperApraFunds().then(function(result) { return result.data.super_products; });
      }],
      electronicServiceAddresses: ['api', function(api) {
        return api.fetchElectronicServiceAddresses().then(function(result) { return result.data.electronic_service_addresses; });
      }]
    }
  });
}])

.controller('View1Ctrl', ['$scope', 'superApraFunds', 'electronicServiceAddresses',
function($scope, superApraFunds, electronicServiceAddresses) {
  $scope.superApraFunds = superApraFunds;
  $scope.electronicServiceAddresses = electronicServiceAddresses;
  $scope.fund = {
    fundType: 'APRA'
  };
}]);
