# SmartFarm

智慧农场 | 用户移动端 | Android APP | MQTT | IoT

## 应用特点

1. 用户注册及登录后端采用**Flask**框架，数据库为**Redis**，增加Token认证机制，相关代码见[server/flaskServer.py](https://github.com/zys91/SmartFarm/blob/main/server/flaskServer.py)，需自行配置参数；

2. 用户注册中手机号短信验证操作采用**MobSDK**，需自行申请API_Key进行配置，教程详见<http://www.mob.com/wiki/detailed?wiki=SMSSDK_for_Android_kuaisujicheng>；

3. 传感数据采用MQTT协议传输，采用 [EMQX](https://github.com/emqx/emqx) 作为网关，利用Webhook设置数据桥接规则，将传感数据接入**ClickHouse**时序数据库，配合**Grafana**做数据展示与分析，并且APP端可以查看历史数据图表；

4. 封装了一些常见的传感器设备的显示控制UI与接口，方便二次开发使用；

5. 完善启动页视频播放，随机展示视频片段；

6. 支持设备扫码实现绑定关联与解绑操作，以及设备在线状态的实时显示；

7. 采用**Nginx**作为网页前端和资源文件获取地址，实现检查更新与在线更新机制。

## 界面展示

![](img\Snipaste_1.png)

![Snipaste_2](img\Snipaste_2.png)

![Snipaste_3](img\Snipaste_3.png)

![Snipaste_4](img\Snipaste_4.png)

## 注意事项

此仓库为智慧农场移动客户端(用户侧)，需与物联网传感终端配合食用，终端侧仓库[传送门](https://github.com/zys91/IoT_Farm_ESP32S3)

## 致谢

1. [EMQX](https://github.com/emqx/emqx)