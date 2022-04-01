package com.awss3.rednodes.controller;

import com.awss3.rednodes.service.AmazonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.channels.MulticastChannel;
import java.util.List;

@RestController
@RequestMapping("/storage/")
public class RestS3Controller {

    @Autowired
    private AmazonService amazonService;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file) {
        return this.amazonService.uploadFile(file);
    }

    @GetMapping("/getFileList")
    public List<String> getFileList(){
        return this.amazonService.listFiles();
    }
}
