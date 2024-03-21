package com.example.webflux_board.board.post.controller.dto;

import com.example.webflux_board.board.post.domain.Post;
import com.example.webflux_board.board.post.persistence.vo.PostVO;
import com.example.webflux_board.board.user.controller.dto.UserDto;
import com.example.webflux_board.board.user.controller.dto.UserDto.UserResponse;
import com.example.webflux_board.board.user.persistence.vo.UserVO;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class PostDto {

    @Data
    @Builder(access = AccessLevel.PRIVATE)
    public static class PostResponse {
        private String title;
        private String content;
        private UserDto.UserResponse author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime requestAt;

        public static PostResponse toDto(Post post) {
            PostVO postInfo = post.getPostInfo();
            UserVO userInfo = post.getUserInfo();
            return builder()
                    .title(postInfo.getTitle())
                    .content(postInfo.getContent())
                    .author(UserResponse.toDto(userInfo))
                    .createdAt(postInfo.getCreatedAt())
                    .updatedAt(postInfo.getUpdatedAt())
                    .requestAt(LocalDateTime.now())
                    .build();
        }
    }

    @Data
    public static class PostRequest {
        private String title;
        private String content;
        private Long authorId;
    }
}
