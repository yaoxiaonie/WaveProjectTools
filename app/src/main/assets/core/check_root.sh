if [[ "$(id -u 2>&1)" == "" ]] || [[ "$UID" == "0" ]] || [[ "$(whoami 2>&1)" == "root" ]] || [[ "$(set | grep 'USER_ID=0')" == "USER_ID=0" ]]; then
    echo "success"
else
    if [[ -d /cache ]]; then
        echo 1 > /cache/get_root
        if [[ -f "/cache/get_root" ]] && [[ "$(cat /cache/get_root)" == "1" ]]; then
            echo "success"
            rm -rf "/cache/get_root"
            return 0
        fi
    fi
    exit 1
fi 
 