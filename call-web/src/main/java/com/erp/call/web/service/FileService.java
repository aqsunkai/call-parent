package com.erp.call.web.service;

import com.erp.call.web.dto.PageRes;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final String EXPORT_PATH = "D:/call/files/";

    private ExecutorService executors = Executors.newCachedThreadPool();

    private volatile Map<String, Boolean> runningMap = new ConcurrentHashMap<>();

    private Map<String, String> RUNNING_RESULT = new ConcurrentHashMap<>();

    public String splitFile(MultipartFile file, Integer headerRowNum, Integer singleFileNum, String sheetName, String splitSheetName, String fileDate) {
        String fileName = file.getOriginalFilename();
        if (Boolean.TRUE.equals(runningMap.get(fileName))) {
            throw new RuntimeException("fileName文件正在解析，请稍等！");
        }
        runningMap.put(fileName, true);
        RUNNING_RESULT.remove(fileName);
        executors.execute(() -> {
            try {
                asyncSplitFile(file, headerRowNum, singleFileNum, sheetName, splitSheetName, fileDate);
            } catch (IOException e) {
                logger.error("异步拆分excel文件关闭流失败", e);
            }
        });
        return fileName;
    }

    private void asyncSplitFile(MultipartFile file, Integer headerRowNum, Integer singleFileNum, String sheetName, String splitSheetName, String fileDate) throws IOException {
        Workbook workbook = null;
        Workbook[] workbooks = null;
        String fileName = file.getOriginalFilename();
        try {
            boolean xlsx = fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm");
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            workbook = getWorkbook(file, xlsx);
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            Sheet oldSheet = workbook.getSheet(sheetName);
            if (null == oldSheet) {
                throw new RuntimeException(sheetName + "名称不存在，请检查！");
            }
            int lastRow = oldSheet.getLastRowNum();
            int fileNum;
            if ((lastRow + 1 - headerRowNum) % singleFileNum == 0) {
                fileNum = (lastRow + 1 - headerRowNum) / singleFileNum;
            } else {
                fileNum = (lastRow + 1 - headerRowNum) / singleFileNum + 1;
            }
            Sheet[] sheets = new Sheet[fileNum];
            workbooks = new Workbook[fileNum];
            for (int i = 1; i < fileNum + 1; i++) {
                Workbook newExcelCreat = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
                Sheet newSheet = newExcelCreat.createSheet(splitSheetName);
                copySheet(oldSheet, newSheet);
                sheets[i - 1] = newSheet;
                workbooks[i - 1] = newExcelCreat;
            }
            Iterator<Row> rowIt = oldSheet.rowIterator();

            for (int i = 0; i < headerRowNum; i++) {
                Row oldRow = rowIt.next();
                for (int y = 0; y < sheets.length; y++) {
                    Row newRow = sheets[y].createRow(i);
                    copyRow(workbooks[y], oldRow, newRow);
                }
            }
            int i = 0;
            int rowNum = -1;
            while (rowIt.hasNext()) {
                rowNum++;
                Row oldRow = rowIt.next();
                Row newRow = sheets[i].createRow(rowNum + headerRowNum);
                copyRow(workbooks[i], oldRow, newRow);
                if (rowNum >= singleFileNum - 1) {
                    i++;
                    rowNum = -1;
                }
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = format.parse(fileDate);
            for (int ii = 0; ii < workbooks.length; ii++) {
                String nextDate = format.format(DateUtils.addDays(currentDate, ii));
                String nextPath = EXPORT_PATH + nextDate;
                String singleFileName = nextPath + "/" + fileName + "(" + nextDate + ")" + suffix;
                File file1 = new File(singleFileName);
                if (file1.exists()) {
                    throw new RuntimeException(singleFileName + "文件已存在，请手动删除！");
                }
                File file2 = new File(nextPath);
                if (!file2.exists()) {
                    if (!file2.mkdirs()) {
                        throw new RuntimeException(nextPath + "目录创建失败！");
                    }
                }
            }

            for (int ii = 0; ii < workbooks.length; ii++) {
                String nextDate = format.format(DateUtils.addDays(currentDate, ii));
                String singleFileName = EXPORT_PATH + nextDate + "/" + fileName + "(" + nextDate + ")" + suffix;
                FileOutputStream fileOut = new FileOutputStream(singleFileName);
                workbooks[ii].write(fileOut);
                fileOut.flush();
                fileOut.close();
            }
        } catch (Exception e) {
            logger.error("异步拆分excel文件失败", e);
            RUNNING_RESULT.put(file.getOriginalFilename(), e.getMessage());
        } finally {
            runningMap.put(file.getOriginalFilename(), false);
            if (null != workbook) {
                workbook.close();
            }
            if (null != workbooks && workbooks.length > 0) {
                for (Workbook book : workbooks) {
                    book.close();
                }
            }
        }
    }

    /**
     * 迭代删除文件夹
     *
     * @param dirPath 文件夹路径
     */
    public void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    private Workbook getWorkbook(MultipartFile file, boolean xlsx) throws IOException {
        InputStream in = file.getInputStream();
        if (xlsx) {
            return new XSSFWorkbook(in);
        } else {
            return new HSSFWorkbook(in);
        }
    }

    /**
     * Sheet复制
     *
     * @param fromSheet
     * @param toSheet
     */
    public static void copySheet(Sheet fromSheet, Sheet toSheet) {
        mergeSheetAllRegion(fromSheet, toSheet);
        // 设置列宽
        int length = fromSheet.getRow(fromSheet.getFirstRowNum()).getLastCellNum();
        for (int i = 0; i <= length; i++) {
            toSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
        }
    }

    /**
     * 合并单元格
     *
     * @param fromSheet
     * @param toSheet
     */
    public static void mergeSheetAllRegion(Sheet fromSheet, Sheet toSheet) {
        int num = fromSheet.getNumMergedRegions();
        CellRangeAddress cellR;
        for (int i = 0; i < num; i++) {
            cellR = fromSheet.getMergedRegion(i);
            toSheet.addMergedRegion(cellR);
        }
    }

    /**
     * 复制单元格
     *
     * @param wb
     * @param fromCell
     * @param toCell
     */
    public static void copyCell(Workbook wb, Cell fromCell, Cell toCell) {
        CellStyle newstyle = wb.createCellStyle();
        newstyle.cloneStyleFrom(fromCell.getCellStyle());
        // 样式
        toCell.setCellStyle(newstyle);
        if (fromCell.getCellComment() != null) {
            toCell.setCellComment(fromCell.getCellComment());
        }
        // 不同数据类型处理
        int fromCellType = fromCell.getCellType();
        if (fromCellType == Cell.CELL_TYPE_FORMULA) {
            toCell.setCellType(Cell.CELL_TYPE_STRING);
        } else {
            toCell.setCellType(fromCellType);
        }
        if (fromCellType == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(fromCell)) {
                toCell.setCellValue(fromCell.getDateCellValue());
            } else {
                toCell.setCellValue(fromCell.getNumericCellValue());
            }
        } else if (fromCellType == Cell.CELL_TYPE_STRING) {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        } else if (fromCellType == Cell.CELL_TYPE_BLANK) {
            // nothing21
        } else if (fromCellType == Cell.CELL_TYPE_BOOLEAN) {
            toCell.setCellValue(fromCell.getBooleanCellValue());
        } else if (fromCellType == Cell.CELL_TYPE_ERROR) {
            toCell.setCellErrorValue(fromCell.getErrorCellValue());
        } else if (fromCellType == Cell.CELL_TYPE_FORMULA) {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        } else { // nothing29
        }

    }

    /**
     * 行复制功能
     *
     * @param wb
     * @param oldRow
     * @param toRow
     */
    public static void copyRow(Workbook wb, Row oldRow, Row toRow) {
        toRow.setHeight(oldRow.getHeight());
        for (Iterator<Cell> cellIt = oldRow.cellIterator(); cellIt.hasNext(); ) {
            Cell tmpCell = cellIt.next();
            Cell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, tmpCell, newCell);
        }
    }

    public PageRes splitFileResult(String fileName) {
        PageRes res = new PageRes();
        res.setRunning(runningMap.get(fileName));
        res.setRunningResult(RUNNING_RESULT.get(fileName));
        return res;
    }

}
