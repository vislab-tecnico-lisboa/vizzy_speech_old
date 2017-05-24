#!/usr/bin/env sh

NODE=$1;

if [ ! "$NODE" ];
then
    NODE=Speaker;
fi

`rospack find woz_dialog`/ros_woz_dialog_project/build/install/ros_woz_dialog_project/bin/ros_woz_dialog_project com.github.woz_dialog.ros_woz_dialog_project.$NODE;
