NotificationTextColorCompat
===============

Fetch the default system notification text color (ContentTitleColor and ContentTextColor) for your custom RemoteViews.

获取系统默认的通知文字颜色(标题和内容文字颜色)，适配自定义通知的RemoteViews中的文字。已测试兼容各种国产ROM。


![NotificationTextColorCompat](https://raw.github.com/Mixiaoxiao/NotificationTextColorCompat/master/README.png) 


Sample APK
-----

[NotificationTextColorCompat.apk](https://raw.github.com/Mixiaoxiao/NotificationTextColorCompat/master/NotificationTextColorCompat.apk)

Usage 用法
-----

```java
 NotificationTextColorCompat.byAuto(context).setContentTitleColor(,).setContentTextColor(,);
```

Device Test 机型测试
-----

```java
NotificationTextColorCompat.byAuto() | byText() | byId() | byAnyTextView() | bySdkVersion() 
```

|Device|byText|byId|byAnyTextView|bySdkVersion|
|---|:---|:---|:---:|:---:|
|Standard-Android4.4|√|√|√|√|
|Standard-Android6.0|√|√|√|√|
|MIUI5-Android4.1|√|√|√|√|
|MIUI7-Android5.0|√|√|-|×|
|MIUI8-Android5.0|√|√|-|-|
|Flyme5-Android5.1|√|×|-|×|

* √ : ContentTitleColor and ContentTextColor are both right
* - : ContentTitleColor and ContentTextColor are mainly right(right | drak text)
* × : ContentTitleColor and ContentTextColor are both wrong

* Welcome to add more device test and thanks a lot!
* 欢迎提交新的机型测试反馈！

Developed By
------------

Mixiaoxiao - <xiaochyechye@gmail.com> or <mixiaoxiaogogo@163.com>



License
-----------

    Copyright 2016 Mixiaoxiao

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
