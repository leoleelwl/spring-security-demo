package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "t_role")
@Accessors(chain = true)
public class Roles{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private RoleName name;

    /**
     * 1.@Transient
     * 2.private transient List<User> users;
     * 以上两种表示此属性不序列化 否则会发生循环引用调toString方法
     * JsonInclude.Include.NON_NULL : 忽略值是null的 字段
     * mappedBy : 表示被维护端 users 有值的话 不会向中间表插入数据
     * @see {com.example.demo.jpa.UserTest}
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ManyToMany(mappedBy ="roles",fetch = FetchType.LAZY)
    private transient List<User> users;

}
