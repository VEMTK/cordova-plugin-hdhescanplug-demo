cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "com.hdhes.plug.HdheScanPlug",
      "file": "plugins/com.hdhes.plug/www/HdheScanPlug.js",
      "pluginId": "com.hdhes.plug",
      "clobbers": [
        "cordova.plugins.HdheScanPlug"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "com.hdhes.plug": "1.0"
  };
});