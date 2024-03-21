package com.example.webflux_board.board.post.persistence;

import com.example.webflux_board.board.post.persistence.vo.PostVO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PostRepository extends ReactiveCrudRepository<PostVO,Long> {
}
