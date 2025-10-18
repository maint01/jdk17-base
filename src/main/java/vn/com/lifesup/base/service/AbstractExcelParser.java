package vn.com.lifesup.base.service;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import vn.com.lifesup.base.dto.excel.ExcelColumn;
import vn.com.lifesup.base.dto.excel.ExcelParseResult;
import vn.com.lifesup.base.dto.excel.CellCallback;
import vn.com.lifesup.base.dto.excel.CellTransform;
import vn.com.lifesup.base.exception.BusinessException;
import vn.com.lifesup.base.util.Translator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

@Log4j2
public abstract class AbstractExcelParser<T> {

    public ExcelParseResult<T> parse(MultipartFile multipartFile, int startRow, long maxFileSize) throws Exception {
        String extension = Objects.requireNonNull(FilenameUtils.getExtension(multipartFile.getOriginalFilename())).toLowerCase();
        if (!allowExtensions().contains(extension)) {
            throw new BusinessException(Translator.getMessage("error.file_extension_not_allowed", allowExtensions().toString()));
        }
        InputStream inputStream = multipartFile.getInputStream();

        validateFileSize(inputStream, maxFileSize);

        long countColumn = Arrays.stream(getTargetClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(ExcelColumn.class) != null).count();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            int errorCount = 0;
            Sheet sheet = workbook.getSheetAt(0);
            Resource resourceTemplate = getResourceTemplate();
            validateHeader(sheet, resourceTemplate, startRow++);
            List<T> results = new ArrayList<>();
            Set<String> duplicateChecker = new HashSet<>();

            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }

                T obj = mapRowToClass(row);
                String dupKey = buildDuplicateKey(obj);
                if (StringUtils.isNotBlank(dupKey) && !duplicateChecker.add(dupKey)) {
                    addErrorMessage(row, Translator.getMessage("error.file.duplicate"));
                }

