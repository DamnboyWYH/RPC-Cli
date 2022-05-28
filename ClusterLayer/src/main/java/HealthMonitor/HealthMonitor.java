package HealthMonitor;

import io.netty.channel.Channel;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HealthMonitor {
    private static final int HeartBeatFail = 5;
    private static final int ServiceFail = 2;

    class health{
        private int healthPointer;
        private int heartBeatFailTime;
        private boolean death;

        public health() {
            healthPointer = 0;
            heartBeatFailTime = 0;
            death = false;
        }

        public void OutOfTime(long startTime, long endTime, int limitTime){
            if(endTime - startTime >= limitTime){
                heartBeatFailTime++;
                healthPointer += heartBeatFailTime * HeartBeatFail;
            }
            if(heartBeatFailTime >= 3){
                death = true;
            }
        }
        public boolean isDeath(){
            return death;
        }


    }
    private static final Map<Channel,health> healthCounter = new ConcurrentHashMap<>();//负责记录节点的健康状况。

    public void newChannelHealthMonitor(Channel channel){
        healthCounter.put(channel,new health());
    }


}
