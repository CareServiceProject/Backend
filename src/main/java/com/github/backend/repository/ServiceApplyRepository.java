package com.github.backend.repository;

import com.github.backend.web.entity.CareEntity;
import com.github.backend.web.entity.MateEntity;
import com.github.backend.web.entity.UserEntity;
import com.github.backend.web.entity.custom.CustomUserDetails;
import com.github.backend.web.entity.enums.CareStatus;
import com.github.backend.web.entity.enums.MateCareStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ServiceApplyRepository extends JpaRepository<CareEntity, Long > {

    List<CareEntity> findAllByCareStatus(CareStatus careStatus);

    List<CareEntity> findAllByUserAndCareStatus(UserEntity userEntity, CareStatus careStatus);
    
    Integer countByCareStatus(CareStatus careStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CareEntity> findById(Long Id);
}
