from __future__ import print_function
import rospy


# Brings in the SimpleActionClient
import actionlib

import woz_dialog_msgs.msg


def test_action():


    client = actionlib.SimpleActionClient('/woz_dialog/speaker', woz_dialog_msgs.msg.SpeechAction)

    client.wait_for_server()

    goal = woz_dialog_msgs.msg.SpeechGoal(language="por-PRT", voice="Joana", message="Epa funciona caracas, ja estou farto disto")

    client.send_goal(goal)

    client.wait_for_result()

    return client.get_result() 

if __name__ == '__main__':
    try:
        # Initializes a rospy node so that the SimpleActionClient can
        # publish and subscribe over ROS.

        rospy.init_node('speech_client_py')
        result = test_action()
        print("Result:", result.success)
    except rospy.ROSInterruptException:
        print("program interrupted before completion", file=sys.stderr)

