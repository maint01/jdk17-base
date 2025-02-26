package vn.com.lifesup.base.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class BaseSearchDTO {
    @Schema(description = "Trang", example = "0")
    private Integer page;
    @Schema(description = "Số bản ghi 1 trang", example = "10")
    private Integer pageSize;
    @Schema(description = "Từ khóa tìm kiếm nhiều trường")
    private String keyword;
    private List<String> sorts;

}
