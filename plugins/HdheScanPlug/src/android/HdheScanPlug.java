package com.hdhes.plug;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.pda.serialport.Tools;

/**
 * This class echoes a string called from JavaScript.
 */
public class HdheScanPlug extends CordovaPlugin {

    private final String TAG = HdheScanPlug.class.getSimpleName();
    //    public static UHFRManager mUhfrManager = null;
    private SharedPreferences mSharedPreferences;
    private ScanUtil instance;
    private boolean isStart = false;
    private final int what_do_tag_inventory_by_time = 0x001;
    private final int what_tag_inventory_by_time_result = 0x002;
    private CallbackContext callbackContext;
    private UHFRManager mUhfrManager = UHFRManager.getInstance();// Init Uhf module


    private BroadcastReceiver keyReceiver;

    //任务类型的参数
    private int type = 0;
    //private int membank ;//read or write memory bank

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (what == what_do_tag_inventory_by_time) {
                Bundle bundle = msg.getData();
                boolean isMulti = bundle.getBoolean("isMulti");
                boolean isTid = bundle.getBoolean("isTid");
                tagInventoryByTime(isMulti, isTid);
                if (isStart) {
                    Message message = new Message();
                    message.what = what_do_tag_inventory_by_time;
                    Bundle b = msg.getData();
                    b.putBoolean("isMulti", isMulti);
                    b.putBoolean("isTid", isTid);
                    message.setData(b);
                    handler.sendMessageDelayed(message, 500);
//                    handler.sendMessage(message);
                }
            }
            if (what == what_tag_inventory_by_time_result) {
                Bundle b = msg.getData();
                String data = b.getString("data");
                String rssi = b.getString("rssi");
                sendResponse(data, rssi, 0, null);
            }
            return false;
        }
    });


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        SoundUtil.initSoundPool(webView.getContext());
        this.callbackContext = callbackContext;
        //tagInventoryByTime
        //showToast
        Log.d(TAG, "execute:args：" + args.toString());
        //int type = 0;//执行任务类型 // 0:read epc  1:  2:write
        if (action.equals("start")) {
            //读的参数
            boolean isMulti = false;
            boolean isTid = false;

            //写的参数
            String selectEpc = "";
            int writeDateType = 0;  //0:reserved 1:epc  2:tid  3:user  写数据区
            String writeData = "";  //写的数据
            int writeLength = 15;  //写的长度
            int writeStartPos = 0;  //起始地址
            String writePass = "";  //访问密码

            //设置的参数
            int readPower = 33; //读功率
            int writePower = 33; //写功率
            String powerLocation = "RG_美国";

            if (args.length() == 0) {
                Toast.makeText(cordova.getContext(), "args error.", Toast.LENGTH_SHORT).show();
                return false;
            }

            JSONObject optionJsonObject = args.getJSONObject(0);
            if (!optionJsonObject.has("type")) {
                Toast.makeText(cordova.getContext(), "args error. type is null", Toast.LENGTH_SHORT).show();
                return false;
            }
            type = optionJsonObject.getInt("type");
            switch (type) {
                case 0:
                    if (optionJsonObject.has("isMulti")) {
                        isMulti = optionJsonObject.getBoolean("isMulti");
                    }
                    if (optionJsonObject.has("isTid")) {
                        isTid = optionJsonObject.getBoolean("isTid");
                    }
                    readInventory(isMulti, isTid);
                    break;
                case 1:
                    if (optionJsonObject.has("selectEpc")) {
                        selectEpc = optionJsonObject.getString("selectEpc");
                    }
                    if (optionJsonObject.has("writeDateType")) {
                        writeDateType = optionJsonObject.getInt("writeDateType");
                    }
                    if (optionJsonObject.has("writeData")) {
                        writeData = optionJsonObject.getString("writeData");
                    }
                    if (optionJsonObject.has("writeLength")) {
                        writeLength = optionJsonObject.getInt("writeLength");
                    }
                    if (optionJsonObject.has("writeStartPos")) {
                        writeStartPos = optionJsonObject.getInt("writeStartPos");
                    }
                    if (optionJsonObject.has("writePass")) {
                        writePass = optionJsonObject.getString("writePass");
                    }

                    writeData(String.valueOf(writeStartPos), writePass, writeData, selectEpc, writeDateType);

                    break;
                case 2:

                    //{"type":2,"setting":{"readPower":"29","writePower":"32","powerLocation":"RG_欧洲2"}}
                    if (optionJsonObject.has("setting")) {
                        JSONObject settingJsonObj = optionJsonObject.getJSONObject("setting");
                        if (settingJsonObj.has("readPower")) {
                            readPower = settingJsonObj.getInt("readPower");
                        }
                        if (settingJsonObj.has("writePower")) {
                            writePower = settingJsonObj.getInt("writePower");
                        }
                        if (settingJsonObj.has("powerLocation")) {
                            powerLocation = settingJsonObj.getString("powerLocation");
                        }

                        Log.d(TAG, "setting:readPower： " + readPower);
                        Log.d(TAG, "setting:writePower： " + writePower);
                        Log.d(TAG, "setting:powerLocation： " + powerLocation);

                        setting(readPower, writePower, powerLocation);

                    } else {
                        Toast.makeText(cordova.getContext(), "args error. setting params value error", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case 3:
                    //读设置信息
                    JSONObject settingInfo = getSetting();

                    Log.d(TAG, "setting:" + settingInfo.toString());

                    sendResponse(settingInfo.toString(), null, 8, null);
                    break;
                case 4:
                    if (optionJsonObject.has("selectEpc")) {
                        selectEpc = optionJsonObject.getString("selectEpc");
                    }
                    if (optionJsonObject.has("writeDateType")) {
                        writeDateType = optionJsonObject.getInt("writeDateType");
                    }
                    if (optionJsonObject.has("writeData")) {
                        writeData = optionJsonObject.getString("writeData");
                    }
                    if (optionJsonObject.has("writeLength")) {
                        writeLength = optionJsonObject.getInt("writeLength");
                    }
                    if (optionJsonObject.has("writeStartPos")) {
                        writeStartPos = optionJsonObject.getInt("writeStartPos");
                    }
                    if (optionJsonObject.has("writePass")) {
                        writePass = optionJsonObject.getString("writePass");
                    }
                    readData(String.valueOf(writeStartPos), writePass, writeData, selectEpc, writeDateType, String.valueOf(writeLength));
                    break;

                default:
                    Toast.makeText(cordova.getContext(), "args error. type value error", Toast.LENGTH_SHORT).show();
                    return false;
            }


            return true;
        } else if (action.equals("stop")) {
            Log.d(TAG, "execute: stop");
            isStart = false;
            // mUhfrManager.asyncStopReading();
            return true;
        } else if (action.equals("register")) {
            if (Build.VERSION.SDK_INT == 29) {
                instance = ScanUtil.getInstance(webView.getContext());
                instance.disableScanKey("134");
            }
            if (mUhfrManager != null) {
                Reader.READER_ERR err = mUhfrManager.setPower(mSharedPreferences.getInt("readPower", 33), mSharedPreferences.getInt("writePower", 33));//set uhf module power
                if (err == Reader.READER_ERR.MT_OK_ERR) {
                    mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                    Toast.makeText(webView.getContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
                            "\n" + "Read Power:" + mSharedPreferences.getInt("readPower", 33) +
                            "\n" + "Write Power:" + mSharedPreferences.getInt("writePower", 33), Toast.LENGTH_LONG).show();
//                showToast(getString(R.string.inituhfsuccess));
                } else {
                    Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
                    if (err1 == Reader.READER_ERR.MT_OK_ERR) {
                        mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                        Toast.makeText(webView.getContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
                                "\n" + "Read Power:" + 30 +
                                "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                    } else {
                        //showToast(getString(R.string.inituhffail));
                        showToast("初始化失败!");
                    }
                }
            } else {
                // showToast(getString(R.string.inituhffail));
                showToast("初始化失败!");
            }
            return true;
        }
        return false;

        // if (action.equals("coolMethod")) {
        //     String message = args.getString(0);
        //     this.coolMethod(message, callbackContext);
        //     return true;
        // }
        // return false;
    }

    /**
     * write tag memory bank
     */
    private void writeData(String startAddrStr, String accessStr, String writeStr, String selectEpc, int writeDateType) {
     /*   String startAddrStr = editStart.getText().toString().trim() ;
        String accessStr = editAccess.getText().toString().trim() ;
        String writeStr = editWriteData.getText().toString().trim() ;*/
        if (startAddrStr == null || startAddrStr.length() == 0) {
            showToast("请输入起始地址");
            return;
        }
        if (accessStr == null || accessStr.length() != 8) {
            showToast("请输入4字节访问密码");
            return;
        }
        if (writeStr == null || writeStr.length() == 0) {
            showToast("请写入数据");
            return;
        }
        byte[] writeDataBytes = null;
        try {
            writeDataBytes = Tools.HexString2Bytes(writeStr);
            if (writeDataBytes.length % 2 != 0) {
                showToast("写入数据的格式不正确");
                return;
            }
        } catch (Exception e) {
            showToast("写入数据的格式不正确!");
            return;
        }
        int addr = Integer.parseInt(startAddrStr);
        byte[] epcBytes = Tools.HexString2Bytes(selectEpc);
        byte[] accessBytes = Tools.HexString2Bytes(accessStr);

        Reader.READER_ERR er;
/*
        if (checkBoxFilter.isChecked())
            //change epc:
//			er = MainActivity.mUhfrManager.writeTagEPCByFilter(writeDataBytes, accessBytes,(short)1000, epcBytes,1, 2,true);
            //write data
            er = mUhfrManager.writeTagDataByFilter((char) writeDateType, addr, writeDataBytes, writeDataBytes.length, accessBytes, (short) 1000, epcBytes, 1, 2, true);
        else
            //change epc:
//			er = MainActivity.mUhfrManager.writeTagEPC(writeDataBytes,accessBytes,(short) 1000);
            //write data:
            er = MainActivity.mUhfrManager.writeTagData((char) membank, addr, writeDataBytes, writeDataBytes.length, accessBytes, (short) 1000);

*/
        /// membank=writeDateType;
//        er = mUhfrManager.writeTagDataByFilter((char) writeDateType, addr, writeDataBytes, writeDataBytes.length, accessBytes, (short) 1000, epcBytes, 1, 2, true);

        er = mUhfrManager.writeTagData((char) writeDateType, addr, writeDataBytes, writeDataBytes.length, accessBytes, (short) 1000);

        if (er == Reader.READER_ERR.MT_OK_ERR) {
            Log.d(TAG, "writeData: er1:" + er);
            sendResponse(null, null, 2, null);
        } else {
            Log.d(TAG, "writeData: er2:" + er);
            sendResponse(null, null, 3, null);
        }
    }

    /**
     * read tag memory bank data
     */
    private void readData(String startAddrStr, String accessStr, String writeStr, String selectEpc, int writeDateType, String lengthStr) {

      /*  String startAddrStr = editStart.getText().toString().trim() ;
        String lengthStr = editLength.getText().toString().trim() ;
        String accessStr = editAccess.getText().toString().trim() ;*/
        if (startAddrStr == null || startAddrStr.length() == 0) {
            showToast("请输入起始地址");
            return;
        }

        if (lengthStr == null || lengthStr.length() == 0) {
            showToast("请输入长度");
            return;
        }

        if (accessStr == null || accessStr.length() != 8) {
            showToast("请输入4字节访问密码");
            return;
        }

        byte[] writeDataBytes = null;
        try {
            writeDataBytes = Tools.HexString2Bytes(writeStr);
            if (writeDataBytes.length % 2 != 0) {
                showToast("写入数据的格式不正确");
                return;
            }
        } catch (Exception e) {
            showToast("写入数据的格式不正确!");
            return;
        }
        int addr = Integer.valueOf(startAddrStr);
        int len = Integer.valueOf(lengthStr);
        byte[] epcBytes = Tools.HexString2Bytes(selectEpc);
        byte[] accessBytes = Tools.HexString2Bytes(accessStr);
        byte[] readBytes = new byte[len * 2];
        Reader.READER_ERR er = Reader.READER_ERR.MT_OK_ERR;
      /*  if (checkBoxFilter.isChecked()){
            //fbank: 1 epc,2 tid ,3 user
            readBytes = mUhfrManager.getTagDataByFilter((char)writeDateType, addr, len, accessBytes, (short) 1000, epcBytes, 1, 2, true);
        }
        else {
            er = MainActivity.mUhfrManager.getTagData(membank, addr, len, readBytes, accessBytes, (short) 1000);
        }*/
        er = mUhfrManager.getTagData((char) writeDateType, addr, len, readBytes, accessBytes, (short) 1000);
        if (er == Reader.READER_ERR.MT_OK_ERR && readBytes != null) {
            sendResponse(Tools.Bytes2HexString(readBytes, readBytes.length), null, 6, null);
            //addTips(getResources().getString(R.string.read_success_) + Tools.Bytes2HexString(readBytes, readBytes.length)) ;
        } else {
            //addTips(getResources().getString(R.string.read_fail_)) ;
            sendResponse(null, null, 7, null);
        }
    }


    /**
     * 读
     *
     * @param isMulti
     * @param isTid
     */
    private void readInventory(boolean isMulti, boolean isTid) {
        isStart = true;
        Message message = new Message();
        message.what = what_do_tag_inventory_by_time;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMulti", isMulti);
        bundle.putBoolean("isTid", isTid);
        message.setData(bundle);
        handler.sendMessage(message);
        // Don't return any result now, since status results will be sent when events come in from broadcast receiver
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }


    private List<Reader.TAGINFO> tagInventoryByTime(boolean isMulti, boolean isTid) {
        Log.d(TAG, "tagInventoryByTime: 开始扫描");
        if (mUhfrManager == null) {
            Log.d(TAG, "tagInventoryByTime: mUhfrManager 为空");
            return null;
        }
        String data = null;
        List<Reader.TAGINFO> list1 = null;

        Log.d(TAG, "tagInventoryByTime: isMulti:" + isMulti + "  isTid:" + isTid);

        if (isMulti) {
            Log.d(TAG, "tagInventoryByTime: isMulti");
            mUhfrManager.setGen2session(true);
            mUhfrManager.asyncStartReading();
            list1 = mUhfrManager.tagInventoryRealTime();
        } else {
            Log.d(TAG, "tagInventoryByTime: 111111:");
            mUhfrManager.asyncStopReading();
            if (isTid) {
                Log.d(TAG, "tagInventoryByTime: isTid");
                list1 = mUhfrManager.tagEpcTidInventoryByTimer((short) 50);
            } else {
                Log.d(TAG, "tagInventoryByTime: default");
                list1 = mUhfrManager.tagInventoryByTimer((short) 50);
            }
        }

        SoundUtil.play(1, 0);
        if (list1 != null && list1.size() > 0) {
            for (Reader.TAGINFO tfs : list1) {
                byte[] epcdata = tfs.EpcId;
                if (isTid) {
                    data = Tools.Bytes2HexString(tfs.EmbededData, tfs.EmbededDatalen);
                } else {
                    data = Tools.Bytes2HexString(epcdata, epcdata.length);
                }
                int rssi = tfs.RSSI;
                Message msg = new Message();
                msg.what = what_tag_inventory_by_time_result;
                Bundle b = new Bundle();
                b.putString("data", data);
                b.putString("rssi", rssi + "");
                Log.d(TAG, "execute:data：" + data);
                Log.d(TAG, "execute:rssi：" + rssi);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }
        return list1;
    }


    private JSONObject getSetting() {


        JSONObject jsonObject = new JSONObject();
        try {
            mSharedPreferences = webView.getContext().getSharedPreferences("UHF", Context.MODE_PRIVATE);
            jsonObject.put("readPower", mSharedPreferences.getInt("readPower", 33));
            jsonObject.put("writePower", mSharedPreferences.getInt("writePower", 33));
            int location = mSharedPreferences.getInt("freRegion", 0);
            switch (location) {
                case 0:
                    jsonObject.put("freRegion", "RG_无");
                    break;
                case 1:
                    jsonObject.put("freRegion", "RG_美国");
                    break;
                case 2:
                    jsonObject.put("freRegion", "RG_欧洲");
                    break;
                case 3:
                    jsonObject.put("freRegion", "RG_韩国");
                    break;
                case 6:
                    jsonObject.put("freRegion", "RG_中国");
                    break;
                case 7:
                    jsonObject.put("freRegion", "RG_欧洲2");
                    break;
                case 8:
                    jsonObject.put("freRegion", "RG_欧洲3");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * @param readPower
     * @param writePower
     * @param powerLocation
     */
    private void setting(int readPower, int writePower, String powerLocation) {
        mSharedPreferences = webView.getContext().getSharedPreferences("UHF", Context.MODE_PRIVATE);
        Reader.READER_ERR err = mUhfrManager.setPower(readPower, writePower);
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            showToast("功率设置成功");
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putInt("readPower", readPower);
            mEditor.putInt("writePower", writePower);
            mEditor.apply();
        } else {
            showToast("功率设置失败");
        }
        Reader.Region_Conf currentFreRegion = null;
        switch (powerLocation) {
            case "RG_中国":
                currentFreRegion = Reader.Region_Conf.RG_PRC;
                break;
            case "RG_美国":
                currentFreRegion = Reader.Region_Conf.RG_NA;
                break;
            case "RG_无":
                currentFreRegion = Reader.Region_Conf.RG_NONE;
                break;
            case "RG_韩国":
                currentFreRegion = Reader.Region_Conf.RG_KR;
                break;
            case "RG_欧洲":
                currentFreRegion = Reader.Region_Conf.RG_EU;
                break;
            case "RG_欧洲2":
                currentFreRegion = Reader.Region_Conf.RG_EU2;
                break;
            case "RG_欧洲3":
                currentFreRegion = Reader.Region_Conf.RG_EU3;
                break;
        }

        if (currentFreRegion != null) {
            err = mUhfrManager.setRegion(currentFreRegion);
        }

        if (err == Reader.READER_ERR.MT_OK_ERR) {
            showToast("功率区域设置成功");
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            if (currentFreRegion != null) {
                mEditor.putInt("freRegion", currentFreRegion.value());
            }
            mEditor.apply();
            sendResponse(null, null, 4, null);
        } else {
            showToast("功率区域设置失败");
            sendResponse(null, null, 5, null);
        }
    }


    private void sendResponse(String data, String rssi, int responseType, String errorMsg) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", type); //0: 1: 2:
            jsonObject.put("responseType", responseType); //0: 1: 2: 3: 4: 5: 6:
            jsonObject.put("data", data);
            jsonObject.put("rssi", rssi);
            jsonObject.put("errorMsg", errorMsg);
            Log.d(TAG, "sendResponse: " + jsonObject.toString());
            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
//            if (type == 0) {
            result.setKeepCallback(true);
//            } else {
//                result.setKeepCallback(false);
//            }
            this.callbackContext.sendPluginResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (mUhfrManager == null) {
            mUhfrManager = UHFRManager.getInstance();
        }
    }


    @Override
    public void onStart() {


    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
        isStart = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUhfrManager != null) {
            mUhfrManager.close();
            mUhfrManager = null;
        }
        isStart = false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        isStart = false;
    }


    private Toast mToast;

    //show toast
    @SuppressLint("ShowToast")
    private void showToast(String info) {
        if (mToast == null) {
            mToast = Toast.makeText(webView.getContext(), info, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(info);
        }
        mToast.show();
    }


}
