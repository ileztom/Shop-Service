package com.shop.service

import com.shop.domain.model.OrderStatsDTO
import com.shop.repository.StatsRepository

class StatsService {
    fun getOrderStats(): OrderStatsDTO {
        return StatsRepository.getOrderStats()
    }
}
