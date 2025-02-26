package vn.com.lifesup.base.util;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.security.util.FieldUtils;
import org.springframework.util.CollectionUtils;
import vn.com.lifesup.base.exception.WriteExcelException;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Builder
@Slf4j
public class ExcelWriter<T> {

    private String templateFile;
    private List<String> dataFields;
    private List<Pair<String, String>> fillData;
    private Collection<T> dataList;
    private List<List<Pair<Integer, Object>>> footer;
    private boolean useRowNum = false;
    private int rowIdx;
    @Builder.Default
    private int rowNum = 1;

    public byte[] write() throws WriteExcelException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(OPCPackage.open(new ClassPathResource("templates/" + templateFile).getInputStream()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            doFillData(sheet);
            doWriteData(sheet);
            if (!CollectionUtils.isEmpty(footer)) {
                writeFooter(sheet);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WriteExcelException(e.getMessage(), e.getCause());
        }
    }

    private void doWriteData(XSSFSheet sheet) throws IllegalAccessException {
        rowIdx = sheet.getLastRowNum() + 1;
        for (T data : dataList) {
            XSSFRow row = sheet.createRow(rowIdx++);
            writeRow(row, data);
        }
    }

    private void writeFooter(XSSFSheet sheet) {
        for (List<Pair<Integer, Object>> fRow : footer) {
            XSSFRow row = sheet.createRow(++rowIdx);
            for (Pair<Integer, Object> c : fRow) {
                XSSFCell cell = row.createCell(c.getFirst());
                setCellValueFromObject(cell, c.getSecond());
            }
        }
    }

    private void writeRow(XSSFRow row, T data) throws IllegalAccessException {
        int startColumn = 0;
        if (useRowNum) {
            XSSFCell cell = row.createCell(0);
            setCellValueFromObject(cell, rowNum++);
            startColumn = 1;
        }
        for (int i = 0; i < dataFields.size(); i++) {
            XSSFCell cell = row.createCell(i + startColumn);
            Object value = FieldUtils.getFieldValue(data, dataFields.get(i));
            setCellValueFromObject(cell, value);
        }
    }

    public static void setCellValueFromObject(XSSFCell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String str) {
            cell.setCellValue(str);
        } else if (value instanceof Double || value instanceof Float) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Integer || value instanceof Long) {
            cell.setCellValue(((Number) value).longValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(value.toString());
        }
    }


    private void doFillData(XSSFSheet sheet) {
        int fillLastRowCheck = sheet.getLastRowNum();
        for (int i = 0; i <= fillLastRowCheck; i++) {
            XSSFRow row = sheet.getRow(i);
            doFillData(row);
        }
    }

    private void doFillData(XSSFRow row) {
        row.cellIterator().forEachRemaining(cell -> {
            if (Objects.isNull(cell)) {
                return;
            }
            if (CellType.STRING.equals(cell.getCellType())) {
                String cellValue = cell.getStringCellValue();
                for (Pair<String, String> fill : fillData) {
                    cellValue = cellValue.replace(fill.getFirst(), fill.getSecond());
                }
                cell.setCellValue(cellValue);
            }
        });
    }
}
