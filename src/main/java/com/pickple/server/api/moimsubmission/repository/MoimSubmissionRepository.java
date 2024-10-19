package com.pickple.server.api.moimsubmission.repository;

import com.pickple.server.api.moimsubmission.domain.MoimSubmission;
import com.pickple.server.global.exception.CustomException;
import com.pickple.server.global.response.enums.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoimSubmissionRepository extends JpaRepository<MoimSubmission, Long> {

    Optional<MoimSubmission> findMoimSubmissionById(Long id);

    default MoimSubmission findMoimSubmissionByIdOrThrow(Long id) {
        return findMoimSubmissionById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MOIM_SUBMISSION_NOT_FOUND));
    }

    boolean existsByMoimIdAndGuestId(Long moimId, Long guestId);

    List<MoimSubmission> findMoimSubmissionsByGuestIdAndMoimSubmissionState(Long guestId, String moimSubmissionState);

    MoimSubmission findMoimSubmissionByMoimIdAndGuestId(Long moimId, Long guestId);

    List<MoimSubmission> findMoimSubmissionsByMoimId(Long moimId);

    @Query("SELECT ms FROM MoimSubmission ms WHERE ms.moim.id = :moimId AND ms.moimSubmissionState IN ('pendingApproval', 'approved', 'rejected','completed')")
    List<MoimSubmission> findMoimSubmissionsByMoimIdAndMoimSubmissionState(@Param("moimId") Long moimId);

    @Query("SELECT ms FROM MoimSubmission ms WHERE ms.guestId = :guestId AND ms.moimSubmissionState = 'completed'")
    List<MoimSubmission> findCompletedMoimSubmissionsByGuest(@Param("guestId") Long guestId);

    @Query("SELECT COUNT(ms) FROM MoimSubmission ms WHERE ms.moim.id = :moimId AND ms.moimSubmissionState IN('approved','completed')")
    long countValidMoimSubmissions(@Param("moimId") Long moimId);

    @Query("SELECT COUNT(ms) FROM MoimSubmission ms WHERE ms.moim.id IN :moimIds AND ms.moimSubmissionState = 'completed'")
    int countCompletedSubmissionsByMoimIds(@Param("moimIds") List<Long> moimIds);

    boolean existsByMoimIdAndGuestIdAndMoimSubmissionState(Long moimId, Long guestId, String MoimSubmissionState);

    @Query("SELECT ms FROM MoimSubmission ms WHERE ms.guestId = :guestId AND ms.moimSubmissionState IN ('pendingPayment','pendingApproval', 'approved', 'rejected','refunded')")
    List<MoimSubmission> findSubmittedMoimSubmissions(@Param("guestId") Long guestId);

    @Query("SELECT ms FROM MoimSubmission ms WHERE ms.moim.id = :moimId AND ms.moimSubmissionState = 'approved'")
    List<MoimSubmission> findApprovedMoimSubmissionsByMoimId(@Param("moimId") Long moimId);
    

}
