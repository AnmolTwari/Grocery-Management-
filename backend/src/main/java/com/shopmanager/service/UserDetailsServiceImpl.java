package com.shopmanager.service;

import java.util.concurrent.ConcurrentHashMap;

import com.shopmanager.entity.User;
import com.shopmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final long TTL_MILLIS = 5 * 60 * 1000L;

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private record CacheEntry(UserDetails details, long loadedAtMillis) {
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(username);
        if (entry != null && now - entry.loadedAtMillis() < TTL_MILLIS) {
            return entry.details();
        }
        UserDetails details = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        cache.put(username, new CacheEntry(details, now));
        return details;
    }

    public void evict(String username) {
        if (username != null) {
            cache.remove(username);
        }
    }
}
