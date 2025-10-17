package vn.com.lifesup.base.dto.excel;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CellCallback {
    private String field;
    private String value;
}
