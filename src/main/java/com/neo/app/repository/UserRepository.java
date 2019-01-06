package com.neo.app.repository;

import com.neo.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

/**
 * 用户数据支持类
 * Created by 杨颖 on 2018/12/1.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	/**
	 * 根据用户名查找
     * Spring Security 通过登录名从数据库中查找用户信息，再与输入的密码进行比较
	 * 若是用 email 作为登录名，改为 findByEmail()
	 */
	UserDetails findByUsername(String username);
}
