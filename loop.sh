#!/bin/bash

# 一些循环执行的命令定义，避免代码推拉不了。例如，你可以这样推送代码直到成功：
# ./loop.sh "git push"

#command="git push --set-upstream origin feature/dev_download"
#command="git push"
#输入的参数
command=$1 # 获取第一个参数作为命令
duration=$2  # 获取第二个参数作为等待时间

# 如果未提供命令，则提示用户输入
if [ -z "$command" ]; then
    echo "Command is empty, please enter a command:"
    read command
fi

# 如果未提供或duration为空，则默认为2秒
if [ -z "$duration" ]; then
    duration=2
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
        echo "Command failed with return code: $retval. Retrying in $duration seconds..."
        sleep $duration  # 使用提供的duration值等待
    fi
done
