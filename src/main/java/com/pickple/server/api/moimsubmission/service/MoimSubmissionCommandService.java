package com.pickple.server.api.moimsubmission.service;

import com.pickple.server.api.moim.domain.Moim;
import com.pickple.server.api.moim.repository.MoimRepository;
import com.pickple.server.api.moimsubmission.domain.MoimSubmission;
import com.pickple.server.api.moimsubmission.domain.MoimSubmissionState;
import com.pickple.server.api.moimsubmission.dto.request.MoimSubmitRequest;
import com.pickple.server.api.moimsubmission.repository.MoimSubmissionRepository;
import com.pickple.server.global.exception.CustomException;
import com.pickple.server.global.response.enums.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimSubmissionCommandService {

    private final MoimRepository moimRepository;
    private final MoimSubmissionRepository moimSubmissionRepository;

    public void createMoimSubmission(Long moimId, Long guestId, MoimSubmitRequest request) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        MoimSubmission moimSubmission = MoimSubmission.builder()
                .guestId(guestId)
                .moim(moim)
                .answerList(request.answerList())
                .accountList(request.accountList())
                .moimSubmissionState(MoimSubmissionState.PENDING_PAYMENT.getMoimSubmissionState())
                .build();
        isDuplicatedMoimSubmission(moimId, guestId);
        moimSubmissionRepository.save(moimSubmission);
    }

    private void isDuplicatedMoimSubmission(Long moimId, Long guestId) {
        if (moimSubmissionRepository.existsByMoimIdAndGuestId(moimId,
                guestId)) {
            throw new CustomException(ErrorCode.DUPLICATION_MOIM_SUBMISSION);
        }
    }

    public void updateSubmissionState(Long moimId, List<Long> submitterIdList) {
        List<MoimSubmission> moimSubmissionList = moimSubmissionRepository.findMoimSubmissionsByMoimId(moimId);

        for (MoimSubmission moimSubmission : moimSubmissionList) {
            if (submitterIdList.contains(moimSubmission.getGuestId())) {
                //신청자 상태가 승인 대기(pendingApproval)일 때 승인
                if (moimSubmission.getMoimSubmissionState()
                        .equals(MoimSubmissionState.PENDING_APPROVAL.getMoimSubmissionState())) {
                    moimSubmission.updateMoimSubmissionState(MoimSubmissionState.APPROVED.getMoimSubmissionState());
                }
            }

            //승인 이후에 승인 대기(pendingApproval)인 요청에 대해 승인 거절(rejected)상태로 변경
            if (moimSubmission.getMoimSubmissionState()
                    .equals(MoimSubmissionState.PENDING_APPROVAL.getMoimSubmissionState())) {
                moimSubmission.updateMoimSubmissionState(MoimSubmissionState.REJECTED.getMoimSubmissionState());
            }
            moimSubmissionRepository.save(moimSubmission);
        }
    }

    public void updateMoimSubmissionStateToPendingApproval(Long moimSubmissionId) {
        MoimSubmission moimSubmission = moimSubmissionRepository.findMoimSubmissionByIdOrThrow(moimSubmissionId);

        if (moimSubmission.getMoimSubmissionState()
                .equals(MoimSubmissionState.PENDING_PAYMENT.getMoimSubmissionState())) {
            moimSubmission.updateMoimSubmissionState(MoimSubmissionState.PENDING_APPROVAL.getMoimSubmissionState());
        } else {
            throw new CustomException(ErrorCode.MOIM_SUBMISSION_STATE_TRANSITION_NOT_ALLOWED);
        }
    }
}