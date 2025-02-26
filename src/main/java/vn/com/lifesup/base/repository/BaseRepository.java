package vn.com.lifesup.base.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.common.BaseSearchDTO;
import vn.com.lifesup.base.dto.common.OrderDTO;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class BaseRepository {

    @Getter
    @PersistenceContext
    private EntityManager entityManager;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Get current session from EntityManager
     *
     * @return Session
     */
    protected Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    protected NamedParameterJdbcTemplate getJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    protected <T> ApiResponse<List<T>> searchAndCountTotal(BaseSearchDTO searchDTO,
                                                           String sqlString,
                                                           Map<String, Object> parameters,
                                                           Class<T> clazz) {
        ApiResponse<List<T>> response = new ApiResponse<>();
        response.setTotalRecords(countNativeTotalRecords(sqlString, parameters));
        sqlString = getSqlPaging(searchDTO, sqlString, parameters);
        response.setData(namedParameterJdbcTemplate.query(sqlString, parameters, BeanPropertyRowMapper.newInstance(clazz)));
        return response;
    }

    protected Integer getOffset(BaseSearchDTO searchDto) {
        return searchDto.getPage() * searchDto.getPageSize();
    }

    private String getSqlPaging(BaseSearchDTO searchDto, String sql, Map<String, Object> parameters) {
        StringBuilder sqlBuilder = new StringBuilder();
        sql = sql.replaceAll("(?i) from ", " from ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" limit :p_offset, :p_page_size");
        parameters.put("p_offset", getOffset(searchDto));
        parameters.put("p_page_size", searchDto.getPageSize());
        return sqlBuilder.toString();
    }

    protected Integer countNativeTotalRecords(String sqlString, Map<String, ?> parameters) {
        return namedParameterJdbcTemplate.queryForObject("select count(*) from (" + sqlString + ") a", parameters, Integer.class);
    }

    protected String getOrderBy(boolean ascending) {
        return ascending ? OrderDTO.ASC : OrderDTO.DESC;
    }
}
