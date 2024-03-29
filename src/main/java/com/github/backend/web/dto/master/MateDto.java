package com.github.backend.web.dto.master;

import com.github.backend.web.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class MateDto {

    @Schema(description = "메이트 cid", example = "1")
    private Long cid;
    @Schema(description = "메이트 아이디",example = "linxizhu129")
    private String mateId;
    @Schema(description = "메이트 성별",example = "MEN/WOMEN")
    private Gender mateGender;
    @Schema(description = "메이트 본명",example = "김용용")
    private String mateName;
    @Schema(description = "블랙리스트 여부",example = "true")
    private boolean isBlacklisted;
    @Schema(description = "메이트 프로필 사진 이름",example = "피카츄.png")
    private String imageName;
    @Schema(description = "메이트 프로필 사진 주소",example = "http://ddd.com")
    private String imageAddress;

}
