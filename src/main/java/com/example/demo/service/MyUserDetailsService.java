package com.example.demo.service;

import com.example.demo.constant.AppConstant;
import com.example.demo.event.UserEvent;
import com.example.demo.model.Roles;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class MyUserDetailsService implements UserDetailsService {

    private Map<String,String> tokenMap = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.demo.model.User user = userRepository.findUserByUsername(username);
        List<Roles> userRoles = user.getRoles();
        List<SimpleGrantedAuthority> authorities =
                userRoles.stream().map(u -> new SimpleGrantedAuthority(u.getName().name()))
                        .collect(Collectors.toList());
        User userDetails = new User(username, user.getPassword(), authorities);
        return userDetails;
    }

    public UserDetails getUserInfo(String username){
        String salt = tokenMap.get(AppConstant.USER_SALT_PREFIX+"_"+username);
        if(StringUtils.isEmpty(salt))
            throw new NonceExpiredException("token已过期");
        /**
         * TODO 从数据库或者缓存中取出jwt token生成时用的salt
         * salt = redisTemplate.opsForValue().get("token:"+username);
         */

        UserDetails userDetails = this.loadUserByUsername(username);
        User.UserBuilder builder = User.withUserDetails(userDetails).password(salt);
        return builder.build();
    }

    public String saveUser(UserDetails userDetails){
        String username = userDetails.getUsername();
        String salt = BCrypt.gensalt();
        tokenMap.put(AppConstant.USER_SALT_PREFIX+"_"+username, salt);
        /**
         * @todo 将salt保存到数据库或者缓存中
         * redisTemplate.opsForValue().set("token:"+username, salt, 3600, TimeUnit.SECONDS);
         */
        return JwtUtils.sign(username, salt);
    }
    //接受User发出的类型为UserSaveEvent的DomainEvents事件
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void event(UserEvent event){
        System.out.println("TransactionalEventListener.........");
        System.out.println(event.getId());
    }
}
