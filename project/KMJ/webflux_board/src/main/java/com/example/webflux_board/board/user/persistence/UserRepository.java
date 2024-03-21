package com.example.webflux_board.board.user.persistence;


import com.example.webflux_board.board.user.persistence.vo.UserVO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<UserVO,Long> {
}
