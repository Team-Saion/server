#!/bin/bash

echo "[DEV] Starting application"

nohup java \
  -Xms128m \
  -Xmx256m \
  -jar /home/ec2-user/runtime/app.jar \
  --spring.profiles.active=dev \
  > /home/ec2-user/runtime/app.log 2>&1 < /dev/null &
