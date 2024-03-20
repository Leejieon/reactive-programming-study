package com.example.webfluxapi.mapper;

import com.example.webfluxapi.dto.MemberDTO;
import com.example.webfluxapi.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberMapper {
    public MemberDTO.Crud ToDTOCrud(Member entity) {
        return MemberDTO.Crud.builder()
                .id(entity.getId())
                .name(entity.getName())
                .age(entity.getAge())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public Member ToCrudEntity(MemberDTO.Crud dto) {
        return Member.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
