#! /bin/bash

# for local dev
java --enable-preview -cp out Main \
        30000 localhost 25565 $HOME/minecraft-server/start-server.sh $HOME/minecraft-server $HOME/minecraft-server/logs/server-script.logs