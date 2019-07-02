package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * PagingAndSortingRepository 继承 CrudRepository
 * JpaRepository 继承 PagingAndSortingRepository
 * CrudRepository 提供基本的增删改查；
 * PagingAndSortingRepository 提供分页和排序方法；
 * JpaRepository 提供JPA需要的方法。
 * 在使用的时候，可以根据具体需要选中继承哪个接口。
 * {@link org.springframework.data.repository.CrudRepository}
 */
public interface UserRepository extends JpaSpecificationExecutor<User>, JpaRepository<User, Long>{

     @Query(value = "from User u where u.id =:id")
     User selectUserByUserId(@Param("id") long id);

     @Modifying
     @Query(value = "update User u set u.enabled =false where u.id in :userIds")
     Integer updateUserByUserId(@Param("userIds" )List<Long> userIds);

     User findUserByUsername(String username);

     default User updateUser(User user){
         User savedUser = this.save(user);
         return savedUser;
     }
}
