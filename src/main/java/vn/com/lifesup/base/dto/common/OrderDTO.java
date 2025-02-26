package vn.com.lifesup.base.dto.common;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderDTO {
    public static final String DESC = "desc";
    public static final String ASC = "asc";
    private String property;
    @JsonProperty
    private boolean ascending;
}
