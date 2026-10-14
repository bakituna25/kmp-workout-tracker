package com.example.aigymapp.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.sqlite.db.SupportSQLiteDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "aigym.db",
            callback = object : AndroidSqliteDriver.Callback(AppDatabase.Schema) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Schema will be created automatically by SQLDelight
                    // Enable foreign keys after creation
                    db.execSQL("PRAGMA foreign_keys=ON;")
                }
                
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Always enable foreign keys on open
                    db.execSQL("PRAGMA foreign_keys=ON;")

                    // scheduled_workouts.sort_order kolonunu kontrol et ve yoksa ekle
                    // (manuel sürükle-bırak sıralama için). Mevcut kayıtlar, o günkü
                    // mevcut görünüm sırasını (id DESC) koruyacak şekilde geriye dönük dolduruluyor.
                    try {
                        db.query("SELECT sort_order FROM scheduled_workouts LIMIT 1").use { }
                    } catch (e: Exception) {
                        db.execSQL("ALTER TABLE scheduled_workouts ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0")
                        db.execSQL(
                            """
                            UPDATE scheduled_workouts
                            SET sort_order = (
                                SELECT COUNT(*) FROM scheduled_workouts sw2
                                WHERE sw2.date_epoch_day = scheduled_workouts.date_epoch_day
                                  AND sw2.id > scheduled_workouts.id
                            )
                            """.trimIndent()
                        )
                    }

                    // UserProfile tablosunu kontrol et ve yoksa oluştur
                    // Bu, mevcut veritabanlarında eksik olan tabloyu ekler
                    try {
                        db.query("SELECT 1 FROM UserProfile LIMIT 1").use { cursor ->
                            // Tablo var, hiçbir şey yapma
                        }
                    } catch (e: Exception) {
                        // Tablo yok, oluştur
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS UserProfile (
                                id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
                                name TEXT NOT NULL DEFAULT 'Misafir',
                                height REAL,
                                weight REAL,
                                fatRatio REAL,
                                age INTEGER,
                                gender TEXT,
                                avatar BLOB
                            )
                        """.trimIndent())
                    }

                    // UserProfile.avatar kolonunu kontrol et ve yoksa ekle
                    // (profil fotoğrafı özelliği için).
                    try {
                        db.query("SELECT avatar FROM UserProfile LIMIT 1").use { }
                    } catch (e: Exception) {
                        db.execSQL("ALTER TABLE UserProfile ADD COLUMN avatar BLOB")
                    }

                    // Goals tablosunu kontrol et ve yoksa oluştur
                    // Hedef takibi için yeni tablo
                    try {
                        db.query("SELECT 1 FROM goals LIMIT 1").use { cursor ->
                            // Tablo var, hiçbir şey yapma
                        }
                    } catch (e: Exception) {
                        // Tablo yok, oluştur
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS goals (
                                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                                title TEXT NOT NULL,
                                description TEXT,
                                goal_type TEXT NOT NULL,
                                target_value INTEGER NOT NULL,
                                exercise_id INTEGER,
                                muscle_group TEXT,
                                start_date TEXT NOT NULL,
                                end_date TEXT,
                                is_active INTEGER NOT NULL DEFAULT 1,
                                created_at INTEGER NOT NULL,
                                FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
                            )
                        """.trimIndent())
                        
                        // İndeksleri de oluştur
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_exercise_id ON goals(exercise_id)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_is_active ON goals(is_active)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_goal_type ON goals(goal_type)")
                    }
                }
                
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    super.onUpgrade(db, oldVersion, newVersion)
                    // Migration handling
                    // UserProfile tablosunu ekle (eğer yoksa)
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS UserProfile (
                            id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
                            name TEXT NOT NULL DEFAULT 'Misafir',
                            height REAL,
                            weight REAL,
                            fatRatio REAL,
                            age INTEGER,
                            gender TEXT,
                            avatar BLOB
                        )
                    """.trimIndent())

                    // Goals tablosunu ekle (eğer yoksa)
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS goals (
                            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            title TEXT NOT NULL,
                            description TEXT,
                            goal_type TEXT NOT NULL,
                            target_value INTEGER NOT NULL,
                            exercise_id INTEGER,
                            muscle_group TEXT,
                            start_date TEXT NOT NULL,
                            end_date TEXT,
                            is_active INTEGER NOT NULL DEFAULT 1,
                            created_at INTEGER NOT NULL,
                            FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
                        )
                    """.trimIndent())
                    
                    // İndeksleri de oluştur
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_exercise_id ON goals(exercise_id)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_is_active ON goals(is_active)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_goal_type ON goals(goal_type)")
                }
            }
        )
    }
}

