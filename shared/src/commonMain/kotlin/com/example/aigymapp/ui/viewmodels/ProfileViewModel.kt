package com.example.aigymapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.aigymapp.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ProfileViewModel - Profil ekranının iş mantığını yöneten ViewModel
 * 
 * Repository'den veri çeker ve UI state'ini günceller
 * Kullanıcı profil düzenleme işlemlerini yönetir
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val coroutineScope: CoroutineScope
) {

    /**
     * Profil ekranının UI state'i
     */
    data class ProfileState(
        val name: String = "Misafir",
        val height: String = "-",
        val weight: String = "-",
        val fat: String = "-",
        val age: String = "-",
        val gender: String = "-",
        val isEditing: Boolean = false,
        val avatarBytes: ByteArray? = null
    )

    /**
     * Mevcut UI state
     */
    var state by mutableStateOf(ProfileState())
        private set

    init {
        // Repository'den kullanıcı profilini dinle ve state'i güncelle
        coroutineScope.launch(Dispatchers.Main) {
            repository.userProfile.collectLatest { profile ->
                if (profile != null) {
                    state = state.copy(
                        name = profile.name,
                        height = profile.height?.let { "${it.toInt()} cm" } ?: "-",
                        weight = profile.weight?.let { "${it.toInt()} kg" } ?: "-",
                        fat = profile.fatRatio?.let { "${it.toInt()}%" } ?: "-",
                        age = profile.age?.toString() ?: "-",
                        gender = profile.gender ?: "-",
                        avatarBytes = profile.avatar
                    )
                }
            }
        }
    }

    /**
     * Düzenleme modunu aç/kapat
     */
    fun toggleEditMode() {
        state = state.copy(isEditing = !state.isEditing)
    }

    /**
     * Kullanıcı profil bilgilerini kaydet
     * String inputları parse ederek Double/Long'a çevirir
     * NumberFormatException'ı güvenli şekilde handle eder
     * 
     * @param name Kullanıcı adı
     * @param heightStr Boy (string)
     * @param weightStr Kilo (string)
     * @param fatStr Vücut yağ oranı (string)
     * @param ageStr Yaş (string)
     * @param gender Cinsiyet
     */
    fun saveProfile(
        name: String,
        heightStr: String,
        weightStr: String,
        fatStr: String,
        ageStr: String,
        gender: String
    ) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                // Parse string değerleri sayılara çevir (boş ise null)
                val height = heightStr.trim().toDoubleOrNull()
                val weight = weightStr.trim().toDoubleOrNull()
                val fat = fatStr.trim().toDoubleOrNull()
                val age = ageStr.trim().toLongOrNull()

                // Repository'ye kaydet — avatar mevcut state'ten aynen geçirilir,
                // yoksa INSERT OR REPLACE bir önceki fotoğrafı siler.
                repository.saveProfile(
                    name = name.trim().ifEmpty { "Misafir" },
                    height = height,
                    weight = weight,
                    fat = fat,
                    age = age,
                    gender = gender,
                    avatar = state.avatarBytes
                )

                // Düzenleme modunu kapat
                coroutineScope.launch(Dispatchers.Main) {
                    state = state.copy(isEditing = false)
                }
            } catch (e: Exception) {
                // Hata durumunda log basabiliriz (şimdilik sessiz handle ediyoruz)
                println("ProfileViewModel: Profil kaydedilemedi - ${e.message}")
            }
        }
    }

    /**
     * Yeni profil fotoğrafını kaydeder (galeri veya kamera).
     */
    fun updateAvatar(bytes: ByteArray) {
        coroutineScope.launch(Dispatchers.Default) {
            repository.updateAvatar(bytes)
        }
    }

    /**
     * Düzenleme için mevcut değerleri döndürür (parse edilmiş)
     */
    fun getEditableValues(): EditableProfile {
        return EditableProfile(
            name = state.name,
            height = state.height.replace(" cm", "").replace("-", ""),
            weight = state.weight.replace(" kg", "").replace("-", ""),
            fat = state.fat.replace("%", "").replace("-", ""),
            age = state.age.replace("-", ""),
            gender = state.gender.replace("-", "Erkek")
        )
    }

    /**
     * Düzenlenebilir profil değerleri
     */
    data class EditableProfile(
        val name: String,
        val height: String,
        val weight: String,
        val fat: String,
        val age: String,
        val gender: String
    )
}
