package vn.com.lifesup.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.lifesup.base.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    List<Role> findByCodeInAndStatusEquals(String[] roleCodes, int status);

    Optional<Role> findByCode(String code);

    Optional<Role> findByName(String name);

    Optional<Role> findFirstByNameIgnoreCase(String name);
}
