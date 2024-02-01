package com.github.backend.web.dto;

import com.github.backend.web.entity.enums.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisteredUser {

private String username;
private int userAge;
private Gender userGender;

}