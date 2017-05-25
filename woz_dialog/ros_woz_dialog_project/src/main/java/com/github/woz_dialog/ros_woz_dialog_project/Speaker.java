package com.github.woz_dialog.ros_woz_dialog_project;


import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;

import com.github.ekumen.rosjava_actionlib.*;

import actionlib_msgs.GoalID;

import woz_dialog_msgs.SpeechActionGoal;
import woz_dialog_msgs.SpeechActionFeedback;
import woz_dialog_msgs.SpeechActionResult;
import woz_dialog_msgs.SpeechGoal;


import java.io.File;
import java.io.IOException;
import java.lang.Runtime;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * Created by João Avelino on 22-05-2017.
 */

public class Speaker extends AbstractNodeMain implements ActionServerListener<SpeechActionGoal>{


    boolean nodeRunning = true;
    Log log;
    private String VOICE = "Joana";
    private String LANGUAGE = "por-PRT";


    TTSHTTPClient ttshttpClient = null;




    private ActionServer<SpeechActionGoal, SpeechActionFeedback, SpeechActionResult> as = null;
    private volatile SpeechActionGoal currentGoal = null;



    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("woz_dialog/speaker");
    }

    @Override
    public void onStart(ConnectedNode node) {
        log = node.getLog();


        //SpeechActionResult result;
        //String id;
        //String errorMsg;

        as = new ActionServer<SpeechActionGoal, SpeechActionFeedback,
                SpeechActionResult>(node, "/woz_dialog/speaker", SpeechActionGoal._TYPE,
                SpeechActionFeedback._TYPE, SpeechActionResult._TYPE);

        as.attachListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){

                nodeRunning = false;

                System.out.println("Shuting down...");
            }
        });


/*
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

            sleep(100); //This shouldn't be done this way, but wtv. I'm out of time.


        }*/

    }

    boolean doTTS(String language, String voice, String speed, String message)
    {

        //Audio file name and path
        //Get the file path
        String fullpath = System.getProperty("user.home");
        fullpath = fullpath + "/wav/" + LANGUAGE + "/" + VOICE + "/" + message.toLowerCase() + ".wav";

        //Check if file exists
        File fileDir = new File(fullpath);

        if(fileDir.exists())
        {

            System.out.println("File found. Playing it...");
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(fileDir);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

        }else
        {
            if(ttshttpClient == null || LANGUAGE != language || VOICE != voice)
            {


                LANGUAGE = language;
                VOICE = voice;

                System.out.println("Voice: " + VOICE);

                ttshttpClient = new TTSHTTPClient("NMDPTRIAL_joao_manuel_avelino_gmail_com20160926153657",
                        "31a53782c0a2f73cb0bcff27a56511f818c92672c708db096a9d1423a24533d3ea6b55ab0d4ce7bd08e7e5ad4cf6a565bfcbd786d4b38f3e50a461f2e994c5d4",
                        VOICE, LANGUAGE);


            }

            log.info("Couldn't play file. Synthesising...");


            try {
                ttshttpClient.synthesise(message);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }



    @Override
    public void goalReceived(SpeechActionGoal speechActionGoal) {
        System.out.println("Speech Goal received.");
        currentGoal = speechActionGoal;

        SpeechActionResult result;
        result = as.newResultMessage();

        String id;
        String errorMsg;

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