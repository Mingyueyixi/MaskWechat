#!/bin/bash

# 一些循环执行的命令定义，避免代码推拉不了。例如，你可以这样推送代码直到成功：
# ./loop.sh "git push"

#command="git push --set-upstream origin feature/dev_download"
#command="git push"
#输入的参数
command=$1
if [ -z "$command" ]; then
    echo "Command is empty, please enter a command:"
    read command
fi

# 开始循环执行命令
while true; do
    # 执行命令并获取返回状态
    eval $command
    retval=$?

    # 检查命令是否成功
    if [ $retval -eq 0 ]; then
        echo "Command executed successfully."
        break
    else
        echo "Command failed with return code: $retval. Retrying..."
        sleep 2  # 等待2秒后重试
    fi
done
