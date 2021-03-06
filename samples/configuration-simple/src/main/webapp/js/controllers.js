/*
 * Copyright (C) 2013 Motown.IO (info@motown.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
angular.module('demoApp.controllers', []).
    controller('ChargingStationController',
        ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
            $scope.init = function () {
                $scope.offset = 0;
                $scope.limit = 10;
                $scope.numberOfPages = 1;
                $scope.getChargingStations();
                if(!$scope.chargingStationTimer) {
                    $scope.chargingStationTimer = $scope.startGetChargingStationsTimer();
                }
            };

            $scope.$on('$destroy', function destroy() {
                $interval.cancel($scope.chargingStationTimer);
                delete $scope.chargingStationTimer;
            });

            $scope.startGetChargingStationsTimer = function () {
                return $interval(function () {
                    $scope.getChargingStations();
                }, 5000);
            };

            $scope.getChargingStations = function () {
                var q = ['offset=' + ($scope.offset || 0), 'limit=' + ($scope.limit || 10)];

                $http({
                    url: 'rest/operator-api/charging-stations?' + q.join('&'),
                    method: 'GET',
                    data: ''
                }).success(function (response) {
                    $scope.chargingStations = response.elements;
                    $scope.numberOfPages = Math.floor(parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) / parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10)) + 1;
                    $scope.totalNumberOfElements = parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) + parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10);
                }).error(function () {
                    console.log('Error getting charging stations, cancel polling.');
                    $interval.cancel($scope.chargingStationTimer);
                    delete $scope.chargingStationTimer;
                });
            };

            $scope.changePage = function(page) {
                $scope.offset = (page - 1) * $scope.limit;
                $scope.getChargingStations();
            };

            $scope.registerChargingStation = function (chargingStation) {
                var cs = chargingStation;

                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['AcceptChargingStation', {
                    }]
                }).success(function (response) {
                    console.log('registered');
                    cs.accepted = true;
                });
            };

            $scope.resetChargingStation = function (chargingStation, type) {
                var resetType = 'soft';

                if (type == 'hard') {
                    resetType = 'hard';
                }

                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestResetChargingStation', {
                        'type': resetType
                    }]
                }).success(function (response) {
                    console.log('reset requested');
                });
            };

            $scope.startTransaction = function (chargingStation) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestStartTransaction', {
                        'evseId': 2,
                        'identifyingToken': {'token': 'TOKEN'}
                    }]
                }).success(function (response) {
                    console.log('start transaction requested');
                });
            };

            $scope.unlockEvse = function (chargingStation, evseId) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestUnlockEvse', {
                        'evseId': evseId,
                        'identifyingToken': {'token': 'TOKEN'}
                    }]
                }).success(function (response) {
                    console.log('unlock evse requested');
                });
            };

            $scope.dataTransfer = function (chargingStation, vendorId, messageId, data) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestDataTransfer', {
                        'vendorId': vendorId,
                        'messageId': messageId,
                        'data': data
                    }]
                }).success(function (response) {
                    console.log('data transfer requested');
                });
            };

            $scope.changeConfiguration = function (chargingStation, key, value) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestChangeConfigurationItem', {
                        'key': key,
                        'value': value
                    }]
                }).success(function (response) {
                    console.log('change configuration requested');
                });
            };

            $scope.getDiagnostics = function (chargingStation, targetLocation) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestDiagnostics', {
                        'targetLocation': targetLocation
                    }]
                }).success(function (response) {
                    console.log('diagnostics requested');
                });
            };

            $scope.getConfiguration = function (chargingStation) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestConfigurationItems', {
                    }]
                }).success(function (response) {
                        console.log('get-configuration requested');
                    });
            };

            $scope.clearCache = function (chargingStation) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestClearCache', {
                    }]
                }).success(function (response) {
                    console.log('clear cache requested');
                });
            };

            $scope.updateFirmware = function (chargingStation, location, retrieveDate, numRetries, retryInterval) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestFirmwareUpdate', {
                        'location': location,
                        'retrieveDate': retrieveDate,
                        'numRetries': numRetries,
                        'retryInterval': retryInterval
                    }]
                }).success(function (response) {
                    console.log('clear cache requested');
                });
            };

            $scope.getAuthorizationListVersion = function (chargingStation) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestAuthorizationListVersion', {
                    }]
                }).success(function (response) {
                    console.log('clear cache requested');
                });
            };

            $scope.sendAuthorizationList = function (chargingStation, listVersion, updateType, items) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestSendAuthorizationList', {
                        'listVersion': listVersion,
                        'updateType': updateType,
                        'items': items
                    }]
                }).success(function (response) {
                    console.log('clear cache requested');
                });
            };

            $scope.changeAvailability = function (chargingStation, type) {
                var availabilityType = 'operative';

                if (type == 'inoperative') {
                    availabilityType = 'inoperative';
                }

                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestChangeAvailability', {
                        'evseId': 1,
                        'availability': availabilityType
                    }]
                }).success(function (response) {
                    console.log('change availability requested');
                });
            };

            $scope.reserveNow = function (chargingStation, evseId, identifyingToken, expiryDate) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestReserveNow', {
                        'evseId': evseId,
                        'identifyingToken': identifyingToken,
                        'expiryDate': expiryDate
                    }]
                }).success(function (response) {
                    console.log('reserve now requested');
                });
            };

            $scope.cancelReservation = function (chargingStation, reservationId) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RequestCancelReservation', {
                        'reservationId': reservationId
                    }]
                }).success(function (response) {
                        console.log('cancel reservation requested');
                    });
            };

            $scope.updateReservable = function (chargingStation, reservable) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['UpdateChargingStationReservable', {
                        'reservable': reservable
                    }]
                }).success(function (response) {
                    console.log('update reservable requested');
                });
            };

            $scope.placeChargingStation = function (chargingStation, coordinates, address, accessibility) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['PlaceChargingStation', {
                        'coordinates': coordinates,
                        'address': address,
                        'accessibility': accessibility
                    }]
                }).success(function (response) {
                    console.log('charging station placed');
                });
            };

            $scope.improveChargingStationLocation = function (chargingStation, coordinates, address, accessibility) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['ImproveChargingStationLocation', {
                        'coordinates': coordinates,
                        'address': address,
                        'accessibility': accessibility
                    }]
                }).success(function (response) {
                    console.log('charging station location improved');
                });
            };

            $scope.moveChargingStation = function (chargingStation, coordinates, address, accessibility) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['MoveChargingStation', {
                        'coordinates': coordinates,
                        'address': address,
                        'accessibility': accessibility
                    }]
                }).success(function (response) {
                    console.log('charging station moved');
                });
            };

            $scope.setOpeningTimes = function (chargingStation, openingTimes) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['SetChargingStationOpeningTimes', {
                        'openingTimes': openingTimes
                    }]
                }).success(function (response) {
                    console.log('Opening times set');
                });
            };

            $scope.addOpeningTimes = function (chargingStation, openingTimes) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['AddChargingStationOpeningTimes', {
                        'openingTimes': openingTimes
                    }]
                }).success(function (response) {
                    console.log('Opening times added');
                });
            };

            $scope.grantPermission = function (chargingStation, commandClass, userIdentity) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['GrantPermission', {
                        'commandClass': commandClass,
                        'userIdentity': userIdentity

                    }]
                }).success(function (response) {
                    console.log('Permission requested');
                });
            };

            $scope.revokePermission = function (chargingStation, commandClass, userIdentity) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStation.id + '/commands',
                    method: 'POST',
                    data: ['RevokePermission', {
                        'commandClass': commandClass,
                        'userIdentity': userIdentity

                    }]
                }).success(function (response) {
                    console.log('Revoke permission requested');
                });
            }
        }]).
    controller('TransactionController',
        ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
            $scope.init = function () {
                $scope.offset = 0;
                $scope.limit = 10;
                $scope.numberOfPages = 1;
                $scope.getTransactions();
                if(!$scope.transactionTimer) {
                    $scope.transactionTimer = $scope.startGetTransactionsTimer();
                }
            };

            $scope.$on('$destroy', function destroy() {
                console.log('Cancelling timer');
                $interval.cancel($scope.transactionTimer);
                delete $scope.transactionTimer;
            });

            $scope.startGetTransactionsTimer = function () {
                return $interval(function () {
                    $scope.getTransactions();
                }, 5000);
            };

            $scope.getTransactions = function () {
                var q = ['offset=' + ($scope.offset || 0), 'limit=' + ($scope.limit || 10)];

                $http({
                    url: 'rest/operator-api/transactions?' + q.join('&'),
                    method: 'GET',
                    data: ''
                }).success(function (response) {
                    $scope.transactions = response.elements;
                    $scope.numberOfPages = Math.floor(parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) / parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10)) + 1;
                    $scope.totalNumberOfElements = parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) + parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10);
                }).error(function() {
                    console.log('Error getting transactions, cancel polling.');
                    $interval.cancel($scope.transactionTimer);
                    delete $scope.transactionTimer;
                });
            };

            $scope.changePage = function(page) {
                $scope.offset = (page - 1) * $scope.limit;
                $scope.getTransactions();
            };

            $scope.stopTransaction = function (chargingStationId, id) {
                $http({
                    url: 'rest/operator-api/charging-stations/' + chargingStationId + '/commands',
                    method: 'POST',
                    data: ['RequestStopTransaction', {
                        'id': id
                    }]
                }).success(function (response) {
                    console.log('remote stop!');
                });
            };
        }]).
    controller('ConfigurationController',
        ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
            $scope.init = function () {
                $scope.offset = 0;
                $scope.limit = 10;
                $scope.numberOfPages = 1;
                $scope.getChargingStationTypes();
            };

            $scope.getChargingStationTypes = function () {
                var q = ['offset=' + ($scope.offset || 0), 'limit=' + ($scope.limit || 10)];

                $http({
                    url: 'rest/config/chargingstationtypes?' + q.join('&'),
                    method: 'GET',
                    data: ''
                }).success(function (response) {
                    $scope.chargingStationTypes = response.elements;
                    $scope.numberOfPages = Math.floor(parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) / parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10)) + 1;
                    $scope.totalNumberOfElements = parseInt(/offset=([^&]+)/.exec(response.last.href)[1], 10) + parseInt(/limit=([^&]+)/.exec(response.last.href)[1], 10);
                });
            };

            $scope.changePage = function(page) {
                $scope.offset = (page - 1) * $scope.limit;
                $scope.getChargingStationTypes();
            };
        }]
    ).


    controller('MobiEuropeController',
        ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
            $scope.init = function () {
                $scope.pmsIdentifier = "NL-MOT";
                $scope.serviceTypeIdentifier = "EV_CHARGING";

                $scope.servicePms = "NL-MOT";
                $scope.localServiceIdentifier = "DEMO_001";
                $scope.userIdentifier = "testPas";
                $scope.connectorIdentifier = "1";

                if(!$scope.pollSessionInfoTimer) {
                    $scope.pollSessionInfoTimer = $scope.startPollSessionInfoTimer();
                }
            };

            $scope.$on('$destroy', function destroy() {
                $interval.cancel($scope.pollSessionInfoTimer);
                delete $scope.pollSessionInfoTimer;
            });

            $scope.startPollSessionInfoTimer = function () {
                return $interval(function () {
                    $scope.pollSessionInfo($scope.authorizationIdentifier);
                }, 2000);
            };

            $scope.pollSessionInfo = function(authorizationIdentifier) {
                if ($scope.sessionInfo != undefined) {
                    if ($scope.sessionInfo.sessionStateMachine.state == 'StartTxRequested' ||
                        $scope.sessionInfo.sessionStateMachine.state == 'StopTxRequested') {
                        console.log("polling");
                        $http({
                            url: 'rest/mobieurope/source/sv1/session',
                            method: 'POST',
                            data: authorizationIdentifier
                        }).success(function (sessionInfo) {
                            $scope.sessionInfo = sessionInfo;
                        });
                    }
                }
            };

            $scope.authorize = function(userIdentifier, pmsIdentifier, servicePms, localServiceIdentifier, connectorIdentifier) {
                $http({
                    headers: {
                        "Content-Type": "application/vnd.io.motown.mobi-europe-source-api-v1+json"
                    },
                    url: 'rest/mobieurope/source/sv1/authorize',
                    method: 'POST',
                    data: {
                        'servicePms': servicePms,
                        'userIdentifier': userIdentifier,
                        'pmsIdentifier': pmsIdentifier,
                        'localServiceIdentifier': localServiceIdentifier,
                        'connectorIdentifier': connectorIdentifier
                    }
                }).success(function (data) {
                    $scope.authorizationIdentifier = data.response.authorizationIdentifier;
                    $scope.sessionInfo = data.sessionInfo;
                    console.log('authorize request sent');
                }).error(function(response) {
                    $scope.responseError = response.responseError;
                    console.log('Error while sending authorize request.');
                });
            };

            $scope.requestStartTransaction = function(authorizationIdentifier) {
                $http({
                    headers: {
                        "Content-Type": "application/vnd.io.motown.mobi-europe-source-api-v1+json"
                    },
                    url: 'rest/mobieurope/source/sv1/requestStartTransaction',
                    method: 'POST',
                    data: {
                        'authorizationIdentifier': authorizationIdentifier
                    }
                }).success(function (data) {
                    $scope.requestIdentifier = data.response.requestIdentifier;
                    $scope.sessionInfo = data.sessionInfo;
                    console.log('requestStartTransaction request sent');
                }).error(function(response) {
                    $scope.responseError = response.responseError;
                    console.log('Error while sending requestStartTransaction request.');
                });
            };

            $scope.requestStopTransaction = function(authorizationIdentifier) {
                $http({
                    headers: {
                        "Content-Type": "application/vnd.io.motown.mobi-europe-source-api-v1+json"
                    },
                    url: 'rest/mobieurope/source/sv1/requestStopTransaction',
                    method: 'POST',
                    data: {
                        'authorizationIdentifier': authorizationIdentifier
                    }
                }).success(function (data) {
                    $scope.response = data.response.requestIdentifier;
                    $scope.sessionInfo = data.sessionInfo;
                    console.log('requestStopTransaction request sent');
                }).error(function(response) {
                    $scope.responseError = response.responseError;
                    console.log('Error while sending requestStopTransaction request.');
                });
            };
        }
    ]).

    controller('MobiEuropeDestinationController',
        ['$scope', '$http', '$interval', function ($scope, $http, $interval) {
            $scope.init = function () {
                if(!$scope.pollSessionInfoTimer) {
                    $scope.pollSessionInfoTimer = $scope.startPollSessionInfoTimer();
                }
            };

            $scope.$on('$destroy', function destroy() {
                $interval.cancel($scope.pollSessionInfoTimer);
                delete $scope.pollSessionInfoTimer;
            });

            $scope.startPollSessionInfoTimer = function () {
                return $interval(function () {
                    $scope.pollSessionInfo();
                }, 2000);
            };

            $scope.pollSessionInfo = function() {
                console.log("polling");
                $http({
                    url: 'rest/mobieurope/destination/dv1/lastopensession',
                    method: 'GET'
                }).success(function (sessionInfo) {
                    $scope.sessionInfo = sessionInfo;
                });
            };
        }
    ]).

    controller('LoginController',
        ['$scope', '$rootScope', '$location', '$cookieStore', 'UserService', function ($scope, $rootScope, $location, $cookieStore, UserService) {
            $scope.rememberMe = false;

            $scope.login = function() {
                UserService.authenticate($.param({username: $scope.username, password: $scope.password}), function(authenticationResult) {
                    var authToken = authenticationResult.token;
                    $rootScope.authToken = authToken;
                    if ($scope.rememberMe) {
                        $cookieStore.put('authToken', authToken);
                    }
                    $location.path("/");
                }, function() {
                    alert('Combination username & password not valid.');
                });
            };
        }
    ]);
