package com.pickple.server.api.moim.service;

import com.pickple.server.api.moim.domain.Moim;
import com.pickple.server.api.moim.domain.QuestionInfo;
import com.pickple.server.api.moim.domain.enums.Category;
import com.pickple.server.api.moim.dto.response.MoimByCategoryResponse;
import com.pickple.server.api.moim.dto.response.MoimDescriptionResponse;
import com.pickple.server.api.moim.dto.response.MoimDetailResponse;
import com.pickple.server.api.moim.dto.response.MoimGetResponse;
import com.pickple.server.api.moim.dto.response.MoimListByHostAndMoimStateGetResponse;
import com.pickple.server.api.moim.dto.response.MoimListByHostGetResponse;
import com.pickple.server.api.moim.repository.MoimRepository;
import com.pickple.server.api.moimsubmission.dto.response.MoimByGuestResponse;
import com.pickple.server.api.moimsubmission.repository.MoimSubmissionRepository;
import com.pickple.server.global.util.DateUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimQueryService {

    private final MoimRepository moimRepository;
    private final Random random = new Random();
    private final MoimSubmissionRepository moimSubmissionRepository;

    public MoimDetailResponse getMoimDetail(Long moimId, Long guestId) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        return MoimDetailResponse.builder()
                .dayOfDay(DateUtil.calculateDayOfDay(moim.getDateList().getDate()))
                .title(moim.getTitle())
                .dateList(moim.getDateList())
                .isOffline(moim.isOffline())
                .spot(moim.getSpot())
                .maxGuest(moim.getMaxGuest())
                .fee(moim.getFee())
                .imageList(moim.getImageList())
                .hostId(moim.getHost().getId())
                .isSubmitted(moimSubmissionRepository.existsByMoimIdAndGuestId(moimId, guestId))
                .build();
    }

    public MoimByGuestResponse getSubmittedMoimDetail(final Long moimId) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        return MoimByGuestResponse.builder()
                .title(moim.getTitle())
                .hostNickname(moim.getHost().getNickname())
                .isOffline(moim.isOffline())
                .spot(moim.getSpot())
                .dateList(moim.getDateList())
                .fee(moim.getFee())
                .hostImageUrl(moim.getHost().getImageUrl())
                .moimImageUrl(moim.getImageList().getImageUrl1())
                .build();
    }

    public List<MoimByCategoryResponse> getMoimListByCategory(final String category) {
        List<Moim> moimList;

        if (category.equals("all")) {
            moimList = moimRepository.findAll();
        } else {
            moimList = moimRepository.findMoimsByCategory(category);
        }

        return moimList.stream()
                .map(oneMoim -> MoimByCategoryResponse.builder()
                        .moimId(oneMoim.getId())
                        .dayOfDay(DateUtil.calculateDayOfDay(oneMoim.getDateList().getDate()))
                        .title(oneMoim.getTitle())
                        .hostNickName(oneMoim.getHost().getNickname())
                        .dateList(oneMoim.getDateList())
                        .moimImageUrl(oneMoim.getImageList().getImageUrl1())
                        .hostImageUrl(oneMoim.getHost().getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public MoimDescriptionResponse getMoimDescription(final Long moimId) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        return MoimDescriptionResponse.builder()
                .description(moim.getDescription())
                .build();
    }

    public QuestionInfo getMoimQuestionList(final Long moimId) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        return moim.getQuestionList();
    }

    public Long getMoimBanner() {
        List<Long> moimIdList = moimRepository.findAll()
                .stream()
                .map(Moim::getId)
                .toList();

        int randomIndex = random.nextInt(moimIdList.size());
        return moimIdList.get(randomIndex);
    }

    public List<MoimListByHostAndMoimStateGetResponse> getMoimListByHostAndMoimState(Long hostId, String moimState) {
        List<Moim> moimList = moimRepository.findMoimsByhostIdAndMoimState(hostId, moimState);

        return moimList.stream()
                .map(oneMoim -> MoimListByHostAndMoimStateGetResponse.builder()
                        .moimId(oneMoim.getId())
                        .moimImage(oneMoim.getImageList().getImageUrl1())
                        .approvedGuest(calculateApprovedGuest(oneMoim.getId()))
                        .title(oneMoim.getTitle())
                        .maxGuest(oneMoim.getMaxGuest())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MoimListByHostGetResponse> getMoimListByHost(Long hostId) {
        List<Moim> moimList = moimRepository.findMoimsByHostId(hostId);

        return moimList.stream()
                .map(oneMoim -> MoimListByHostGetResponse.builder()
                        .moimId(oneMoim.getId())
                        .dayOfDay(DateUtil.calculateDayOfDay(oneMoim.getDateList().getDate()))
                        .title(oneMoim.getTitle())
                        .hostNickName(oneMoim.getHost().getNickname())
                        .dateList(oneMoim.getDateList())
                        .moimImageUrl(oneMoim.getImageList().getImageUrl1())
                        .hostImageUrl(oneMoim.getHost().getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    private Long calculateApprovedGuest(Long moimId) {
        try {
            return moimSubmissionRepository.countValidMoimSubmissions(moimId);
        } catch (InvalidDataAccessResourceUsageException e) {
            // 테이블이 존재하지 않아서 발생한 예외일 경우 0 반환
            return 0L;
        }
    }

    public List<String> getCategories() {
        return Arrays.stream(Category.values())
                .map(category -> category.category)
                .collect(Collectors.toList());
    }

    public MoimGetResponse getMoimForReview(Long moimId) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);

        return MoimGetResponse.builder()
                .title(moim.getTitle())
                .moimImageUrl(moim.getImageList().getImageUrl1())
                .hostImageUrl(moim.getHost().getImageUrl())
                .hostNickName(moim.getHost().getNickname())
                .dateList(moim.getDateList())
                .build();
    }

}
