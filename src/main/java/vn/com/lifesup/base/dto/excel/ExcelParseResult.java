package vn.com.lifesup.base.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelParseResult<T> {
    private List<T> data;         // danh sách đối tượng parse được
    private Boolean hasError;     // có lỗi validate hay không
    private Integer errorCount;   // tổng số dòng bị lỗi
    private ByteArrayResource fileOut; // file excel (đã được append cột lỗi)
}
