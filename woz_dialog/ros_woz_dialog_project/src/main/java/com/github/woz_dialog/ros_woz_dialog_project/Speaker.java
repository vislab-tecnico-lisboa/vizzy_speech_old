package com.github.woz_dialog.ros_woz_dialog_project;

import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import com.github.ekumen.rosjava_actionlib.ActionClientListener;

import actionlib_msgs.GoalStatusArray;

/**
 * Created by avelino on 22-05-2017.
 */

public class Speaker extends AbstractNodeMain implements ActionClientListener{

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("woz_dialog/speaker");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("chatter", std_msgs.String._TYPE);
        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String message) {
                log.info("I heard: \"" + message.getData() + "\"");
            }
        });
    }

    @Override
    public void resultReceived(Message message) {

    }

    @Override
    public void feedbackReceived(Message message) {

    }

    @Override
    public void statusReceived(GoalStatusArray goalStatusArray) {

    }
}