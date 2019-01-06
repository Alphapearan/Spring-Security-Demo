# Spring Security 简单使用

我们使用 Spring Boot 作为项目的框架，因此本教程介绍的是 Spring Security 与 Spring Boot 的集成与使用。

## 1. 引入依赖

```xml
<dependencies>
     <!--导入 Spring Boot 对 Spring Security 的支持-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!--导入 Thymeleaf 对 Spring Security 的支持-->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity4</artifactId>
    </dependency>
</dependencies>
```

此时直接运行项目，会弹出浏览器默认的身份验证窗口，被称为 Spring Security 的 httpBasicLogin。这是由于 Spring Security 生成了一个默认的用户，其用户名为 user，密码则是随机生成的，在运行日志中可以看到。但是我们不需要这类验证方式，通过以下配置关闭它。

```properties
security.basic.enable=false
```

## 2. Spring Security Demo

### (1) 创建实体对象

**创建权限类 Authority**

为了指示该类是一个权限类，需要实现 GrantedAuthority 接口，该接口只有一个方法 getAuthority() 获取为系统定义的权限。

```java
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 权限
 */
@Entity
public class Authority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name; // 权限名称

    /**
     * 获取权限名称  
     */
    @Override
    public String getAuthority() {
        return name;
    }

    // 省略 getter、setter 方法
}
```

**创建用户类 User**

用户类需要实现 UserDetails 接口，用于表示该类为用户类。Spring Security 默认使用 username 字段作为登录名，使用 password 字段作为密码，因此代码中使用这两个字段表示用户名和密码更为方便。

```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户
 */
@Entity
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Size(min = 2, max = 20)
    @Column(nullable = false, length = 20)
    private String username; // 登录名用 username 表示更为容易

    @NotEmpty(message = "密码不能为空")
    @Size(min = 2, max = 20)
    @Column(nullable = false, length = 20)
    private String password; // 密码用 password 表示更方便

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinTable( // 用户与权限是多对多关系
            name = "user_authority",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "authority_id",
                    referencedColumnName = "id"
            )
    )
    private List<Authority> authorities; // 权限

    // 省略 getter、setter 方法

    /**
     * 获取登录名
     */
    @Override
    public String getUsername() {
        return username; // 若系统使用如 email 作为登录名，请改为 email 或定义 username = email
    }

    /**
     * 获取用户权限
     * 在该方法中将 List<Authority> 转成 List<SimpleGrantedAuthority> 便于框架识别
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        List<Authority> authorities = this.authorities;
        for (Authority authority : authorities) {
            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        return simpleGrantedAuthorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

**DAO 接口**

Spring Security 通过登录名从数据库中查找用户信息，再与输入的密码进行比较，因此 DAO 接口中一定要有实现类似数据库查询的方法。若是用 email 作为登录名，改为 findByEmail()。

```java
import com.neo.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

/**
 * 用户 DAO 接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Spring Security 通过登录名从数据库中查找用户信息，再与输入的密码进行比较
     */
    UserDetails findByUsername(String username);
}
```

**UserDetailsService**

由于我们要从数据库中获取用户信息，因此我们需要自定义实现 UserDetailsService 接口的 loadUserByUsername() 方法。

```java
import com.neo.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义 UserDetailsService
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepository.findByUsername(s);
    }
}
```

编写以上所需的类的代码之后，我们可以通过以下简单的 Spring Security 配置实现登录功能。

```java
/**
 * Spring Security 简单配置
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin() // 使用 form 表单登录
                .and()
                .authorizeRequests()
                .anyRequest().authenticated(); // 拦截任何请求
    }
}
```

运行程序，使用浏览器访问 http://localhost:8080/ 会自动跳转到一个默认的登录页面，在该页面登录后可以访问系统的其他页面。

## Spring Security 配置

**请求授权**

Spring Security 通过重写 configure(HttpSecurity http) 方法实现请求拦截的。

Spring Security 使用以下匹配其来匹配请求路径：

* antMatchers：使用 Ant 风格的路径匹配
* regexMatchers：使用正则表达式匹配路径
* anyRequest：匹配所有请求路径

<未完待续>






