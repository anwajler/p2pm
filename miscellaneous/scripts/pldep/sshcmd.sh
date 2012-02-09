#!/bin/bash

while getopts ":c:u:h:" opt; do
  case $opt in
    c) command=$OPTARG ;;
    u) username=$OPTARG ;;
    h) hostname=$OPTARG ;;
    \?) exit 1 ;;
    :) exit 1 ;;
  esac
done

if [ -f ~/.agent.env ] ; then
    . ~/.agent.env > /dev/null
    if ! kill -0 $SSH_AGENT_PID > /dev/null 2>&1; then
        eval `ssh-agent | tee ~/.agent.env` > /dev/null 2>&1
        ssh-add ~/.ssh/id_?*[^pub$]? > /dev/null 2>&1
    fi
else
    eval `ssh-agent | tee ~/.agent.env` > /dev/null 2>&1
    ssh-add ~/.ssh/id_?*[^pub$]? > /dev/null 2>&1
fi

ssh -t -q -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $username@$hostname "$command"
