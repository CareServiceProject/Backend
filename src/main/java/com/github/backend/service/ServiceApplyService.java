package com.github.backend.service;

import com.github.backend.repository.AuthRepository;
import com.github.backend.repository.MateRepository;
import com.github.backend.repository.RatingRepository;
import com.github.backend.repository.ServiceApplyRepository;
import com.github.backend.service.exception.CommonException;
import com.github.backend.web.dto.CommonResponseDto;
import com.github.backend.web.dto.apply.ServiceApplyDto;
import com.github.backend.web.dto.apply.UserDto;
import com.github.backend.web.dto.apply.UserMyPageDto;
import com.github.backend.web.dto.apply.UserProceedingDto;
import com.github.backend.web.dto.chatDto.CreatedChatRoomDto;
import com.github.backend.web.entity.*;
import com.github.backend.web.entity.custom.CustomUserDetails;
import com.github.backend.web.entity.enums.CareStatus;
import com.github.backend.web.entity.enums.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.backend.web.dto.apply.UserDto.careEntityToUserDto;
import static com.github.backend.web.dto.apply.UserProceedingDto.careEntityToUserDto2;

@Service
@Slf4j
@Builder
@RequiredArgsConstructor
public class ServiceApplyService {
    private final ServiceApplyRepository serviceApplyRepository;
    private final AuthRepository authRepository;
    private final MateRepository mateRepository;
    private final RatingRepository ratingRepository;
    private final ChatRoomService chatRoomService;

    @Transactional
    public CreatedChatRoomDto applyService(ServiceApplyDto serviceApplyDto, CustomUserDetails customUserDetails) {
        UserEntity userEntity = findById(customUserDetails);

        CareEntity careEntity = CareEntity.builder()
                .user(userEntity)
                .departureLoc(serviceApplyDto.getMeetingLoc())
                .arrivalLoc(serviceApplyDto.getDestination())
                .careDate(LocalDate.parse(serviceApplyDto.getMeetingDate()))
                .careDateTime(LocalTime.parse(serviceApplyDto.getStartTime()))
                .requiredTime(LocalTime.parse(serviceApplyDto.getEndTime()))
                .gender(serviceApplyDto.getGender())
                .cost(serviceApplyDto.getCost())
                .content(serviceApplyDto.getContent())
                .careStatus(CareStatus.WAITING)
                .build();

        CareEntity care = serviceApplyRepository.save(careEntity);
        Long chatRoomCid = chatRoomService.createRoom(care);

        return CreatedChatRoomDto.builder().chatRoomCid(chatRoomCid).build();
    }


    public List<UserDto> findByServiceStatus(CustomUserDetails customUserDetails, String status) {
        UserEntity userEntity = findById(customUserDetails);
        CareStatus careStatus;

        if (status.equalsIgnoreCase("waiting")) {
            careStatus = CareStatus.WAITING;
            List<CareEntity> careList = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, careStatus);
            return careEntityToUserDto(careList);
        } else if (status.equalsIgnoreCase("cancel")) {
            careStatus = CareStatus.CANCEL;
            List<CareEntity> careList = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, careStatus);
            return careEntityToUserDto(careList);
        }
        return new ArrayList<>();
    }


    @Transactional
    public List<UserProceedingDto> findByServiceStatus2(CustomUserDetails customUserDetails, String status) {
        UserEntity userEntity = findById(customUserDetails);
        CareStatus careStatus;

        if (status.equalsIgnoreCase("proceeding")) {
            careStatus = CareStatus.IN_PROGRESS;
            List<CareEntity> careList = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, careStatus);
            return careEntityToUserDto2(careList);
        } else if (status.equalsIgnoreCase("completed")) {
            careStatus = CareStatus.HELP_DONE;
            List<CareEntity> careList = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, careStatus);
            return careEntityToUserDto2(careList);
        }
        return new ArrayList<>();
    }

    public CommonResponseDto cancelByService(Long careCid) {
        CareEntity careEntity = serviceApplyRepository.findById(careCid).orElseThrow(() -> new CommonException("해당 서비스를 찾을 수 없습니다.", ErrorCode.FAIL_RESPONSE));
        careEntity.setCareStatus(CareStatus.CANCEL);

        serviceApplyRepository.save(careEntity);
        return CommonResponseDto.builder().code(200).success(true).message("요청하신 서비스가 성공적으로 취소되었습니다.").build();
    }

    @Transactional
    public UserMyPageDto findByMyPage(CustomUserDetails customUserDetails) {
        String userId = customUserDetails.getUser().getUserId();
        UserEntity userEntity = findById(customUserDetails);

        List<CareEntity> waiting = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, CareStatus.WAITING);
        List<CareEntity> proceeding = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, CareStatus.IN_PROGRESS);
        List<CareEntity> completed = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, CareStatus.HELP_DONE);
        List<CareEntity> cancelled = serviceApplyRepository.findAllByUserAndCareStatus(userEntity, CareStatus.CANCEL);

        ProfileImageEntity profileImage = userEntity.getProfileImage();

        long waitingCount = waiting.size();
        long proceedingCount = proceeding.size();
        long completedCount = completed.size();
        long cancelledCount = cancelled.size();

        if (profileImage == null) {
            return UserMyPageDto.builder()
                    .userId(userId)
                    .imageAddress(null)
                    .imageName(null)
                    .waitingCount(waitingCount)
                    .proceedingCount(proceedingCount)
                    .completedCount(completedCount)
                    .cancelledCount(cancelledCount)
                    .build();
        } else {
            return UserMyPageDto.builder()
                    .userId(userId)
                    .imageAddress(profileImage.getFileUrl())
                    .imageName(profileImage.getFileExt())
                    .waitingCount(waitingCount)
                    .proceedingCount(proceedingCount)
                    .completedCount(completedCount)
                    .cancelledCount(cancelledCount)
                    .build();
        }
    }

    public CommonResponseDto updateByMateStarCount(Long careCid, Double starCount) {
        CareEntity careEntity = serviceApplyRepository.findById(careCid)
                .orElseThrow(() -> new CommonException("해당 서비스를 찾을 수 업습니다.", ErrorCode.FAIL_RESPONSE));

        MateEntity mate = mateRepository.findById(careEntity.getMate().getMateCid())
                .orElseThrow(() -> new CommonException("해당 메이트를 찾을 수 없습니다.", ErrorCode.FAIL_RESPONSE));

        Optional<MateRatingEntity> mateRating = ratingRepository.findByMate(mate);

        MateRatingEntity rating;
        if (mateRating.isPresent()) {
            rating = mateRating.get();
            rating.setTotalRating(rating.getTotalRating() + starCount);
            rating.setRatingCount(rating.getRatingCount() + 1);
        } else {rating = MateRatingEntity.createNewMateRating(starCount, 1, mate);}

        ratingRepository.save(rating);

        return CommonResponseDto.builder().code(200).success(true).message("평가가 완료되었습니다.").build();
    }

    public UserEntity findById(CustomUserDetails customUserDetails){
        return authRepository.findById(customUserDetails.getUser().getUserCid()).orElseThrow(() -> new CommonException("유저를 찾을 수 없습니다.", ErrorCode.FAIL_RESPONSE));
    }



}
