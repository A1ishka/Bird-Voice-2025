package by.dis.birdvoice.db

import androidx.room.*
import by.dis.birdvoice.db.objects.CollectionBird

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(collectionBird: CollectionBird)

    @Query("SELECT * FROM birds_collection")
    fun getCollection() : List<CollectionBird>

    @Delete
    fun delete(collectionBird: CollectionBird)
}