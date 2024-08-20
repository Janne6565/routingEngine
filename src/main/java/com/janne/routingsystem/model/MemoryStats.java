package com.janne.routingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryStats {
    private float heapSize;
    private float heapMaxSize;
    private float heapFreeSize;
}
