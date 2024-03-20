package com.example.webfluxapi.controller;

import com.example.webfluxapi.common.ApiResponse;
import com.example.webfluxapi.dto.MemberDTO;
import com.example.webfluxapi.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/member")
    public Mono<ApiResponse> create(@RequestBody MemberDTO.Crud memberDTO) {
        return memberService.create(memberDTO)
                .map(member -> ApiResponse.builder()
                        .code(200)
                        .message("CREATE SUCCESS!")
                        .data(member)
                        .build())
                .switchIfEmpty(Mono.just(ApiResponse.builder()
                        .code(500)
                        .message("CREATE ERROR")
                        .build()));
    }

    @GetMapping("/member/item/{id}")
    public Mono<ApiResponse> item(@PathVariable("id") Integer id) {
        return memberService.item(id)
                .map(member -> ApiResponse.builder()
                        .code(200)
                        .message("FIND ITEM SUCCESS!")
                        .data(member)
                        .build())
                .switchIfEmpty(Mono.just(ApiResponse.builder()
                        .code(500)
                        .message("FIND ITEM ERROR")
                        .build()));
    }

    @GetMapping("/member/list/{page}")
    public Mono<ApiResponse> list(
            @PathVariable("page") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return memberService.list(page < 1 ? 1 : page, limit)
                .collectList()
                .map(members -> ApiResponse.builder()
                        .page(page < 1 ? 1 : page)
                        .limit(limit)
                        .code(200)
                        .message("FIND MEMBER LIST SUCCESS!")
                        .data(members)
                        .build())
                .switchIfEmpty(Mono.just(ApiResponse.builder()
                        .code(500)
                        .message("FIND MEMBER LIST ERROR")
                        .build()));
    }
}
