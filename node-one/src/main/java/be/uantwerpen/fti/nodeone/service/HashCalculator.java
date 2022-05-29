package be.uantwerpen.fti.nodeone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HashCalculator {
    public int calculateHash(String hostname) {
        return (int) (((long) hostname.hashCode() + (long) Integer.MAX_VALUE) * ((double) Short.MAX_VALUE / (2 * (double) Integer.MAX_VALUE)));
    }
}