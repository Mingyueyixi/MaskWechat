# MaskWechat

[Source link / 源码地址](https://github.com/Mingyueyixi/MaskWechat)

反馈问题可点击源码地址，发起issues


## 介绍
这是一个微信 Xposed 模块，她可以隐藏掉特定用户的聊天记录，防止私密的聊天被第三人偷看


## 使用说明

### 添加配置

1.  激活模块，作用域勾选微信
2.  在模块App中点击`添加配置`卡片
3.  跳转到微信主页（若微信本身不在首页，请自行返回主页后操作），点击用户发起聊天
4.  模块会抓取糊脸Id（微信用户的唯一id），并弹出对话框。
5.  确认后，点击对话框确定按钮。再次进入聊天页即隐藏与此用户的聊天记录

### 临时解除隐藏（Since v1.6）

1.6版本： 

在聊天记录空白处，连续点击5次以上，每次点击间隔不超过150毫秒（超过则重新计数），则解除隐藏


### 清除配置

通过`配置管理`清除即可


PS.

- 模块仅仅是隐藏了视图，不对用户数据进行修改
- 模块目前仅隐藏聊天记录，防止被偷窥，而不会”伪装“或修改好友/群组信息，此类功能暂时不会添加
- 模块正常只隐藏主页相关消息，不包括通知栏等渠道的消息，有较强隐私需求用户，建议关闭微信的`通知显示消息详情`

## 适配版本

8.0.22 (2140) 2022-04-29    
8.0.32 (2300) 2023-01-06    

PS.
- 其他版本号以及32位版本未经测试，不保证功能可用
- 模块一般只测试通过了最后一个适配的微信版本，因为作者精力有限+穷没有多余手机测试
- 微信版本集合： https://weixin.qq.com/cgi-bin/readtemplate?lang=zh_CN&t=weixin_faq_list&head=true


## 交流

QQ群：点击添加 [549614926](https://qm.qq.com/cgi-bin/qm/qr?k=J884tv29im41_SuTo1Hm_gapAL6gBySJ&authKey=6jUdcjVCpYgDGmLsZmtkCtxJQY+oas0RACQ6vS9E+4xpMRB6858C/OMLhlSKxZRC&noverify=0)

CI编译telegram频道:点击添加 [MaskWechatCI](https://t.me/MaskWechatCI)


## 声明

1. App旨在学习开发，请勿用于非法用途
2. App不会主动收集任何个人信息  

