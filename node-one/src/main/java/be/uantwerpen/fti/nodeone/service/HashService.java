package be.uantwerpen.fti.nodeone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HashService {
    public int calculateHash(String hostname) {
        return (int) (((long) hostname.hashCode() + (long) Integer.MAX_VALUE) * ((double) Short.MAX_VALUE / (2 * (double) Integer.MAX_VALUE)));
    }
}
