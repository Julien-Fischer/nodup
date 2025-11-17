#!/usr/bin/env bash

uninstall_nodup() {
    read -r -p  "This will uninstall nodup. Proceed? (Y/n):" user_input
    user_input=${user_input:-Y}
    if [[ "${user_input,,}" =~ ^(y|yes)$ ]]; then
        sudo rm -rf ~/nodup
        sudo rm -rf /usr/local/bin/nodup
    else
        echo "Uninstall aborted."
    fi  
}

uninstall_nodup "$@"
