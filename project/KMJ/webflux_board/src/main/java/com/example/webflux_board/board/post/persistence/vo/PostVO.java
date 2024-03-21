package com.example.webflux_board.board.post.persistence.vo;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Table("posts")
public class PostVO {

    @Id
    private Long id;

    private String title;

    private String content;

    private Long authorId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public PostVO(String title, String content, Long authorId) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}