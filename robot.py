#!/usr/bin/python

# -*- coding: utf-8 -*-
# @Date:2023/02/13 0:06
# @Author: Lu
# @Description
import argparse
import html
import json
import mimetypes
import os
import subprocess
from enum import Enum
from pathlib import Path

import requests

http = requests.Session()


def read_file(file_path, default=b''):
    result = default
    try:
        with open(file_path, "rb") as f:
            result = f.read()
    except Exception as e:
        print(e)
    return result


def json_loads(text, default={}):
    result = default
    try:
        result = json.loads(text)
    except Exception as e:
        print(e)
        pass
    return result


def run_cmd(cmd, prt=True):
    if prt:
        print(cmd)
    p = subprocess.Popen(args="sh",
                         shell=True,
                         text=False,
                         start_new_session=True,
                         stdin=subprocess.PIPE,
                         stdout=subprocess.PIPE)
    p.stdin.write(cmd.encode('utf-8') + b"\n")
    p.stdin.close()
    bin_result = p.stdout.read()
    p.stdout.close()
    p.kill()
    result = bin_result.decode('utf-8')
    if prt:
        print(result)
    return result


def test_cmd():
    git.head_commit
    print(git.head_commit_msg)


class GitMan(object):
    @property
    def head_commit(self):
        return run_cmd("git rev-parse HEAD").strip()

    @property
    def curr_branch(self):
        return run_cmd("git branch --show-current").strip()

    @property
    def head_commit_msg(self):
        # 此命令没有问题，但目前在github action环境执行拿不到
        # return run_cmd("git log --pretty=format:"%s" HEAD~..HEAD").strip()
        # 也没有问题，但报错，将head命令当做git参数解析了
        # return run_cmd('git log  --pretty=format:"%s" head -n 1').strip()
        try:
            # 可能git版本问题，这个命令未裁剪返回也只有一条
            return run_cmd('git log  --pretty=format:"%s"').strip().splitlines()[0]
        except:
            return ''


class GithubApi(object):
    def __init__(self, token):
        self.token = token

    def getReleases(self, tag):
        result = None
        try:
            result = met.github_release
        except Exception as e:
            print(e)
            print(met.github_release)
        if not result:
            url = f"https://api.github.com/repos/Mingyueyixi/MaskWechat/releases/tags/{tag}"
            headers = {
                "Accept": "application/vnd.github+json",
            }
            if self.token:
                # github 对ip请求有限制，超出频次将得不到请求结果，需要设置auth
                headers["Authorization"] = self.token
            # api.github不需要走代理。代理服务器通常请求超量，需要auth
            res = requests.get(url, headers=headers)
            print(url, res.content.decode('utf-8'))
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
        self.bot_user = None
        self.token = None
        self.base_url = None
        self.doInit(user, token)

    def doInit(self, user, token):
        self.bot_user = user
        self.token = token
        self.base_url = f"https://api.telegram.org/bot{self.token}"
        return self

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
        print("sendMessage result:", res.text)

    def sendMediaGroup(self, chatId, media_paths: [], caption=None):
        # 这是开源封装好的工具类
        # https://github.com/python-telegram-bot/python-telegram-bot
        # 以下是自行实现的多媒体组消息发送方法，根据telegrame文档：
        # https://core.telegram.org/bots/api#sendMediaGroup
        # https://core.telegram.org/bots/api#inputmediadocument
        # 根据文档，已经存在的文件则使用file_id即可
        media_arr = []
        files = {}
        for i in range(len(media_paths)):
            m = Path(media_paths[i]).resolve()
            print(m, os.path.exists(m))
            name = os.path.basename(m)
            # 名称过长无法发送
            attach_name = f"attach_{id(name)}"
            content_type = mimetypes.guess_type(m)[0]
            if not content_type:
                if m.suffix == '.apk':
                    content_type = 'application/vnd.android.package-archive'
                else:
                    content_type = 'application/octet-stream'

            files[attach_name] = (name, read_file(m), content_type)
            media_item = {
                "type": "document",
                "media": f"attach://{attach_name}",
            }

            media_arr.append(media_item)

        # 添加说明
        if caption:
            # 说明文字超过长度将无法发送
            sub = len(caption) - 1024
            if sub > 0:
                caption = caption[: -sub - 3] + "……"
            if media_arr:
                media_arr[-1]["caption"] = caption

        res = http.post(
            f"{self.base_url}/sendMediaGroup",
            data={
                "chat_id": chatId,
                "media": json.dumps(media_arr),
            },
            files=files
        )
        print(media_arr)
        print("sendMediaGroup result:", res.content.decode('utf-8'))
        return res


def escapeMsgMarkdownV2(text: str):
    result = text
    for s in ['_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!']:
        result = result.replace(s, "\\" + s)
    return result


