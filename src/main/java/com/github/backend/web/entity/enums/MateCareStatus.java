package com.github.backend.web.entity.enums;

import com.github.backend.service.exception.CommonException;

public enum MateCareStatus {
    IN_PROGRESS("IN_PROGRESS","진행 중"),
    HELP_DONE("HELP_DONE","도움 완료"),
    CANCEL("CANCEL","취소");

    private final String mateCareStatus_english;
    private final String mateCareStatus_korean;

    MateCareStatus(String mateCareStatus_english, String mateCareStatus_korean) {
        this.mateCareStatus_english = mateCareStatus_english;
        this.mateCareStatus_korean = mateCareStatus_korean;
    }

    public static MateCareStatus valueOfTerm(String mateCareStatus) {
        for(MateCareStatus status: values()){
            if(mateCareStatus.equals(status.mateCareStatus_english)){
                return status;
            }
        }
        throw new CommonException("존재하지 않는 상태입니다. 다시 입력해주세요",ErrorCode.BAD_REQUEST_RESPONSE);
    }
}
