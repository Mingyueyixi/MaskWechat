# MaskWechat

[Source link / 项目地址](https://github.com/Mingyueyixi/MaskWechat)

反馈问题可点击以上地址，发起issues


## 介绍
这是一个微信 Xposed 模块，她可以隐藏掉特定用户的聊天记录，防止私密的聊天被第三人偷看


## 使用说明

### 添加配置

1.  激活模块，作用域勾选微信
2.  在模块App中点击`添加配置`卡片
3.  跳转到微信主页（若微信本身不在首页，请自行返回主页后操作），点击用户发起聊天
4.  模块会抓取糊脸Id（微信用户的唯一id），并弹出对话框。
5.  确认后，点击对话框确定按钮。再次进入聊天页即隐藏与此用户的聊天记录

### 2.0版本变更

1. 全局配置移动不再个人弹窗中出现，而是移动到了配置中心
2. 配置中心打开路径为：“微信设置->关怀模式->开启按钮（长按）”


### 临时解除隐藏（Since v1.6）

v1.6版本：  
在聊天记录空白处，连续点击5次以上（不包含5次），每次点击间隔不超过150毫秒（超过则重新计数），则解除隐藏

v1.8版本：  
实现了自定义点击次数与时间间隔

**PS.**  
150毫秒/次，意味着一秒内需要点击6次（普通人极限手速），才能解开，如果你无法解开，不用怀疑，就是手速太慢了，去修改时间间隔吧

### 搜索列表隐藏特定用户所在行（Since V1.7）

v1.7版本新增功能（预计**Only For 8.0.32**）当Wechat主页发起的搜索结果命中糊脸ID时，将隐藏所在行视图


### 清除配置

通过`配置管理`清除即可


**PS.**  
- 模块仅仅是隐藏了视图，不对用户数据进行修改
- 模块目前仅隐藏聊天记录，防止被偷窥，而不会”伪装“或修改好友/群组信息，此类功能暂时不会添加
- 模块正常只隐藏主页相关消息，不包括通知栏等渠道的消息，有较强隐私需求用户，建议关闭微信的`通知显示消息详情`


### 隐藏App图标（Since V1.13）

v1.13版本开始，默认隐藏App在桌面的图标。隐藏以后，打开模块App可以通过以下途径：    
1. 从 lsp 模块管理器打开App
2. 从网页链接（deeplink）打开App

**PS.**   
- App主页跳转链接：[maskwechat://com.lu.wxmask](maskwechat://com.lu.wxmask)  
- 小米系统可通过系统的”扫一扫“扫描上述链接对应的二维马直接打开（请自行去生成一个吧），其他app如浏览器需可点击短链打开：[https://sourl.cn/sPfEeY](https://sourl.cn/sPfEeY)，可以保存为书签  
- 微信/QQ不支持通过此类链接跳转  
- 自行配置 html 如：`<a href="maskwechat://com.lu.wxmask">maskwechat://com.lu.wxmask</a>`  

## 适配版本

8.0.22 (2140) 2022-04-29  
8.0.32 (2300) 2023-01-06  
8.0.33 (2320) 2023-02-23  
8.0.34 (2340) 2023-03-23  
8.0.35 (2340) 2023-04-20 (PS. 不是作者写错，而是特么下载到了一个和8.0.34版本号一样的8.0.35)   
8.0.35 (2360) 2023-04-20  
8.0.37 (2380) 2023-05-25   
8.0.38 (2400) 2023-06-21
8.0.40 (2420) 2023-07-20
8.0.40 (2420) 2023-07-20    
8.0.41 (2441) 2023-09-06
8.0.42 (2460) 2023-09-22
8.0.43 (2480) 2023-11-06
8.0.44 (2502) 2023-12-04
8.0.45 (2521) 2024-01-02
8.0.46 (2540) 2024-01-23
8.0.47 (2560) 2024-02-01

**PS.**
- 仅支持上述版本，所有其他版本号以及32位版本未经测试，预计百分之九十九不可用
- 模块一般只测试通过了最后一个适配的微信版本，因为作者精力有限+穷没有多余手机测试
- 微信更新记录官网： https://weixin.qq.com/cgi-bin/readtemplate?lang=zh_CN&t=weixin_faq_list&head=true

如果你版本是受支持的，但无法正常使用，请参考以下问题：

**问题1：为什么我的微信版本是8.0.32，但是无法正常使用，什么反应都没有？**  
答：微信实际上特么的有很多个8.0.32版本，而你的版本悲剧了不支持。在官网上，截止当前时间（2023/02/24），作者能找到的8.0.32就有8个版本下载链接：  
（1）[https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64.apk)  
（2）[https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_1.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_1.apk)   
（3）[https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_2.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_2.apk)  
（4）[https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_3.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_3.apk)  
（5）[https://dldir1.qq.com/weixin/android/weixin8032android2300.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300.apk)  
（6）[https://dldir1.qq.com/weixin/android/weixin8032android2300_1.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_1.apk)  
（7）[https://dldir1.qq.com/weixin/android/weixin8032android2300_2.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_2.apk)  
（8）[https://dldir1.qq.com/weixin/android/weixin8032android2300_3.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_3.apk)  

**问题2：为什么我的微信版本是8.0.32，仍然弹出不支持的版本对话框？**  
答：原因类似问题1，只不过你的微信版本号不是2300，仅仅是版本名称叫8.0.32，而这、导致模块不支持。由此可见，8.0.32的安装包也远不止问题1所列。假如你能正常使用，但是弹窗，请反馈版本号以便去除弹窗。


**问题3：上面列举了那么多个下载链接，到底下载那个？**  
答：以下链接来自官网：  
8.0.22（2140）：[https://dldir1.qq.com/weixin/android/weixin8022android2140_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8022android2140_arm64.apk)  
SHA1: 2FAB9BF8E160F38494FE7D6D4D7A56DF63B6EB58  

8.0.32（2300）：[https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_3.apk](https://dldir1.qq.com/weixin/android/weixin8032android2300_arm64_3.apk)  
SHA1: 45A408C5222C1A03D3B7C84F06DA97AD2F5B4ADC  

8.0.33（2320）：[https://dldir1.qq.com/weixin/android/weixin8033android2320_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8033android2320_arm64.apk)  
SHA1: 8069F4730CF8839BE68609EF2F4702349E23A86B  

8.0.34（2340）: [https://dldir1.qq.com/weixin/android/weixin8034android2340_arm64_1.apk](https://dldir1.qq.com/weixin/android/weixin8034android2340_arm64_1.apk)  
SHA1: 30D0D0C25561D367A9E359A1804EA90352BEA7F5  

8.0.35 (2360): [https://dldir1.qq.com/weixin/android/weixin8035android2360_arm64_3.apk](https://dldir1.qq.com/weixin/android/weixin8035android2360_arm64_3.apk)  
SHA1: 5593FB24667D44ABD2299EF1118CD3498099B719  

8.0.37（2380）：[https://dldir1.qq.com/weixin/android/weixin8037android2380_arm64_1.apk](https://dldir1.qq.com/weixin/android/weixin8037android2380_arm64_1.apk)  
SHA1: 410E675B0014F6DF768825F647F69D98A110D50D   

8.0.38（2400）：[https://dldir1.qq.com/weixin/android/weixin8038android2400_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8038android2400_arm64.apk)  
SHA1: F62FAD64F70F1181EC62DD3BD796A047028078FA  

8.0.40（2420）：[https://dldir1.qq.com/weixin/android/weixin8040android2420_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8040android2420_arm64.apk)  
SHA1: BCCA3CCACE5F40184A42FEFB06190C7279024985  

8.0.41（2441）：[https://dldir1.qq.com/weixin/android/weixin8041android2441_arm64_1.apk](https://dldir1.qq.com/weixin/android/weixin8041android2441_arm64_1.apk)    
SHA1: 51D3E1C9594723FE8A69B68780C4B561964C7718  

8.0.42（2460）：[https://dldir1.qq.com/weixin/android/weixin8042android2460_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8042android2460_arm64.apk)    
SHA1: 227E395C67A2C0B0BCC750E1A3C52F642B433441    

8.0.43（2480）：[https://dldir1.qq.com/weixin/android/weixin8043android2480_0x28002b35_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8043android2480_0x28002b35_arm64.apk)    
SHA1: C46C85AF05130EDABCDBA8D487A5373ECE4AE6D0

8.0.44（2502）：[https://dldir1.qq.com/weixin/android/weixin8044android2502_0x28002c36_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8044android2502_0x28002c36_arm64.apk)    
SHA1: 38525994D6D69106CDB3D6F9F62B045CFF9CC4D5     

8.0.45（2521）：[https://dldir1.qq.com/weixin/android/weixin8045android2521_0x28002d34_arm64_1.apk](https://dldir1.qq.com/weixin/android/weixin8045android2521_0x28002d34_arm64_1.apk)    
SHA1: F44F35663E2A2C3BF9EA671270D65902AB5727DA    

8.0.46（2540）: [https://dldir1.qq.com/weixin/android/weixin/android/weixin8046android2540_0x28002e34_arm64.apk](https://dldir1.qq.com/weixin/android/weixin/android/weixin8046android2540_0x28002e34_arm64.apk)    
SHA1: 173D8632093949D7AB1DA6D8B8CB5C1252876BEB    

8.0.47（2560）: [https://dldir1.qq.com/weixin/android/weixin8047android2560_0x28002f36_arm64.apk](https://dldir1.qq.com/weixin/android/weixin8047android2560_0x28002f36_arm64.apk)    
SHA1: 79F1341563A9CCCAF3090D27A5E9D529008EEC42    


推荐适配的最后两个版本，因为其他版本，作者自己不再使用

**问题4：是否支持Google Play版本？**  
答：不支持。  
（1）如前所述，微信的版本是如此之多，作者没有充足的时间和精力，只能随便摇骰子选一个来适配，而Google Play版没摇中  
（2）Google Play版并非主流使用版本，故未来也没有计划支持Google Play版  


## 交流

CI编译telegram频道（频道不能聊天）： 点击添加 [https://t.me/MaskWechatCI](https://t.me/MaskWechatCI)

telegram普通群：点击添加 [https://t.me/MaskWechatX](https://t.me/MaskWechatX)

## 声明

1. 项目旨在个人测试与学习开发，请勿用于商业用途，请勿用于非法用途  
2. 项目所发布的所有App版本，虽名为release，实际均为开发包，均使用同一个测试签名，因此它将不会在应用市场发布  
3. 项目只保证自身不会包含任何恶意代码，不会主动收集任何个人信息，但不能保证第三方库安全  
4. 您应当知道并理解使用`模块`的风险，使用此模块如造成问题与作者无关  
5. 您只有在清楚并同意本声明的情况下，才可使用本项目的App  
