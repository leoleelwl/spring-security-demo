package com.example.demo.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    public String  hello(){
        return "hello gays";
    }


    @PreAuthorize("#u == 'zhangsan'")
    @RequestMapping(value = "/greeting",method = RequestMethod.GET)
    public String  greeting(@P("u") String username){
        return "hello "+username;
    }


    @RequestMapping(value = "/hey",method = RequestMethod.GET)
    public String  hey(){
        return "hey spring security";
    }

}
