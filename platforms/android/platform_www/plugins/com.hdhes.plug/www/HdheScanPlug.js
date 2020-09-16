cordova.define("com.hdhes.plug.HdheScanPlug", function(require, exports, module) {

    var exec = require('cordova/exec');
    var cordova = require('cordova');

    var Scan = function () {
        this._level = null;
        this._isPlugged = null;
        // Create new event handlers on the window (returns a channel instance)
        this.channels = {
            scanresult: cordova.addWindowEventHandler('scanresult')
        };
        for (var key in this.channels) {
            this.channels[key].onHasSubscribersChange = Scan.onHasSubscribersChange;
        }
    };

    function handlers () {
        return (
        //+ battery.channels.batterylow.numHandlers + battery.channels.batterycritical.numHandlers
            battery.channels.batterystatus.numHandlers
        );
    }

    Scan.onHasSubscribersChange = function(){
        if (this.numHandlers === 1 && handlers() === 1) {
            exec(scan._status, scan._error, 'HdheScanPlug', 'tagInventoryByTime', []);
        } else if (handlers() === 0) {
            exec(null, null, 'HdheScanPlug', 'stop', []);
        }
    }

    Scan.prototype._status = function (info) {
        if (info) {
              // Something changed. Fire batterystatus event
             cordova.fireWindowEvent('scanresult', info);
        }
    };

//exports.showToast = function (arg0, success, error) {
//    exec(success, error, 'HdheScanPlug', 'showToast', [arg0]);
//};


    var scan = new Scan();

    module.exports = scan;

});
