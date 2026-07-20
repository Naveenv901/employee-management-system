package com.example.demo.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.service.MailService;

@Component
public class Scheduler {

    @Autowired
    private MailService mailService;

    private static final Logger  logger = LoggerFactory.getLogger(Scheduler.class);
    
    @Scheduled(fixedRate=60000)
    public void sendDailyMail() {
        logger.info("Running scheduled task: runing every 1 minute");
        // mailService.sendMail(
        //     "naveenvajjiravel@gmail.com",
        //     "Test Email from Spring Scheduler",
        //     "Hello! This is a sample email."
        // );
    }
}