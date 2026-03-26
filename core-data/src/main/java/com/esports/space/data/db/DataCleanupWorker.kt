package com.esports.space.data.db

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.dao.DeviceSnapshotDao
import com.esports.space.data.db.dao.PlaySessionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val snapshotDao: DeviceSnapshotDao,
    private val sessionDao: PlaySessionDao,
    private val eventDao: AgentEventDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        snapshotDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(7))
        sessionDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(90))
        eventDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(30))
        return Result.success()
    }
}