                if (rowHasError(row, (int) countColumn)) { // countColumn is not defined/visible, assuming it's available
                    errorCount++;
                } else {
                    results.add(obj);
                }
            }

            if (errorCount == 0) {
                return ExcelParseResult.<T>builder()
                        .data(results)
                        .hasError(false)
                        .errorCount(errorCount)
                        .build();
            } else {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    workbook.write(bos);
                    return ExcelParseResult.<T>builder()
                            .data(results)
                            .hasError(true)
                            .errorCount(errorCount)
                            .fileOut(new ByteArrayResource(bos.toByteArray()))
                            .build();
                }
            }
        }
    }

    private List<String> extractHeadersFromTemplate(Resource headerTemplate, int rowStart) throws Exception {
        try (InputStream is = headerTemplate.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(rowStart);
            if (headerRow == null) {
                throw new BusinessException(Translator.getMessage("error.header_row_is_missing_in_template_file."));
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }

            return headers;
        }
    }

    protected void validateFileSize(InputStream inputStream, long maxFileSize) throws Exception {
        if (inputStream.available() > maxFileSize) {
            throw new BusinessException(Translator.getMessage("error.file.too_large", getHumanReadableFileSize(maxFileSize)));
        }
    }

    protected void validateHeader(Sheet inputSheet, Resource resourceTemplate, int startRow) throws Exception {
        List<String> expectedHeaders = extractHeadersFromTemplate(resourceTemplate, startRow);
        Row headerRow = inputSheet.getRow(startRow);

        if (headerRow == null) {
            throw new BusinessException(Translator.getMessage("error.file_content_invalid"));
        }

        for (int i = 0; i < expectedHeaders.size(); i++) {
            String expected = expectedHeaders.get(i);
            String actual = getCellValue(headerRow.getCell(i));

            if (!expected.equalsIgnoreCase(actual != null ? actual.trim() : null)) {
                throw new BusinessException(Translator.getMessage("error.file_content_invalid"));
            }

            Cell cellResult = headerRow.createCell(expectedHeaders.size());
            cellResult.setCellValue("Kết quả");
            inputSheet.setColumnWidth(expectedHeaders.size(), 30 * 256);
        }

    }

    // Mapping
    @SuppressWarnings("java:S3011")
    private T mapRowToClass(Row row) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T instance = getTargetClass().getDeclaredConstructor().newInstance();
        StringBuilder errors = new StringBuilder();

        for (Field field : getTargetClass().getDeclaredFields()) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation == null) continue;

            Cell cell = row.getCell(annotation.index());
            String value = getCellValue(cell);

            // validate
            if (annotation.notNull() && StringUtils.isBlank(value)) {
                errors.append(Translator.getMessage("VALID_000", annotation.fieldName())).append("\n");
            }

            validateMaxLength(annotation, value, errors);
            validateMinLength(annotation, value, errors);
            validatePattern(annotation, value, errors);
            value = validateExtra(instance, field, value, errors);

            // set value
            if (value != null) {
                field.setAccessible(true);
                field.set(instance, value.trim());
            }
        }

        if (!errors.isEmpty()) {
            addErrorMessage(row, errors.toString());
        }

        return instance;
    }

    private void validatePattern(ExcelColumn annotation, String value, StringBuilder errors) {
        if (StringUtils.isBlank(annotation.pattern())) {
            return;
        }
        if (StringUtils.isBlank(value)) {
            return;
        }

        if (!Pattern.matches(annotation.pattern(), value)) {
            errors.append(Translator.getMessage("VALID_001", annotation.fieldName()))
                    .append("\n");
        }
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return StringUtils.EMPTY;

        DataFormatter dataFormatter = new DataFormatter();
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                Workbook workbook = cell.getSheet().getWorkbook();
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                return String.valueOf(evaluator.evaluate(cell).getNumberValue()).trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return dataFormatter.formatCellValue(cell);
                } else {
                    double numericValue = cell.getNumericCellValue();
                    BigDecimal bigDecimalValue = BigDecimal.valueOf(numericValue);
                    return bigDecimalValue.toPlainString();
                }
            case STRING:
                return cell.getStringCellValue().trim();
            default:
                return StringUtils.EMPTY;
        }
    }

    private boolean rowHasError(Row row, int lastCellImport) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) return false;

        Cell lastCell = row.getCell(lastCellImport);
        if (lastCell == null) return false;

        String value = getCellValue(lastCell);
        return org.apache.commons.lang3.StringUtils.isNotBlank(value);
    }

    private void addErrorMessage(Row row, String errorMessage) {
        int lastCell = row.getLastCellNum() < 0 ? 0 : row.getLastCellNum(); // Placeholder
        Cell cell = row.createCell(lastCell, CellType.STRING);
        cell.setCellValue(errorMessage.trim());
    }

    private static String getHumanReadableFileSize(long bytes) {
        if (bytes < 0) {
            return "0 B";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public ByteArrayResource downloadTemplate() {
        try {
            byte[] fileContent = getResourceTemplate().getInputStream().readAllBytes();
            return new ByteArrayResource(fileContent);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex.toString());
            return null;
        }
    }

    private void validateMaxLength(ExcelColumn annotation, String value, StringBuilder errors) {
        if (annotation.maxLength() > 0 && value.length() > annotation.maxLength()) {
            errors.append(Translator
                            .getMessage("VALID_002",
                                    annotation.fieldName(), String.valueOf(annotation.maxLength())))
                    .append("\n");
        }
    }

    private void validateMinLength(ExcelColumn annotation, String value, StringBuilder errors) {
        if (annotation.minLength() > 0 && value.length() < annotation.minLength()) {
            errors.append(Translator
                            .getMessage("VALID_003",
                                    annotation.fieldName(), String.valueOf(annotation.minLength())))
                    .append("\n");
        }
    }

    private String validateExtra(T instance, Field field, String value, StringBuilder errors) {
        if (errors.isEmpty()) {
            CellTransform cellTransform = validateExtra(instance, CellCallback.builder().value(value).field(field.getName()).build());
            if (cellTransform != null && StringUtils.isNotBlank(cellTransform.getMessageError())) {
                errors.append(cellTransform.getMessageError());
            }
            if (cellTransform != null && StringUtils.isNotBlank(cellTransform.getValueTransform())) {
                return cellTransform.getValueTransform();
            }
        }

        return value;
    }

    // --- Abstract methods ---
    protected List<String> allowExtensions() {
        return List.of("xls", "xlsx");
    }

    protected abstract Class<T> getTargetClass();

    protected abstract String buildDuplicateKey(T obj);

    protected abstract CellTransform validateExtra(T obj, CellCallback cellCallback);

    protected abstract Resource getResourceTemplate();

}
