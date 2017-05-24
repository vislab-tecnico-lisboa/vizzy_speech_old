package com.github.woz_dialog.ros_woz_dialog_project;


import org.omg.SendingContext.RunTime;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;

import com.github.ekumen.rosjava_actionlib.*;

import actionlib_msgs.GoalStatusArray;
import actionlib_msgs.GoalID;
import actionlib_msgs.GoalStatus;

import woz_dialog_msgs.SpeechActionGoal;
import woz_dialog_msgs.SpeechActionFeedback;
import woz_dialog_msgs.SpeechActionResult;
import woz_dialog_msgs.SpeechGoal;
import woz_dialog_msgs.SpeechResult;
import woz_dialog_msgs.SpeechFeedback;

import java.lang.Runtime;



/**
 * Created by João Avelino on 22-05-2017.
 */

public class Speaker extends AbstractNodeMain implements ActionServerListener<SpeechActionGoal>{


    boolean nodeRunning = true;


    private ActionServer<SpeechActionGoal, SpeechActionFeedback, SpeechActionResult> as = null;
    private volatile SpeechActionGoal currentGoal = null;



    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("woz_dialog/speaker");
    }

    @Override
    public void onStart(ConnectedNode node) {
        final Log log = node.getLog();

        SpeechActionResult result;
        String id;
        String errorMsg;

        as = new ActionServer<SpeechActionGoal, SpeechActionFeedback,
                SpeechActionResult>(node, "/woz_dialog/speaker", SpeechActionGoal._TYPE,
                SpeechActionFeedback._TYPE, SpeechActionResult._TYPE);

        as.attachListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){

                nodeRunning = false;

                System.out.println("Shuting down...");
                System.exit(0);
            }
        });

        while(nodeRunning) {
            if (currentGoal != null) {
                result = as.newResultMessage();

                String language = currentGoal.getGoal().getLanguage();
                String voice = currentGoal.getGoal().getVoice();
                byte speed = currentGoal.getGoal().getSpeed();
                String message = currentGoal.getGoal().getMessage();

                String speedStr;

                if (speed == SpeechGoal.FAST)
                    speedStr = "fast";
                else if (speed == SpeechGoal.VERYFAST)
                    speedStr = "x-fast";
                else if (speed == SpeechGoal.MEDIUM)
                    speedStr = "medium";
                else if (speed == SpeechGoal.SLOW)
                    speedStr = "slow";
                else if (speed == SpeechGoal.XSLOW)
                    speedStr = "xslow";
                else
                    speedStr = "default";


                result.getResult().setSuccess(doTTS(language, voice, speedStr, message));


                id = currentGoal.getGoalId().getId();
                as.setSucceed(id);
                as.setGoalStatus(result.getStatus(), id);
                System.out.println("Sending result...");
                as.sendResult(result);
                currentGoal = null;
            }

            sleep(100);
            System.out.println("Looping a lot... This is unthinkable and stupid!");
        }

    }

    boolean doTTS(String language, String voice, String speed, String message)
    {

        





        return true;
    }

    @Override
    public void goalReceived(SpeechActionGoal speechActionGoal) {
        System.out.println("Speech Goal received.");
    }

    @Override
    public void cancelReceived(GoalID goalID) {
        System.out.println("Cancel received.");
    }

    @Override
    public boolean acceptGoal(SpeechActionGoal speechActionGoal) {
        return false;
    }


    private void sleep(long msec) {
        try {
            Thread.sleep(msec);
        }
        catch (InterruptedException ex) {
        }
    }
}