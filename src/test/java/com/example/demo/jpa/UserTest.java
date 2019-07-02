package com.example.demo.jpa;

import com.example.demo.model.RoleName;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;


@SpringBootTest
@RunWith(SpringRunner.class)
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Test
    public void saveUser(){
 /*       Roles role = new Roles();
        role.setName(RoleName.ROLE_USER);
        ArrayList<Roles> list = new ArrayList<>();
        list.add(role);*/
        User user = new User();
        user.setUsername("testLister");
        //user.setRoles(list);
        userRepository.save(user);
    }

    @Test
    @Transactional
    public void addUser(){
        User user1 = new User();
        user1.setUsername("peter");

        Roles role1 = new Roles();
        role1.setName(RoleName.ROLE_ADMIN);
        Roles role2 = new Roles();
        role2.setName(RoleName.ROLE_USER);
        ArrayList<Roles> roleList = new ArrayList<>();
        roleList.add(role1);
        roleList.add(role2);
        user1.setRoles(roleList);
        userRepository.save(user1);
    }

    @Test
    public void delUser(){
        userRepository.deleteAll(Lists.newArrayList(new User().setId(1L),new User().setId(4L)));
    }

    @Test
    public void queryUserByPage(){
        // page 这里是从 第 0 页 开始
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.Direction.DESC,"id");
        Page<User> allUser = userRepository.findAll(pageRequest);
        List<User> users = allUser.getContent();
        System.out.println(users);
    }

    @Test
    public void findUser(){
        User user = userRepository.selectUserByUserId(1);
        System.out.println(user);
    }


    @Test
    @Transactional
    public void updateUser(){
        ArrayList<Long> userIds = new ArrayList<>();
        userIds.add(1L);
        Integer id = userRepository.updateUserByUserId(userIds);
        System.out.println("user.id"+id);
    }

    @Test
    public void queryUserByExample(){
        User user = new User().setEnabled(true).setUsername("testLister");
        Example<User> userExample = Example.of(user);
        List<User> userList = userRepository.findAll(userExample);
        userList.stream().forEach(System.out::println);
    }

    @Test
    public void queryUserByExampleMatcher(){
        User user = new User().setUsername("test");
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.startsWith());
        Example<User> userExample = Example.of(user, matcher);
        userRepository.findAll(userExample).stream().forEach(System.out::println);
    }

    @Test
    public void queryUserBySpecification(){
        List<User> users = userRepository.findAll((Specification<User>) (root, criteriaQuery, criteriaBuilder) -> {
            Predicate between = criteriaBuilder.between(root.get("id"), 1, 5);
                    return between;
                }
        );
        System.out.println(users);
    }

    @Test
    public void addUser2(){
        User user = new User().setUsername("aaaaaaaaaa").setId(37l);
        List<Roles> roles = Arrays.asList(new Roles().setName(RoleName.ROLE_ADMIN).setId(17l),
                new Roles().setName(RoleName.ROLE_USER).setId(18l));
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Test
    public void deleteUser(){
        userRepository.delete(new User().setId(44L));
    }

    @Test
    public void testOrElse(){
        User jhy = new User().setId(1L);
        /*User user = jhy.stream()
                .filter(u -> u.getUsername().equals("jhy")).findAny()
                .orElse(new User().setId(2L).setUsername("not found"));*/

        User user = Optional.of(jhy).orElse(getUser());
        System.out.println(user);
        User user2 = Optional.of(jhy).orElseGet(()->getUser());
        System.out.println(user2);
    }

    public User getUser(){
        System.out.println("getUser()");
        return new User().setId(2L).setUsername("not found");
    }

    public static void testFaltMap(){
        User user1 = new User()
                        .setId(1L)
                        .setUsername("jhy")
                        .setRoles(Arrays.asList(
                                new Roles().setId(1L).setName(RoleName.ROLE_ADMIN),
                                new Roles().setId(2L).setName(RoleName.ROLE_ADMIN))
        );
        User user2 = new User().setId(2L).setUsername("xxx").setRoles(
                Arrays.asList(new Roles().setId(1L).setName(RoleName.ROLE_ADMIN),
                        new Roles().setId(2L).setName(RoleName.ROLE_ADMIN))
        );
        List<User> users = Arrays.asList(user1, user2);
        //users.stream().forEach(u-> System.out.println(u));
        users.parallelStream().map(u->u.getRoles()).forEach(r-> System.out.println(r));
        System.out.println("------------------------------------");
        users.parallelStream().flatMap(u->u.getRoles().stream()).forEach(System.out::println);
    }

    public static void main(String[] args) {
        testFaltMap();
    }
}
