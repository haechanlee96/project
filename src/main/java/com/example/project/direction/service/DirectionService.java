package com.example.project.direction.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.direction.entity.Direction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    public List<Direction> buildDirectionList(DocumentDto documentDto){
        // 약국 데이터 조회


        // 거리계산 알고리즘 이용하여, 고객과 약국 사이의 거리를 계산하고 sort
    }


    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
}
