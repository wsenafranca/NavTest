package washington.franca.com.navtest.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import washington.franca.com.navtest.model.NewsSuggestion

@Dao
interface NewsSuggestionDao {
    @Query("SELECT * FROM news_suggestion WHERE text LIKE :query ORDER BY time DESC")
    fun search(query:String): Maybe<List<NewsSuggestion>>
    @Query("SELECT * FROM news_suggestion ORDER BY time DESC")
    fun getAll():Maybe<List<NewsSuggestion>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(vararg suggestions: NewsSuggestion): Completable
    @Delete
    fun delete(suggestion: NewsSuggestion):Completable
}