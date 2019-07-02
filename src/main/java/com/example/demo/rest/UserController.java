package com.example.demo.rest;

import com.auth0.jwt.JWT;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MyUserDetailsService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder encoder;

    @Value("${app.security.tokenHeader}")
    private String TOKEN_HEADER;

    @Autowired
    DaoAuthenticationProvider daoAuthenticationProvider;

    @Autowired
    RoleRepository roleRepository;
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody @NotNull @Valid User user){
        user.setPassword(encoder.encode(user.getPassword()));
        List<Roles> rolesList = user.getRoles().stream().map(e -> roleRepository.findById(e.getId()).get()).collect(Collectors.toList());
        user.setRoles(rolesList);
        User save = repository.save(user);
        return ResponseEntity.ok(save);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> findUser(@NotNull @PathVariable String username){
        User user = repository.findUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody @NotNull @Valid User user){
        User updateUser = repository.updateUser(user);
        return ResponseEntity.ok(updateUser);
    }

    @Autowired
    MyUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @NotNull @Valid User user, HttpServletRequest request){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword());
        Authentication authenticate = daoAuthenticationProvider.authenticate(token);
        Collection<? extends GrantedAuthority> grantedAuthorities = authenticate.getAuthorities();
        //拿到用户的权限
        List<String> roles = grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        authenticate.getCredentials();
        //生成token
        String signedToken = userDetailsService.saveUser((UserDetails) authenticate.getPrincipal());
        //拿到token过期时间
        Date expiresAt = JWT.decode(signedToken).getExpiresAt();
        ImmutableMap<String, Object> map =
                ImmutableMap.of("username", user.getUsername(),
                "roles", roles.toArray(),
                "token", signedToken,
                "expireAt", expiresAt);
        return ResponseEntity.ok(map);
    }

    public String extractTokenHeader(HttpServletRequest request){
        return request.getHeader(TOKEN_HEADER);
    }

}
