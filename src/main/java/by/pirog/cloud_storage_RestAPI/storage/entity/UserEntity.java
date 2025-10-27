package by.pirog.cloud_storage_RestAPI.storage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="t_user", schema="users")
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="c_id")
    private Long id;

    @Column(name="c_username", unique=true, nullable=false)
    private String username;

    @Column(name="c_password", nullable=false)
    private String password;
}
