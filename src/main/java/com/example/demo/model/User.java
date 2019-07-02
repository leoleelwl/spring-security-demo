package com.example.demo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.example.demo.event.UserEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "t_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class User extends BaseModel{

    /**
     *  这个注解会在表里创建一个user_seq表 用于序列号的生成
     *  use {@link GeneratedValue}
     *  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
     *  use  {@link SequenceGenerator }
     *  @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
     *  @GeneratedValue(GenerationType.AUTO) jpa 会自动生成一张 sequence 表
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "USERNAME",unique = true,length = 50)
    @Size(min = 3,max = 50/*,message = "参数必须在 {min} 到 {max}之间"*/)
    private String username;

    @Column(name = "PASSWORD", length = 100)
    /*@NotNull*/
    @Size(min = 4, max = 100)
    /*@JsonIgnore*/
    private String password;


    @Column(name = "AGE", length = 3)
    private Integer age;

    @Column(name = "EMAIL", length = 50)
    @Size(min = 4, max = 50)
    private String email;

    private Boolean enabled;

    /**
     * ManyToMany 多对多关系
     * FetchType.Eager : 立即加载此属性
     *
     * @JoinTable： {
     * name : 中间表的表名  T_USER_ROLE 有两个字段{ userid roleid}
     * joinColumns : {
     * @JoinColumn (name : userid ( 中间表的其中一个字段名)
     * referencedColumnName : 表示 中间表 userid 字段 对应本表 user表中 的 关联的字段 即 t_user.id字段
     * )
     * }
     * inverseJoinColumns：(
     * @JoinColumn name : roleid(中间表的其中的另一个字段名)
     * referencedColumnName : 表示 中间表 roleid 字段 对应权限表(t_role) 中的关联的字段即 t_role.id字段
     * )
     * }
     * }
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH,CascadeType.PERSIST})
    @JoinTable(name = "T_USER_ROLE",
            joinColumns = {@JoinColumn(name = "userid", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "roleid", referencedColumnName = "id")}
    )
    /*@JsonIgnoreProperties({"id"})*/
    private List<Roles> roles;
//    private List<Roles> roles = new ArrayList<>();

    @DomainEvents
    List<UserEvent> domainEvents() {
        // … return events you want to get published here
        System.out.println("user.domainEvents");
        ArrayList<UserEvent> events = new ArrayList<>();
        events.add(new UserEvent(this.id));
        return events;
    }

    @AfterDomainEventPublication
    void callbackMethod() {
        System.out.println("user.AfterDomainEventPublication");
    }
}
