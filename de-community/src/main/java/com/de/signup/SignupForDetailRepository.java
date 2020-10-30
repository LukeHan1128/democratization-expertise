package com.de.signup;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.de.user.Users;
import com.de.user.Usersdetail;

@Repository
public interface SignupForDetailRepository extends JpaRepository<Usersdetail, Integer>{
	List<Usersdetail> findAll();
   List<Usersdetail> findByEnterpriseno(Integer enterpriseno);
   
}
