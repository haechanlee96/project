package com.example.project.pharmacy.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.dto.KakaoApiResponseDto;
import com.example.project.api.service.KakaoAddressSearchService;
import com.example.project.direction.entity.Direction;
import com.example.project.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    public void recommendPharmacyList(String address) {

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentDtoList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address : {}", address);
            return;
        }

        // 사용자가 입력한 값을 바탕으로 현재 주소 계산 -> 첫번째 가장 관련성 높은 것을 추출 = 주소
        DocumentDto documentDto = kakaoApiResponseDto.getDocumentDtoList().get(0);

//        List<Direction> directionList = directionService.buildDirectionList(documentDto);

        //api -> 카테고리 , 거리 리스트를 뽑는다 => 추천 경로
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        directionService.saveAll(directionList);
    }
}
