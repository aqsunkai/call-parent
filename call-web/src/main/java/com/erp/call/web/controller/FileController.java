package com.erp.call.web.controller;

import com.erp.call.web.service.FileService;
import com.erp.call.web.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/file/split")
    @ResponseBody
    public Result<String> splitFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("headerRowNum") Integer headerRowNum,
                                    @RequestParam("fileNum") Integer fileNum,
                                    @RequestParam("sheetName") String sheetName,
                                    @RequestParam("splitSheetName") String splitSheetName,
                                    @RequestParam("fileDate") String fileDate) {
        return Result.success(fileService.splitFile(file, headerRowNum, fileNum, sheetName, splitSheetName, fileDate));
    }
}
