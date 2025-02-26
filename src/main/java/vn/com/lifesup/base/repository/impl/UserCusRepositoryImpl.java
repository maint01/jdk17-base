package vn.com.lifesup.base.repository.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.user.UserDTO;
import vn.com.lifesup.base.dto.user.UserSearchDTO;
import vn.com.lifesup.base.repository.BaseRepository;
import vn.com.lifesup.base.repository.UserCusRepository;
import vn.com.lifesup.base.util.FnCommon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserCusRepositoryImpl extends BaseRepository implements UserCusRepository {

    @Override
    public ApiResponse<List<UserDTO>> filter(UserSearchDTO searchDTO) {
        StringBuilder sql = getStringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        if (StringUtils.isNotBlank(searchDTO.getUsername())) {
            sql.append(" and user.login like :username");
            parameters.put("username", FnCommon.makeLikeParam(searchDTO.getUsername().trim()).toLowerCase());
        }

        if (StringUtils.isNotBlank(searchDTO.getFullName())) {
            sql.append(" and user.full_name like :fullName");
            parameters.put("fullName", FnCommon.makeLikeParam(searchDTO.getFullName().trim()).toLowerCase());
        }

        if (StringUtils.isNotBlank(searchDTO.getMobilePhone())) {
            sql.append(" and user.mobile_phone like :mobilePhone");
            parameters.put("mobilePhone", FnCommon.makeLikeParam(searchDTO.getMobilePhone()).toLowerCase());
        }

        if (StringUtils.isNotBlank(searchDTO.getAuthority())) {
            sql.append(" and authority.name = :authority");
            parameters.put("authority", searchDTO.getAuthority());
        }

        if (searchDTO.getStatus() != null) {
            sql.append(" and user.activated = :status");
            parameters.put("status", searchDTO.getStatus());
        }
        sql.append(" \ngroup by user.login, user.first_name, user.last_name," +
                " user.email, user.activated, user.lang_key" +
                " , user.created_by, user.created_date, user.last_modified_by, user.last_modified_date");
        return queryPage(searchDTO, sql.toString(), parameters, UserDTO.class);
    }

    private static StringBuilder getStringBuilder() {
        String queryString = "select\n" +
                "    user.id\n" +
                "    , user.login\n" +
                "    , concat(user.first_name, ' ', user.last_name)  fullName\n" +
                "    , user.email\n" +
                "    , user.activated status\n" +
                "    , case when user.activated = 1 then 'Hoạt động' else 'Không hoạt động' end statusName\n" +
                "    , user.lang_key\n" +
                "    , user.created_by createdBy\n" +
                "    , user.created_date createdDate\n" +
                "    , user.last_modified_by lastModifiedBy\n" +
                "    , user.last_modified_date lastModifiedDate\n" +
                "    , group_concat(authority.name) authority\n" +
                "from jhi_user user\n" +
                "join jhi_user_authority map on map.user_id=user.id\n" +
                "join jhi_authority authority on authority.name=map.authority_name\n" +
                "where 1=1";
        return new StringBuilder(queryString);
    }
}
