'use strict';

var exceptionCatcherModule = angular.module('exceptionCatcher.module', []);
exceptionCatcherModule.factory('$exceptionHandler', ['$injector', function ($injector) {
    return function errorCatcherHandler(exception, cause) {    	
        console.error(exception.stack);
        var MessageHandler = $injector.get("MessageHandler")
        MessageHandler.manageException(exception);
    };
}]);

var errorCatcherModule = angular.module('errorCatcher.module', []);
errorCatcherModule.factory('errorHttpInterceptor', ['$q', '$injector', function ($q, $injector) {
    return {
        responseError: function responseError(rejection) {
        	var MessageHandler = $injector.get("MessageHandler")
        	MessageHandler.manageError(rejection);
            return $q.reject(rejection);
        }
    };
}])
.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('errorHttpInterceptor');
}]);