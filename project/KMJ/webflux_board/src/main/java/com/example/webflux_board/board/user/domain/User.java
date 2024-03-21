package com.example.webflux_board.board.user.domain;

import com.example.webflux_board.board.user.persistence.vo.UserVO;
import lombok.Data;

@Data
public class User {

    private UserVO userVO;

    public User(UserVO userVO) {
        this.userVO = userVO;
    }
}
