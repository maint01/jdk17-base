package vn.com.lifesup.base.dto.excel;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CellTransform {
    private String valueTransform;
    private String messageError;
}
