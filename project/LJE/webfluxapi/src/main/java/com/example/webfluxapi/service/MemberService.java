package com.example.webfluxapi.service;

import com.example.webfluxapi.dto.MemberDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MemberService {
    public Mono<MemberDTO.Crud> create(MemberDTO.Crud dto);
    public Mono<MemberDTO.Crud> item(Integer id);
    public Flux<MemberDTO.Crud> list(Integer page, Integer limit);
}
