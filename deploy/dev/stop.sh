#!/bin/bash

echo "[DEV] Stopping application"

PID=$(pgrep -f 'java.*app.jar' | head -n 1)

if [ -n "$PID" ]; then
  kill -15 "$PID"

  for i in {1..20}
  do
    if ! ps -p "$PID" > /dev/null
    then
      echo "Application stopped"
      exit 0
    fi

    sleep 1
  done

  kill -9 "$PID"
fi
