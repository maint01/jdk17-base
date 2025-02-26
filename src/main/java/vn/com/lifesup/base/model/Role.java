package vn.com.lifesup.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Role.
 */
@Getter
@Entity
@Table(name = "jhi_role")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private String id;

    private String code;

    private String name;

    private String description;

    private Integer status;

    @Column(name = "create_date")
    private Instant createDate;

    @Column(name = "create_user")
    private String createUser;
    
    @Column(name = "modify_date")
    private Instant modifyDate;

    @Column(name = "modify_user")
    private String modifyUser;
    
}
