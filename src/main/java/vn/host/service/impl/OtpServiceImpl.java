package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.host.service.OtpService;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final SecureRandom rnd = new SecureRandom();

    @Value("${app.otp.length:6}")
    private int len;
    @Value("${app.otp.ttl-seconds:300}")
    private long ttl;

    record Entry(String code, Instant exp) {
    }

    private String digits(int n) {
        StringBuilder s = new StringBuilder(n);
        for (int i = 0; i < n; i++) s.append(rnd.nextInt(10));
        return s.toString();
    }

    @Override
    public String issue(String email) {
        String code = digits(len);
        store.put(email.toLowerCase(), new Entry(code, Instant.now().plusSeconds(ttl)));
        return code;
    }

    @Override
    public boolean verify(String email, String code) {
        Entry e = store.get(email.toLowerCase());
        if (e == null) return false;
        if (Instant.now().isAfter(e.exp)) {
            store.remove(email.toLowerCase());
            return false;
        }
        boolean ok = e.code.equals(code);
        if (ok) store.remove(email.toLowerCase());
        return ok;
    }

    @Override
    public void invalidate(String email) {
        store.remove(email.toLowerCase());
    }
}