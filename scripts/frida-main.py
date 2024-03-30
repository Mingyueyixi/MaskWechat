# coding=utf-8
# @Date:2024/03/03 18:47
# @Author: Lu
# @Description
import os
import re
import subprocess

import frida
import frida_tools.ps
import onceutils
from frida.core import Device, Session, Script


def on_message(message: dict, data):
    for k, v in message.items():
        print(f'\n{k}:{v}')


def main():
    devices = frida.enumerate_devices()
    if not devices:
        raise Exception('Not device connect')

    device: Device = devices[0]

    pkg = 'com.tencent.mm'
    cmd = f"adb shell ps|grep {pkg}"
    p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)

    # text = onceutils.bin2text(p.stdout)
    # pid = int(re.split(r"\s+", text)[1])
    pid = device.spawn([pkg])
    device.resume(pid)

    # process: Session = device.attach(pid)
    script: Script = None
    script_path: str = 'hook_db.js'
    while True:
        if script and not script.is_destroyed:
            script.unload()
        if not script_path:
            break

        with open(script_path, 'r', encoding='utf-8') as f:
            print(f'load hook script: {script_path}')
            script = process.create_script(f.read())
            script.on('message', on_message)
            script.load()
        script_path = input('输入新的Hook Script路径：').strip()


if __name__ == '__main__':
    main()
