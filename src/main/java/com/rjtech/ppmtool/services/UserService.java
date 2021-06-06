package com.rjtech.ppmtool.services;

import com.rjtech.ppmtool.domain.User;
import com.rjtech.ppmtool.exceptions.UsernameAlreadyExistsException;
import com.rjtech.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User saveUser(User newUser){

        try {
            newUser.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
            //Username has to be unique (exception)
            newUser.setUsername(newUser.getUsername());
            // Make sure password and confirm password match
            // We dont persist or show the confirmPassword
            newUser.setConfirmPassword("");
            return userRepository.save(newUser);
        } catch (Exception e){
            throw new UsernameAlreadyExistsException("Username '" + newUser.getUsername() + "' already exists.");
        }

    }
}
