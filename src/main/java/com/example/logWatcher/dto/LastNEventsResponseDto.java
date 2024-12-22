package com.example.logWatcher.dto;

import lombok.Data;

import java.util.List;

@Data
public class LastNEventsResponseDto {
       private List<String> events ;
       private String message ;
       private int code ;
}
