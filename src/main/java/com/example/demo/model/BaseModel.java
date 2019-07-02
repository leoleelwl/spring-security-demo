package com.example.demo.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Data
public class BaseModel{

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createtime")
    private Date createTime;
}
