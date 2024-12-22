package com.example.logWatcher.controller;


import com.example.logWatcher.dto.LastNEventsResponseDto;
import com.example.logWatcher.service.FileReaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final FileReaderService fileReaderService;
    public EventController(FileReaderService fileReaderService) {
        this.fileReaderService = fileReaderService;
    }

    @PostConstruct
    public void init() {
        System.out.println("EventController is initialized!");
    }
    @GetMapping("/")
    public ResponseEntity<LastNEventsResponseDto> get10Lines(){
           return fileReaderService.getLastNLinesFromFile(10);
    }

}
