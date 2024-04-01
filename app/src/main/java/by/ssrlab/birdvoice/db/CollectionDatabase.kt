package by.ssrlab.birdvoice.db

import androidx.room.Database
import androidx.room.RoomDatabase
import by.ssrlab.birdvoice.db.objects.CollectionBird

@Database(entities = [CollectionBird::class], version = 6, exportSchema = false)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao() : CollectionDao
}