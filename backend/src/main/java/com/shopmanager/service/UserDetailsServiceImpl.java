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
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(identifier);
        if (entry != null && now - entry.loadedAtMillis() < TTL_MILLIS) {
            return entry.details();
        }
        String lookup = identifier.trim();
        UserDetails details = userRepository.findByUsernameIgnoreCase(lookup)
                .or(() -> userRepository.findByEmailIgnoreCase(lookup))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        cache.put(identifier, new CacheEntry(details, now));
        return details;
    }

    public void evict(String username) {
        if (username != null) {
            cache.remove(username);
        }
    }

    public void evictUser(com.shopmanager.entity.User user) {
        if (user == null) {
            return;
        }
        evict(user.getUsername());
        evict(user.getEmail());
    }
}
