package me.j360.trace.example.dubbo.service;

/**
 * Package: me.j360.trace.example.dubbo.service
 * User: min_xu
 * Date: 16/9/22 下午2:43
 * 说明：
 */
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(Long uid) {
        return String.format("_%d",uid);
    }
}
