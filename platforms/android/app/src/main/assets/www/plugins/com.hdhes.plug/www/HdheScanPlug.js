cordova.define("com.hdhes.plug.HdheScanPlug", function(require, exports, module) {



    var exec = require('cordova/exec');
    var cordova = require('cordova');

    //设置信息
    var option = {
        type:0, // 0:read epc  1:  2:write
        isMulti:false,
        isTid:false,
        selectEpc:"",     //选择EPC
        writeData:"",     //写的数据
        writeDateType:3,  // 0:reserved 1:epc  2:tid  3:user  写数据区
        writeLength:15,   //写的长度
        writeStartPos:0,  //起始地址
        writePass:"",     //访问密码
        onReadSuccess:function(info){   //读成功回调

        },
        onReadFail:function(error){  // 读失败回调

        },
        onWriteSuccess:function(){  //写成功回调

        },
        onWriteFail:function(error){  //写失败回调

        },
        onWriteReadSuccess:function(info){

        },
        onWriteReadFail:function(){

        },

        setting:{
            readPower:33, //读功率
            writePower:33, //写功率
            powerLocation:"RG_美国", //频率区域
            onSettingSuccess:function(){
            },
            onSettingFail:function(){
            }
        }
    }

    var Scan = function () {

//        cordova.addWindowEventHandler('listenerData'); //注册
        this.type = 0; // 0:read epc 1:write  2:设置  3:读设置信息
        this.isMulti = false;
        this.isTid = false;

      //  cordova.addWindowEventHandler('listenerData');

        //
        this.onReadSuccess = null;
        this.onReadFail = null;

        //
        this.onWriteSuccess = null;
        this.onWriteFail = null;


        //
        this.onSettingSuccess = null;
        this.onSettingFail = null;

        //
        this.onReadSettingSuccess = null;

        //
        this.writeData = null;
        this.writeType = 0; // 0:reserved 1:epc  2:tid  3:user

        //
        this.onWriteReadSuccess = null;
        this.onWriteReadFail = null;
    };

    Scan.prototype.start = function(option){
        if(this.onReadSuccess ){
            return;
        }
        this.type = option.type;
        //设置读的回调
        this.onReadSuccess = option.onReadSuccess;
        this.onReadFail = option.onReadFail;
        //设置写的回调
        this.onWriteSuccess = option.onWriteSuccess;
        this.onWriteFail = option.onWriteFail;
        //设置的回调
        this.onSettingSuccess = option.onSettingSuccess;
        this.onSettingFail = option.onSettingFail;
        //
        this.onWriteReadSuccess = option.onWriteReadSuccess;
        this.onWriteReadFail = option.onWriteReadFail;
        //
        this.onReadSettingSuccess = option.onReadSettingSuccess;

        exec(scan._status, scan._error, 'HdheScanPlug', 'start', [option]);
    }

    Scan.prototype.stop = function(){
        this.onReadSuccess  = null;
        exec(null, null, 'HdheScanPlug', 'stop', []);
    }

    Scan.prototype._status = function (info) {
        console.log("info:"+info);
        if (info) {
            if(info.responseType === 0){ // 读成功
                if(scan.onReadSuccess){
                    scan.onReadSuccess(info);
                }
                cordova.fireWindowEvent('listenerData', info);
            }else if(info.responseType === 1){ //读失败
                scan.onReadFail();
            }else if(info.responseType === 2){ //写成功
                scan.onWriteSuccess();
            }else if(info.responseType === 3){ //写失败
                scan.onWriteFail();
            }else if(info.responseType === 4){ //设置成功
                scan.onSettingSuccess();
            }else if(info.responseType === 5){ //设置失败
                scan.onSettingFail();
            }else if(info.responseType === 6){ //ReadData成功
                scan.onWriteReadSuccess(info.data);
            }else if(info.responseType === 7){ //ReadData失败
                scan.onWriteReadFail();
            }else if(info.responseType === 8){
                scan.onReadSettingSuccess(info.data);
            }
        }
    };
    var scan = new Scan();
    exec(null, null, 'HdheScanPlug', 'register', []);
    module.exports = scan;
});
