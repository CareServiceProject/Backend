package com.github.backend.web.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.backend.web.entity.enums.Gender;
import com.github.backend.web.entity.enums.MateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Setter
@Table(name = "mates_table")
public class MateEntity extends BaseEntity{
  @Id
  @Column(name = "mate_cid")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "메이트 고유 아이디")
  private Long mateCid;

  @NotNull
  @Column(name = "mate_id", length = 30)
  @Schema(description = "메이트 아이디", example = "")
  private String mateId;

  @NotNull
  @Column(name = "mate_password")
  @Schema(description = "메이트 비밀번호", example = "qwer1234")
  private String password;

  @Column(name = "mate_email", length = 30)
  @Schema(description = "메이트 이메일", example = "supercoding@admin.com")
  private String email;

  @Column(name = "mate_name", length = 30)
  @Schema(description = "메이트 이름", example = "메이트")
  private String name;

  @Column(name = "mate_phone", length = 30)
  @Schema(description = "메이트 휴대폰번호", example = "010-1111-2222")
  private String phoneNumber;

  @Column(name = "mate_address", length = 100)
  @Schema(description = "메이트 주소", example = "어디일까요~")
  private String address;

  @Column(name = "mate_gender")
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(name = "mate_registration_num")
  @Schema(description = "주민등록번호", example = "950000-1020200")
  private String registrationNum;

  @Column(name = "login_type", length = 100)
  @Schema(description = "로그인 형식", example = "GOOGLE")
  private String loginType;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_image_cid", referencedColumnName = "profile_image_cid")
  private ProfileImageEntity profileImage;


  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "roles_cid", referencedColumnName = "roles_cid")
  private RolesEntity roles;

  @Column(name = "is_deleted", length = 10)
  @Schema(description = "삭제여부", example = "Y")
  private String isDeleted;

  @Column(name = "is_blacklisted", columnDefinition = "TINYINT(1)")
  @ColumnDefault("false")
  @Schema(description = "블랙리스트등록여부", example = "true")
  private boolean blacklisted;


  @Column(name = "register_status", length = 10)
  @Schema(description = "등록현황", example = "인증 전")
  @Enumerated(EnumType.STRING)
  private MateStatus mateStatus;


  public MateEntity update(String email, String password, String phoneNumber) {
    this.email = email;
    this.password = password;
    this.phoneNumber = phoneNumber;
    return this;
  }
  @Builder
  public MateEntity(String mateId, String password, String email, String name, String phoneNumber, String address, Gender gender, String registrationNum,RolesEntity roles, String isDeleted,boolean isBlacklisted){
    this.mateId = mateId;
    this.password = password;
    this.email = email;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.gender = gender;
    this.roles = roles;
    this.registrationNum = registrationNum;
    this.isDeleted = isDeleted;
    this.mateStatus = MateStatus.PREPARING;
    this.blacklisted = isBlacklisted;
  }
}
