package kubisp.habittracker.persistence.entry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query(
        "SELECT * FROM entry WHERE habit_id = :habitId AND date BETWEEN :firstDay AND :lastDay ORDER BY added_order"
    )
    fun entriesOnDays(habitId: String, firstDay: String, lastDay: String): Flow<List<EntryRow>>

    @Query("SELECT added_order FROM entry WHERE id = :id")
    suspend fun addedOrderOf(id: String): Long?

    @Insert
    suspend fun insert(row: EntryRow)

    @Update
    suspend fun update(row: EntryRow)

    @Query("DELETE FROM entry WHERE id = :id")
    suspend fun delete(id: String)
}
