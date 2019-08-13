package washington.franca.com.navtest.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import washington.franca.com.navtest.model.NewsSuggestion

@androidx.room.Database(version = 1, entities = [NewsSuggestion::class])
abstract class Database : RoomDatabase() {
    abstract fun newsSuggestionDao(): NewsSuggestionDao
    companion object {
        private val sObject = Object()
        private var db:Database? = null
        @Synchronized
        fun get(context:Context):Database {
            synchronized(sObject) {
                if(db == null) {
                    db = Room.databaseBuilder(context.applicationContext, Database::class.java, "db").build()
                }
                return db!!
            }
        }
    }


}