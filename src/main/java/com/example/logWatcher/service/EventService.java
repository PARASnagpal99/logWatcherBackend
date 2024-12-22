package com.example.logWatcher.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EventService {
       @Autowired
       private final NotificationService notificationService;
       @Autowired
       public EventService(NotificationService notificationService){
           this.notificationService = notificationService;
       }
       public void publishEvent(String event){
              System.out.println("PUBLISHING EVENT : " + event);
              notificationService.sendNotification("/topic/events" , event);
       }
}
