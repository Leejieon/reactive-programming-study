package com.example.webflux_board.board.post.domain;

import com.example.webflux_board.board.post.persistence.vo.PostVO;
import com.example.webflux_board.board.user.persistence.vo.UserVO;
import lombok.Data;

@Data
public class Post {
    private PostVO postInfo;
    private UserVO userInfo;

    public Post(PostVO postInfo, UserVO userInfo) {
        this.postInfo = postInfo;
        this.userInfo = userInfo;
    }
}