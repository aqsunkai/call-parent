package com.erp.call.web.controller;

import com.erp.call.web.dto.PageRes;
import com.erp.call.web.service.FileService;
import com.erp.call.web.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/file/split")
    public Result<String> splitFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("headerRowNum") Integer headerRowNum,
                                    @RequestParam("fileNum") Integer fileNum,
                                    @RequestParam("sheetName") String sheetName,
                                    @RequestParam("splitSheetName") String splitSheetName,
                                    @RequestParam("fileDate") String fileDate) {
        return Result.success(fileService.splitFile(file, headerRowNum, fileNum, sheetName, splitSheetName, fileDate));
    }

    @GetMapping(value = "/file/splitResult")
    public Result<PageRes> splitFileResult(@RequestParam("fileName") String fileName) {
        return Result.success(fileService.splitFileResult(fileName));
    }

}
