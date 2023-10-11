package com.example.project.pharmacy.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.dto.KakaoApiResponseDto;
import com.example.project.api.service.KakaoAddressSearchService;
import com.example.project.direction.dto.OutputDto;
import com.example.project.direction.entity.Direction;
import com.example.project.direction.service.Base62Service;
import com.example.project.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.result.Output;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    private static final String ROAD_VIEW_VASE_URL = "https://map.kakao.com/link/roadview/";
//    private static final String DIRECTION_VASE_URL = "https://map.kakao.com/link/map/";

    private final Base62Service base62Service;

    @Value("${pharmacy.recommendation.base.url}")
    private String baseUrl;

    public List<OutputDto> recommendPharmacyList(String address) {

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentDtoList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address : {}", address);
            return Collections.emptyList();
        }

        // 사용자가 입력한 값을 바탕으로 현재 주소 계산 -> 첫번째 가장 관련성 높은 것을 추출 = 주소
        DocumentDto documentDto = kakaoApiResponseDto.getDocumentDtoList().get(0);

//        List<Direction> directionList = directionService.buildDirectionList(documentDto);

        //api -> 카테고리 , 거리 리스트를 뽑는다 => 추천 경로
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        return directionService.saveAll(directionList)
                .stream()
                .map(t -> convertToOutputDto(t))
                .collect(Collectors.toList());
    }

    // 결과 화면으로 보내기 위해서 작성
    private OutputDto convertToOutputDto(Direction direction) {

//        String params = String.join(",", direction.getTargetPharmacyName(),
//                String.valueOf(direction.getTargetLatitude()), String.valueOf(direction.getTargetLongitude()));// 이름 + 위도 + 경도-> 요청양식에 맞게
//        // 은혜약국,38.11,128.11 -> 파라미터
//
//        String result = UriComponentsBuilder.fromHttpUrl(DIRECTION_VASE_URL + params)
//                .toUriString();// 한글에 대해서 자동으로 인코딩, restTemplate->은 스트링x ,uri 로 반환 스트링은 또 인코딩 수행
//
//        log.info("directionparams: {}, url: {}", params, result);

        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl(baseUrl +base62Service.encodeDirectionId(direction.getId())) // -> short url
                .roadViewUrl(ROAD_VIEW_VASE_URL + direction.getTargetLatitude() + "," + direction.getTargetLongitude()) // roadview 추가
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
