package com.clinic.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "追踪", description = "记录前端错误")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TraceController {

    @PostMapping("/error")
    public static void error(String any) {
        log.error(any);
    }
}
