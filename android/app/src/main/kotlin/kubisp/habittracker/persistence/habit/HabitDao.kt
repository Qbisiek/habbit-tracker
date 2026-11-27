package kubisp.habittracker.persistence.habit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit ORDER BY added_order")
    fun habits(): Flow<List<HabitRow>>

    @Query("SELECT * FROM habit WHERE id = :id")
    fun habit(id: String): Flow<HabitRow?>

    @Query("SELECT added_order FROM habit WHERE id = :id")
    suspend fun addedOrderOf(id: String): Long?

    @Insert
    suspend fun insert(row: HabitRow)

    // A plain UPDATE, never INSERT OR REPLACE: a replace deletes the row first, and the cascade would delete its entries.
    @Update
    suspend fun update(row: HabitRow)
}
