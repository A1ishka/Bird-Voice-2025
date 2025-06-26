package by.dis.birdvoice.db.objects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birds_collection")
data class CollectionBird(
    @PrimaryKey
    val name: String,
    val image: String,
)