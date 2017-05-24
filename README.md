# vizzy_speech

Software for dialogue with Vizzy


# Message generation (BUG)

Due to a bug in genjava that makes message generation from actions troublesome (or impossible), the custom messages are provided in a jar file on the external_libs folder. Right now the woz_dialog_msgs package is generating the messages directly from the msg folder. If you wish to change the action edit the SpeechFeedback.msg, SpeechGoal.msg and SpeechResult.msg files instead of the Speech.action file. That file will only be useful when this bug is corrected...
