package com.example.logWatcher.service;

import com.example.logWatcher.dto.LastNEventsResponseDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



@Service
public class FileReaderService  {
    @Autowired
    private final EventService eventService;
    private boolean shouldIRun = true;

    private long lastKnownPosition = 0;

    @Value("${log.dir.path}")
    private String directoryPath ;

    @Value("${log.file.path}")
    private String filePath ;


    public FileReaderService(EventService eventService) {
        this.eventService = eventService;
    }

    @PostConstruct
    public void onStart(){
        this.initializeLastKnownPosition();
        new Thread(this::readFileAndSendEvent).start();
    }

    public void stopRunning() {
        this.shouldIRun = false;
        System.out.println("Stopping the file reader service.");
    }

    public void readFileAndSendEvent() {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath , "r")) {
            while (shouldIRun) {
                Thread.sleep(1000);
                long currentLength = randomAccessFile.length() ;
                randomAccessFile.seek(lastKnownPosition);
                if(randomAccessFile.getFilePointer() < currentLength){
                    String newContent = randomAccessFile.readLine();
                    eventService.publishEvent("NEW CONTENT : " + newContent);
                }
                lastKnownPosition = currentLength;
            }
        } catch (Exception e) {
            System.out.println("ERROR READING file : " + e.getMessage());
            stopRunning();
        }
    }

    private void initializeLastKnownPosition(){
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath , "r")){
                lastKnownPosition = randomAccessFile.length();
                System.out.println("Initialized lastKnownPosition to: " + lastKnownPosition);
            }catch (Exception exception){
                System.out.println("Exception while initialising last known position : " + exception.getMessage());
            }
    }

    public ResponseEntity<LastNEventsResponseDto> getLastNLinesFromFile(int n) {
        LastNEventsResponseDto lastNEventsResponseDto = new LastNEventsResponseDto();
        try{
            String filePath = this.filePath;
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                    System.out.println("Line : " + line);
                    if (lines.size() > n) {
                        lines.removeFirst();
                    }
                }
            }
            Collections.reverse(lines);
            lastNEventsResponseDto.setEvents(lines);
            lastNEventsResponseDto.setCode(HttpStatus.OK.value());
            lastNEventsResponseDto.setMessage("Successfully Fetched last 10 Events");
            return new ResponseEntity<>(lastNEventsResponseDto , HttpStatus.OK);
        }catch (Exception exception){
            System.out.println("ERROR READING LAST N LINES : " + exception.getMessage());
            lastNEventsResponseDto.setMessage("ERROR READING LAST N LINES : " + exception.getMessage());
            lastNEventsResponseDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(lastNEventsResponseDto , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
