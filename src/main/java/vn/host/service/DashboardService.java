package vn.host.service;

import vn.host.dto.dashboard.DashboardRes;

import java.time.Instant;

public interface DashboardService {
    DashboardRes buildForShop(Long shopId, Instant fromInclusive, Instant toExclusive);
}
