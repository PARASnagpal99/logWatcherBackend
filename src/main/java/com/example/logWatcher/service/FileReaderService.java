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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


@Service
public class FileReaderService  {

    @Autowired
    private final EventService eventService;

    public FileReaderService(EventService eventService) {
        this.eventService = eventService;
    }

    @PostConstruct
    public void onStart(){
          new Thread(this::readFileAndSendEvent).start();
    }

    @Value("${log.dir.path}")
    private String directoryPath ;

    @Value("${log.file.path}")
    private String filePath ;

    public void readFileAndSendEvent() {
        try {
            Path directoryPath = Paths.get(this.directoryPath);

            WatchService watchService = FileSystems.getDefault().newWatchService();

            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            System.out.println("Started watching directory with path : " + directoryPath);

            WatchKey key;

            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            eventService.publishEvent("File created: " + event.context());
                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            eventService.publishEvent("File deleted: " + event.context());
                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            eventService.publishEvent("File modified: " + event.context());
                        }
                    }
                key.reset();
                Thread.sleep(100);
            }

        } catch (Exception exception) {
            System.out.println("ERROR READING directory : " + exception.getMessage());
            exception.printStackTrace();
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
