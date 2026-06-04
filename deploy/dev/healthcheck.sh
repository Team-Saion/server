#!/bin/bash

echo "[DEV] Health Check Start"

PID=$(pgrep -f 'java.*app.jar' | head -n 1)

if [ -z "$PID" ]; then
  echo "Application process not found"
  exit 1
fi

PORT=$(ss -lntp 2>/dev/null | grep "$PID" | awk '{print $4}' | awk -F: '{print $NF}' | head -n 1)

if [ -z "$PORT" ]; then
  echo "Listening port not found"
  exit 1
fi

echo "Detected Port : $PORT"

for retry in {1..20}
do
  RESPONSE=$(curl -s http://localhost:${PORT}/actuator/health)

  if echo "$RESPONSE" | grep -q '"status":"UP"'
  then
      echo "Health Check Success"
      exit 0
  fi

  sleep 5
done

echo "Health Check Fail"
exit 1
