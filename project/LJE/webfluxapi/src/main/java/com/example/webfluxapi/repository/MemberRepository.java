package com.example.webfluxapi.repository;

import com.example.webfluxapi.entity.Member;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MemberRepository extends ReactiveCrudRepository<Member, Integer> {

    @Query("SELECT * FROM MEMBER ORDER BY ID DESC LIMIT :page, :size")
    Flux<Member> findAllBy(Integer page, Integer size);
}
