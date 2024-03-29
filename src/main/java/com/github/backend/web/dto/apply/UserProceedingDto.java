package com.github.backend.web.dto.apply;

import com.github.backend.repository.ChatRoomRepository;
import com.github.backend.web.entity.CareEntity;
import com.github.backend.web.entity.ProfileImageEntity;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class UserProceedingDto {
    private String mateName;
    private String content;
    private String Date;
    private String Location;
    private Long careCid;
    private Long roomCid;

    private String myId;

    private String imageName;
    private String imageAddress;



    public static List<UserProceedingDto> careEntityToUserDto2(List<CareEntity> careList, ChatRoomRepository chatRoomRepository){
        List<UserProceedingDto> proceedingService = new ArrayList<>();
        for (CareEntity careEntity : careList) {
            ProfileImageEntity profileImage = careEntity.getMate().getProfileImage();
            Long roomCid = chatRoomRepository.findByCareCid(careEntity.getCareCid()).getChatRoomCid();

            if(profileImage == null){
                UserProceedingDto userProceedingDto = UserProceedingDto.builder()
                        .careCid(careEntity.getCareCid())
                        .mateName(careEntity.getMate().getMateId())
                        .Location(careEntity.getDepartureLoc())
                        .content(careEntity.getContent())
                        .imageName(null)
                        .imageAddress(null)
                        .Date(convertDateToString(careEntity.getCareDate(), careEntity.getCareDateTime(), careEntity.getRequiredTime()))
                        .myId(careEntity.getUser().getUserId())
                        .roomCid(roomCid).build();
                proceedingService.add(userProceedingDto);
            } else {
                UserProceedingDto userProceedingDto = UserProceedingDto.builder()
                        .careCid(careEntity.getCareCid())
                        .mateName(careEntity.getMate().getMateId())
                        .Location(careEntity.getDepartureLoc())
                        .content(careEntity.getContent())
                        .imageAddress(profileImage.getFileUrl())
                        .imageName(profileImage.getFileExt())
                        .Date(convertDateToString(careEntity.getCareDate(), careEntity.getCareDateTime(), careEntity.getRequiredTime()))
                        .myId(careEntity.getUser().getUserId())
                        .roomCid(roomCid).build();
                proceedingService.add(userProceedingDto);
            }
        }

        proceedingService.sort(Comparator.comparing(UserProceedingDto::getCareCid).reversed());

        return proceedingService;
    }


    private static String convertDateToString(LocalDate date, LocalTime careDateTime, LocalTime requiredTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        String dateString = date.format(formatter);
        String timeString = careDateTime.format(formatter2);
        String requiredTimeString = requiredTime.format(formatter2);

        return dateString + " / " + timeString + " ~ " + requiredTimeString;
    }
}


