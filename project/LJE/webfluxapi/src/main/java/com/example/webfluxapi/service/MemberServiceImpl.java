package com.example.webfluxapi.service;

import com.example.webfluxapi.dto.MemberDTO;
import com.example.webfluxapi.mapper.MemberMapper;
import com.example.webfluxapi.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    public Mono<MemberDTO.Crud> create(MemberDTO.Crud dto) {
        return memberRepository.save(memberMapper.ToCrudEntity(dto)).flatMap(member -> {
            return memberRepository.findById(member.getId());
        }).map(memberMapper::ToDTOCrud);
    }

    @Override
    public Mono<MemberDTO.Crud> item(Integer id) {
        return memberRepository.findById(id).map(memberMapper::ToDTOCrud);
    }

    @Override
    public Flux<MemberDTO.Crud> list(Integer page, Integer limit) {
        return memberRepository.findAllBy((page - 1) * limit, limit).map(memberMapper::ToDTOCrud);
    }
}
