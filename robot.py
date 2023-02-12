# -*- coding: utf-8 -*-
# @Date:2023/02/13 0:06
# @Author: Lu
# @Description
import argparse
import os

import requests

http = requests.Session()


class ReleaseRobot(object):

    def post(self):
        url = f"https://api.github.com/repos/Mingyueyixi/MaskWechat/releases/tags/{tag_name}"
        headers = {
            "Accept": "application/vnd.github+json",
        }
        if github_token:
            headers["Authorization"] = github_token

        res = http.get(url, headers=headers)
        print(url, res.text)
        json_data = res.json()
        if not json_data:
            raise Exception(f"{url} response is empty")

        mess = f"新版本{json_data['name']}发布： {json_data['html_url']}\n"
        for ass in json_data.get("assets"):
            mess += "下载：" + ass["browser_download_url"] + "\n"

        print(mess)
        res = http.post(f"https://api.telegram.org/bot{bot_token}/sendMessage", json={
            "chat_id": chat_id,
            "text": mess
        })
        print(res.text)


def main():
    parser = argparse.ArgumentParser(description='Mask Robot')
    parser.add_argument('-e', '--event', metavar='pattern', required=True,
                        dest='event', action='store',
                        help='the event for robot')
    args = parser.parse_args()
    print(f"python {__file__} by --event {args.event}")
    if args.event == "release":
        ReleaseRobot().post()
    else:
        print("not support robot")


if __name__ == '__main__':
    tag_name = os.environ.get("TAG_NAME")
    github_token = os.environ.get("github_token")
    chat_id = os.environ.get("chat_id")
    bot_token = os.environ.get("bot_token")

    tag_name = "v1.11"
    main()
