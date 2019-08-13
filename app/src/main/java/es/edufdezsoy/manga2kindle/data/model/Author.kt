package es.edufdezsoy.manga2kindle.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Author(
    @PrimaryKey val id: Int,
    val name: String?,
    val surname: String?,
    val nickname: String?
)