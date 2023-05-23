package com.xuananh.demoimport.controller;

import com.xuananh.demoimport.service.InterestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/interest-rate")
public class InterestRateController {

    @Autowired
    private InterestRateService interestRateService;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("exel-file") MultipartFile multipartFile) throws Exception {
        return ResponseEntity.ok(interestRateService.upload(multipartFile));
    }
}
