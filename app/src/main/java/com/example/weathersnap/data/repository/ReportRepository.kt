package com.example.weathersnap.data.repository

import com.example.weathersnap.data.local.dao.ReportDao
import com.example.weathersnap.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun saveReport(report: ReportEntity) {
        reportDao.insertReport(report)
    }
}
