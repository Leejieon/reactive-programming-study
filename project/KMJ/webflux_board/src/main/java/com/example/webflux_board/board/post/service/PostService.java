package com.example.webflux_board.board.post.service;

import com.example.webflux_board.board.post.domain.Post;
import com.example.webflux_board.board.post.persistence.PostRepository;
import com.example.webflux_board.board.post.persistence.vo.PostVO;
import com.example.webflux_board.board.user.persistence.UserRepository;
import com.example.webflux_board.board.user.persistence.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Flux<Post> findAll() {
        Flux<PostVO> all = postRepository.findAll();

        return all.flatMap(postVO -> {
            Mono<UserVO> author = userRepository.findById(postVO.getAuthorId());
            return author.flatMap((userVO) -> Mono.just(new Post(postVO,userVO)));
        });
    }

    public Mono<Post> findOneById(Long id) {

        Mono<PostVO> byId = postRepository.findById(id);

        return byId.flatMap(postVO -> {
            Mono<UserVO> author = userRepository.findById(postVO.getAuthorId());
            return author.flatMap((userVO) -> Mono.just(new Post(postVO,userVO)));
        });
    }

    public Mono<Post> save(String title, String content, Long authorId) {
        PostVO postVO = new PostVO(title, content, authorId);

        return postRepository.save(postVO)
                .flatMap(post -> {
                    Mono<UserVO> byId = userRepository.findById(post.getAuthorId());
                    return byId.flatMap(userVO -> Mono.just(new Post(postVO,userVO)));
                });
    }
}
