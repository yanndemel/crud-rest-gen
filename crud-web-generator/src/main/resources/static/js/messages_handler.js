'use strict';

/**
 * Module for messages
 */
var messageHandler = angular.module('messageHandler.module', []);

/**
 * Factory for messages handler
 */
messageHandler.factory('MessageHandler',['$rootScope', function($rootScope) {

    // message
    $rootScope.message = {};
    // successful messages
    $rootScope.message.successs = [];
    // error messages
    $rootScope.message.errors = [];
    // server error messages
    $rootScope.message.serverErrors = [];

    var $this = {};

    /**
     * Clean all messages
     */
    $this.cleanMessage = function() {
        $rootScope.message.successs = [];
        $rootScope.message.errors = [];
        $rootScope.message.serverErrors = [];
    };

    /**
     * Add a successful message
     */
    $this.addSuccess = function(success) {
    	if($rootScope.message.successs.indexOf(success)<0)
    		$rootScope.message.successs.push(success);
    };

    /**
     * Add an error message
     */
    $this.addError = function(error) {            	
    	if($rootScope.message.errors.indexOf(error)<0)
    		$rootScope.message.errors.push(error);
    };
    
    $this.addUnknownError = function() {            	
    	$this.addError("An error occured while accessing to the API (maybe a connection problem). Please check the browser console for details.")
    };

    /**
     * Add a server error message (no translate)
     */
    $this.addServerError = function(serverError) {
    	if($rootScope.message.serverErrors.indexOf(serverError)<0)
    		$rootScope.message.serverErrors.push(serverError);
    };

  

    /**
     * Manage the error
     */
    $this.manageError = function(http) {
        if( http.status === 404 ) {
            if( http.data == null || http.data === "" ) {
                $this.addError('Server not responding');
            } else {
                $this.addError('Invalid URL : '+http.config.url);
            }
        } else if( http.status === 400 ) {
            if(http.data == null) {
                $this.addError('Bad URL : '+http.config.url);
            } else {
                $this.addServerError(http.data);
            }
        } else if( http.data != null && http.data !== "" ) {
            $this.addServerError(http.data);         
        } else if(http.status <= 0) {
        	$this.addUnknownError();
        }
    };

    /**
     * Manage the exception
     */
    $this.manageException = function(error) {
    	if(error.message != null)
    		$this.addServerError(error);
    	else
    		$this.addError(error);
    };

    // Return message handler
    return $this;
}]);
