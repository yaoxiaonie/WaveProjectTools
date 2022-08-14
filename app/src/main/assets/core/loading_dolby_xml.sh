#!/system/bin/sh

mount -o rw,remount /data
mount -o rw,remount /vendor

function getDolbyStatus() {
    if [ "$(ls /data/vendor/dolby)" != "" ]; then
        echo 1
    else
        echo 0
    fi
}

function setDolbyStatus() {
    if [ "$1" = "true" ]; then
        cp -frp /vendor/etc/dolby/* /data/vendor/dolby/
    elif [ "$1" = "false" ]; then
        rm -rf /data/vendor/dolby/*
    fi
}

case "$1" in
get)
    getDolbyStatus
    ;;
set)
    setDolbyStatus "$2"
esac