class TGNotification(object):
    def __init__(self, tg_bot: TGBot):
        self.tg_bot = tg_bot

    def postRelease(self):
        release_data = GithubApi(met.github_token).getReleases(met.tag_name)
        html_url = release_data["html_url"]
        body = html.escape(release_data['body']).strip()
        name = release_data["name"]

        mess = f"\n<b>{name}发布：</b>{html_url}\n{body}"
        mess += "\n<b>产物下载：</b>\n"
        for ass in release_data.get("assets"):
            ass_name = ass["name"]
            download_url = ass["browser_download_url"]
            mess += f'<a href="{download_url}">{ass_name}</a>\n'
        print(mess)
        self.tg_bot.sendMessage(met.chat_id, mess, ParseMode.HTML)

    def postCI(self):
        github_commits = met.github_event_commits
        github_run_url = met.github_run_url

        body = ''
        print("github_commits", type(github_commits), github_commits)

        for g_commit in github_commits:
            g_msg = g_commit.get('message')
            if not g_msg: g_msg = ""
            body += g_msg.strip() + "\n"
        if not body:
            body += met.git_commit_msg
        mess = f"糊脸CI提示\n"
        mess += f"编译地址：{github_run_url}\n"
        mess += f"代码分支：{met.git_branch}\n"
        mess += f"提交哈希：{met.git_commit[:6]}\n"
        mess += f"更新信息：{body}"
        res = self.tg_bot.sendMediaGroup(met.chat_id, caption=mess, media_paths=filter_apk_paths())
        if res.status_code == 200:
            res_json = res.json()
            if res_json['ok']:
                return
        self.tg_bot.sendMessage(met.chat_id, f"CI编译完成{github_run_url}")


def filter_apk_paths(verify_commit=False):
    apk_dir = project_path / "app/build/outputs/apk/"
    short = met.git_commit[:6]
    if verify_commit:
        result = [e for e in apk_dir.glob(f"*/*{short}*.apk")]
    else:
        result = [e for e in apk_dir.glob(f"*/*.apk")]
    return result


def main():
    parser = argparse.ArgumentParser(description='Mask Robot')
    parser.add_argument('-e', '--event', metavar='pattern', required=True,
                        dest='event', action='store',
                        help='the event for robot')
    args = parser.parse_args()
    print(f"python {__file__} by --event {args.event}")
    bot = TGBot("@MaskCIBot", met.bot_token)
    if args.event == "ci" or not args.event:
        TGNotification(bot).postCI()
    elif args.event == "release":
        TGNotification(bot).postRelease()
    elif args.event == "shabi":
        met.bot_token = bytes(
            [53, 57, 55, 50, 48, 54, 51, 52, 53, 49, 58, 65, 65, 70, 52, 98, 72, 98, 103, 72, 68, 88, 84, 112, 85, 103,
             120,
             110, 74, 55, 100, 69, 103, 86, 55, 57, 111, 73, 50, 80, 49, 113, 67, 89, 49, 119]).decode('utf-8')
        # 和ShabiGo机器人的聊天id
        met.chat_id = 5318101494
        bot.doInit("@ShabiGo", met.bot_token)
        TGNotification(bot).postCI()
    else:
        print("not support robot")


class Meta(object):
    def __init__(self):
        self.github_context = json_loads(read_file('github_context.txt'), {})
        self.github_event = self.github_context.get('event', {})
        self.github_event_commits = self.github_event.get("commits", [])

        self.tag_name = os.environ.get("tag_name", "")
        self.github_token = os.environ.get("github_token", "")
        self.chat_id = os.environ.get("CHAT_ID", "")
        self.bot_token = os.environ.get("BOT_TOKEN", "")

        self.github_run_url = os.environ.get("GITHUB_RUN_URL", "")
        self.github_release = os.environ.get("event", {}).get("release", {})
        self.git_branch = os.environ.get("GIT_BRANCH", git.curr_branch)
        self.git_commit = os.environ.get("GIT_COMMIT", git.head_commit)
        self.git_commit_msg = self.github_event.get('head_commit', {}).get('message', git.head_commit_msg)

    def __str__(self):
        return json.dumps(self.__dict__)


project_path = Path(__file__).parent
git = GitMan()
met = Meta()
print(met)


def setup_test():
    http.proxies = {
        "http": "127.0.0.1:7890",
        "https": "127.0.0.1:7890"
    }
    met.github_context = json_loads(read_file('test_data/git_context.json'), {})
    met.bot_token = bytes(
        [53, 57, 55, 50, 48, 54, 51, 52, 53, 49, 58, 65, 65, 70, 52, 98, 72, 98, 103, 72, 68, 88, 84, 112, 85, 103, 120,
         110, 74, 55, 100, 69, 103, 86, 55, 57, 111, 73, 50, 80, 49, 113, 67, 89, 49, 119]).decode('utf-8')
    # 和ShabiGo机器人的聊天id
    met.chat_id = 5318101494
    # met.github_token =
    tg = TGBot("@ShabiGo_bot", met.bot_token)
    return tg


def test_postRelease():
    tg = setup_test()
    met.tag_name = 'v1.15'
    TGNotification(tg).postRelease()


def test_sendMediaGroup():
    # tg.getChannelChatId()
    tg = setup_test()
    met.tag_name = 'v1.15'
    release_data = GithubApi(met.github_token).getReleases(met.tag_name)
    # 测试发送apk、txt
    tg.sendMediaGroup(met.chat_id, ["local.properties", "gradle.properties"], f"新版本发布\n{release_data['body']}")

    # bot = TGBot()
    # await bot.sendMediaGroup(
    #     met.chat_id,
    #     media=[
    #         InputMediaDocument(media=open(project_path / 'test1.txt'), filename="test1.txt"),
    #         InputMediaDocument(media=open(project_path / 'test2.txt'), filename="test2.txt")
    #     ],
    #     caption="更新橙瓜"
    # )


# @pytest.mark.asyncio
def test_postCI():
    tg = setup_test()
    met.github_run_url = 'https://github.com/Mingyueyixi/MaskWechat/actions/runs/4709147817'
    TGNotification(tg).postCI()


if __name__ == '__main__':
    main()
