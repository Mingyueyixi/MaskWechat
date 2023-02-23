# -*- coding: utf-8 -*-
# @Date:2023/02/13 0:06
# @Author: Lu
# @Description
import argparse
import base64
import html
import json
import os
from enum import Enum

import requests

http = requests.Session()


class GithubApi(object):
    def __init__(self, token):
        self.token = token

    def getReleases(self, tag):
        result = github_release
        if not result:
            url = f"https://api.github.com/repos/Mingyueyixi/MaskWechat/releases/tags/{tag}"
            headers = {
                "Accept": "application/vnd.github+json",
            }
            if self.token:
                headers["Authorization"] = self.token

            res = http.get(url, headers=headers)
            print(url, res.text)
            result = res.json()
            if not result:
                raise Exception(f"{url} response content is empty")
        return result


class ParseMode(Enum):
    HTML = "HTML"
    Markdown = "Markdown"
    MarkdownV2 = "MarkdownV2"


class TGBot(object):
    def __init__(self, user, token):
        self.bot_user = user
        self.token = token
        self.base_url = f"https://api.telegram.org/bot{self.token}"

    def getChannelChatId(self):
        """
        在群组中@username机器人并发消息后，通过getUpdates接口获取事件，其中有chatId
        频道和群组的响应体不一样，这是获取频道的ChatId
        """
        res = http.get(f"{self.base_url}/getUpdates")
        res_json = res.json()
        print(json.dumps(res_json, ensure_ascii=False))
        if res_json['ok']:
            for ele in res_json['result']:
                try:
                    chatId = ele['channel_post']['chat']['id']
                    if chatId:
                        return chatId
                except Exception:
                    pass
        return None

    def sendMessage(self, chatId, text: str, parse_mode: ParseMode = None, disable_preview=False):
        data = {
            "chat_id": chatId,
            "text": text,
            "disable_web_page_preview": disable_preview
        }
        if parse_mode:
            data["parse_mode"] = parse_mode.value

        res = http.post(f"{self.base_url}/sendMessage", json=data)
        print(res.text)


def escapeMsgMarkdownV2(text: str):
    result = text
    for s in ['_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!']:
        result = result.replace(s, "\\" + s)
    return result


class ReleaseNotification(object):
    def __init__(self, tg_bot: TGBot):
        self.tg_bot = tg_bot

    def post(self):
        release_data = GithubApi(github_token).getReleases(tag_name)
        html_url = release_data["html_url"]
        body = html.escape(release_data['body']).strip()
        name = release_data["name"]

        mess = f'{html_url}'
        mess += f"\n<b>{name}发布：</b>\n{body}"
        mess += "\n<b>产物下载：</b>\n"
        for ass in release_data.get("assets"):
            ass_name = ass["name"]
            download_url = ass["browser_download_url"]
            mess += f'<a href="{download_url}">{ass_name}</a>\n'
        print(mess)
        self.tg_bot.sendMessage(chat_id, mess, ParseMode.HTML)


def main():
    parser = argparse.ArgumentParser(description='Mask Robot')
    parser.add_argument('-e', '--event', metavar='pattern', required=True,
                        dest='event', action='store',
                        help='the event for robot')
    args = parser.parse_args()
    print(f"python {__file__} by --event {args.event}")
    if args.event == "release":
        ReleaseNotification(TGBot("@ShabiGo_bot", bot_token)).post()
    else:
        print("not support robot")


tag_name = os.environ.get("tag_name")
github_token = os.environ.get("github_token")
chat_id = os.environ.get("chat_id")
bot_token = os.environ.get("bot_token")
github_release = os.environ.get("github_release")


def test_main():
    http.proxies = {
        "http": "127.0.0.1:7890",
        "https": "127.0.0.1:7890"
    }
    global tag_name, github_token, chat_id, bot_token
    tag_name = 'v1.14'
    bot_token = base64.decodebytes(b"NTk3MjA2MzQ1MTpBQUY0YkhiZ0hEWFRwVWd4bko3ZEVnVjc5b0kyUDFxQ1kxdw==").decode("utf-8")
    chat_id = 5318101494
    ReleaseNotification(TGBot("@ShabiGo_bot", bot_token)).post()


if __name__ == '__main__':
    main()
