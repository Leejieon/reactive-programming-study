package com.example.webflux_board.board.user.controller.dto;

import com.example.webflux_board.board.user.domain.User;
import com.example.webflux_board.board.user.persistence.vo.UserVO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    @Builder(access = AccessLevel.PRIVATE)
    public static class UserResponse {
        private String name;
        private int age;
        private LocalDateTime createdAt;
        private LocalDateTime updateAt;

        private LocalDateTime requestAt;

        public static UserResponse toDto(User user) {
            UserVO userVO = user.getUserVO();
            return toDto(userVO);
        }

        public static UserResponse toDto(UserVO userVO) {
            return builder()
                    .name(userVO.getName())
                    .age(userVO.getAge())
                    .createdAt(userVO.getCreatedAt())
                    .updateAt(userVO.getUpdatedAt())
                    .requestAt(LocalDateTime.now())
                    .build();
        }
    }

    @Data
    public static class UserRequest {

        @NotEmpty
        private String name;

        @NotNull
        private Integer age;
    }
}
