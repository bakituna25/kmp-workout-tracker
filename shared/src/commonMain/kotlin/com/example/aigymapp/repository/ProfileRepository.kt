package com.example.aigymapp.repository

import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.UserProfile
import com.example.aigymapp.database.asFlowOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * ProfileRepository - Kullanıcı profil verilerini yöneten repository sınıfı
 * SQLDelight kullanarak UserProfile tablosunu yönetir
 */
class ProfileRepository(private val database: AppDatabase) {

    /**
     * Kullanıcı profil bilgilerini Flow olarak döner
     * id = 1 olan tek kullanıcı profili döner
     */
    val userProfile: Flow<UserProfile?> = database.userProfileQueries.getUser().asFlowOneOrNull()

    /**
     * Kullanıcı profil bilgilerini veritabanına kaydeder
     * INSERT OR REPLACE kullanarak her zaman id=1'e kaydeder
     * 
     * @param name Kullanıcı adı
     * @param height Boy (cm)
     * @param weight Kilo (kg)
     * @param fat Vücut yağ oranı (%)
     * @param age Yaş
     * @param gender Cinsiyet
     */
    suspend fun saveProfile(
        name: String,
        height: Double?,
        weight: Double?,
        fat: Double?,
        age: Long?,
        gender: String,
        avatar: ByteArray?
    ) = withContext(Dispatchers.Default) {
        database.userProfileQueries.insertOrUpdate(
            name = name,
            height = height,
            weight = weight,
            fatRatio = fat,
            age = age,
            gender = gender,
            avatar = avatar
        )
    }

    /**
     * Sadece profil fotoğrafını günceller, diğer alanlara dokunmaz.
     * Kullanıcı hiç "Kaydet" yapmadıysa id=1 satırı henüz yok demektir;
     * updateAvatar'ın kendisi düz bir UPDATE olduğundan önce satırı
     * garanti altına alıyoruz (yoksa UPDATE sessizce 0 satır etkiler).
     */
    suspend fun updateAvatar(bytes: ByteArray) = withContext(Dispatchers.Default) {
        database.transaction {
            database.userProfileQueries.ensureProfileRow()
            database.userProfileQueries.updateAvatar(bytes)
        }
    }
}
