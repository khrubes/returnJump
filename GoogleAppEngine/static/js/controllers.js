'use strict';

App.controller('HomeCtrl', [
    '$scope',
    function ($scope) {

    }
]);

App.controller('GooglePlusCtrl', [
    '$scope',
    '$window',
    '$location',
    'GooglePlusFactory',
    'ngProgress',
    function ($scope, $window, $location, GooglePlusFactory, ngProgress) {
        ngProgress.color('#ffff00');
        ngProgress.start();

        // Used to keep sign in button in a fixed position
        $scope.width = $window.innerWidth;
        $(window).resize(function(){
            $scope.$apply(function(){
               $scope.width = $window.innerWidth;
            });
        });

        $scope.signedIn = false;
        $scope.signOut = GooglePlusFactory.signOut;

        $scope.signInCallback = function (authResult) {
            if (authResult['status']['signed_in']) {
                // Needs to digest (prevents it from doing nothing after you sign in)
                $scope.$apply(function () {
                    $scope.signedIn = true;
                    $location.path('/fridge');
                });
            } else {
                $scope.signedIn = false;
                $location.path('/');

                ngProgress.complete();
            }
        };

        $scope.start = function () {
            GooglePlusFactory.renderSignInButton($scope);
        };

        $scope.start();
    }
]);

App.controller('FridgeCtrl', [
    '$scope',
    '$http',
    '$q',
    'ngProgress',
    'GooglePlusFactory',
    function ($scope, $http, $q, ngProgress, GooglePlusFactory) {
        $scope.fridgeItems = [];
        $scope.message = 'Loading...';

        var getFridgeItems = function (email) {
            $http({method: 'GET', url: '/api/fridge/' + email, headers : {'Content-Type': 'application/json'}}).
                success(function(data, status, headers, config) {
                    $scope.fridgeItems = data.data;

                    if (!Array.isArray($scope.fridgeItems)) {
                        $scope.fridgeItems = [];                        
                    }

                    if ($scope.fridgeItems.length === 0) {
                        $scope.message = 'Your fridge is empty :(';
                    }

                    ngProgress.complete();
                }).
                error(function(data, status, headers, config) {
                    $scope.message = 'Something when wrong, try again later.';

                    ngProgress.complete();
                });
        };

        if (GooglePlusFactory.getEmail() === null) {
            // Make this a function to avoid duplicate
            GooglePlusFactory.setProfile().then(function () {
                getFridgeItems(GooglePlusFactory.getEmail());
            }, function () {
                setTimeout(function () {
                    // Make this a function to avoid duplicate
                    GooglePlusFactory.setProfile().then(function () {
                        getFridgeItems(GooglePlusFactory.getEmail());
                    }, function () {
                        $scope.message = 'Something when wrong, try again later.';

                        ngProgress.complete();
                    });
                }, 1000);
            });
        } else {
            getFridgeItems(GooglePlusFactory.getEmail());
        }

    }
]);
