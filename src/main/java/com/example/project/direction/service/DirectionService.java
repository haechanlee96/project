package com.example.project.direction.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.service.KakaoCategorySearchService;
import com.example.project.direction.entity.Direction;
import com.example.project.direction.repository.DirectionRepository;
import com.example.project.pharmacy.dto.PharmacyDto;
import com.example.project.pharmacy.entity.Pharmacy;
import com.example.project.pharmacy.service.PharmacySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private static final int MAX_SEARCH_COUNT = 3;// 3개 최대 검색 갯수
    private static final double RADIUS_KM = 10.0;// 반경 10km

    private final PharmacySearchService pharmacySearchService;
    private final DirectionRepository directionRepository;
    private final KakaoCategorySearchService kakaoCategorySearchService; // 약국 카테고리

    // db 저장
    @Transactional
    public List<Direction> saveAll(List<Direction> directionList){ // 최대 3개의 리스트를 반환
        if(CollectionUtils.isEmpty(directionList)) return Collections.emptyList();
        return directionRepository.saveAll(directionList);
    }

    public List<Direction> buildDirectionList(DocumentDto documentDto){ // 공공기관 데이터를 받아와서 비교
        if(Objects.isNull(documentDto)) return Collections.emptyList();

        // 거리계산 알고리즘 이용하여, 고객과 약국 사이의 거리를 계산하고 sort -> Haversine Fomula
        return pharmacySearchService.searchPharmacyDtoList() // 약국 리스트 조회
                .stream().map(pharmacyDto ->
                        Direction.builder()
                                .inputAddress(documentDto.getAddressName())
                                .inputLatitude(documentDto.getLatitude())
                                .inputLongitude(documentDto.getLongitude())
                                .targetPharmacyName(pharmacyDto.getPharmacyName())
                                .targetAddress(pharmacyDto.getPharmacyAddress())
                                .targetLatitude(pharmacyDto.getLatitude())
                                .targetLongitude(pharmacyDto.getLongitude())
                                .distance(
                                        calculateDistance(documentDto.getLatitude(), documentDto.getLongitude(),
                                                pharmacyDto.getLatitude(), pharmacyDto.getLongitude())
                                )
                                .build())
                .filter(direction -> direction.getDistance() <= RADIUS_KM) // 반경 10KM 거리순 필터
                .sorted(Comparator.comparing(Direction::getDistance)) // 오름 차순 정렬
                .limit(MAX_SEARCH_COUNT) // 최대 3개까지만 필요
                .collect(Collectors.toList());
    }


    // pharmacy search by category kakao api
    public List<Direction> buildDirectionListByCategoryApi(DocumentDto inputDocumentDto) { // 카카오 api를 통해 비교
        if(Objects.isNull(inputDocumentDto)) return Collections.emptyList();

        return kakaoCategorySearchService
                .requestPharmacyCategorySearch(inputDocumentDto.getLatitude(), inputDocumentDto.getLongitude(), RADIUS_KM)
                // kakaoapiresonsedto로 반환된 것을 -> 약국 검색 요청
                .getDocumentDtoList() // 여러 개의 약국 정보가 리스트로 담겨있다.
                .stream().map(resultDocumentDto -> //
                        Direction.builder()
                                .inputAddress(inputDocumentDto.getAddressName()) // 사용자의 현재값은 어디서 받냐고??
                                .inputLatitude(inputDocumentDto.getLatitude())
                                .inputLongitude(inputDocumentDto.getLongitude())
                                .targetPharmacyName(resultDocumentDto.getPlaceName()) // result라는 곳에
                                .targetAddress(resultDocumentDto.getAddressName())
                                .targetLatitude(resultDocumentDto.getLatitude())
                                .targetLongitude(resultDocumentDto.getLongitude())
                                .distance(resultDocumentDto.getDistance() * 0.001) // km 단위
                                .build())
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }



    // Haversine formula
    // 고객의 주소 정보, 약국의 주소 정보 두 위도 경도 사이의 거리 구하기
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }
}
