---
title: 扫描插件
description: 扫描插件 cordova 版本.
---
<!--

# license: Licensed to the Apache Software Foundation (ASF) under one

#         or more contributor license agreements.  See the NOTICE file

#         distributed with this work for additional information

#         regarding copyright ownership.  The ASF licenses this file

#         to you under the Apache License, Version 2.0 (the

#         "License"); you may not use this file except in compliance

#         with the License.  You may obtain a copy of the License at

#

#           http://www.apache.org/licenses/LICENSE-2.0

#

#         Unless required by applicable law or agreed to in writing, 

#         software distributed under the License is distributed on an

#         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY

#         KIND, either express or implied.  See the License for the

#         specific language governing permissions and limitations

#         under the License.
-->

# cordova-plugin-hdhescanplug

## 安装方式

#### 现只支持 本地离线安装：

``` 
$ npm install -g plugman
$ plugman install --platform android --project platforms\android --plugin HdheScanPlug(插件路径)
```

## 支持版本

* Android 4.0.0 or above

## 示例

### 扫描数据

``` js
HdheScanPlug.start({
    type: 0, //0：代表读数据
    isMulti: false, // 是否多标签模式
    isTid: false, // 是否tid
    onReadSuccess: function(info) { //扫描数据成功回调
        // info = {data:'',rssi:''}
    },
    onReadFail: function() { //扫描数据失败回调

    },
});
```

### 写入数据

``` js
HdheScanPlug.start({
    type: 1, //1:代表写数据
    selectEpc: '', //选择EPC
    writeData: '', //写的数据
    writeDateType: 0, // 0:reserved 1:epc 2:tid  3:user  
    writeLength: 15, //写的长度
    writeStartPos: 0, //起始地址
    writePass: 000000, //访问密码
    onWriteSuccess: function() {
        //写入成功的回调
    },
    onWriteFail: function(error) {
        //写入失败的回调
    },
});
```

### 设置功率与区域

``` js
HdheScanPlug.start({
    type: 2, //2：代表读设置信息
    setting: {
        readPower: 33, //读功率
        writePower: 33, //写功率
        powerLocation: '', //频率区域
        onSettingSuccess: function() {
            //设置成功回调
        },
        onSettingFail: function() {
            //设置失败回调
        }
    }
});
```

### 读取设置信息

``` js
HdheScanPlug.start({
    type: 3, //3:读取设置信息
    onReadSettingSuccess: function(info) {
        var obj = JSON.parse(info);
        var readPower = obj.readPower; //读功率
        var writePower = obj.writePower; //写功率
        var powerLocation = obj.freRegion; //区域
    }
});
```

### 读取写的数据信息
``` js
HdheScanPlug.start({
    type: 4, //4：读取写的数据信息
    selectEpc: '',     //选择EPC
    writeData: '',     //写的数据
    writeDateType: 0,  // 0:reserved 1:epc  2:tid  3:user  写数据区
    writeLength: 15,   //写的长度
    writeStartPos: 0,  //起始地址
    writePass: '',     //访问密码
    onWriteReadSuccess: function (info) {
        //读取写的数据成功回调
    },
    onWriteReadFail: function () {
        //读取写的数据失败回调
    },
});   

```