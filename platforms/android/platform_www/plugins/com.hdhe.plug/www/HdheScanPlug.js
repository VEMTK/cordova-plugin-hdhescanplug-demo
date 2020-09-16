cordova.define("com.hdhe.plug.HdheScanPlug", function(require, exports, module) {
var exec = require('cordova/exec');

exports.showToast = function (arg0, success, error) {
    exec(success, error, 'HdheScanPlug', 'showToast', [arg0]);
};

});
