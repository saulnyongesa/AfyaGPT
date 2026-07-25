package com.example.afyagpt.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.data.repository.AuthResult
import com.example.afyagpt.domain.model.Profession
import com.example.afyagpt.util.ValidationResult
import com.example.afyagpt.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * AuthViewModel.kt — Authentication Logic
 *
 * Manages the state and business logic for Sign Up, Login, and PIN reset screens.
 */

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profession: String = "",
    val professionalNumber: String = "",
    val facilityName: String = "",
    val county: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val showPin: Boolean = false,
    val showConfirmPin: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val professionError: String? = null,
    val facilityError: String? = null,
    val countyError: String? = null,
    val pinError: String? = null,
    val confirmPinError: String? = null
)

data class LoginUiState(
    val identifier: String = "", // Phone or Email
    val pin: String = "",
    val showPin: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val failedAttempts: Int = 0,
    val isLockedOut: Boolean = false,
    val lockoutSecondsRemaining: Int = 0
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val facilityRepository: com.example.afyagpt.data.repository.FacilityRepository
) : ViewModel() {

    val cachedFacilities: StateFlow<List<com.example.afyagpt.data.local.entity.FacilityEntity>> =
        facilityRepository.getFacilities().stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            facilityRepository.fetchAndCacheFacilities()
        }
    }

    // Lists for dropdowns
    val kenyaCounties = listOf(
        "Baringo", "Bomet", "Bungoma", "Busia", "Elgeyo Marakwet", "Embu", "Garissa", "Homa Bay",
        "Isiolo", "Kajiado", "Kakamega", "Kericho", "Kiambu", "Kilifi", "Kirinyaga", "Kisii",
        "Kisumu", "Kitui", "Kwale", "Laikipia", "Lamu", "Machakos", "Makueni", "Mandera", "Marsabit",
        "Meru", "Migori", "Mombasa", "Murang'a", "Nairobi", "Nakuru", "Nandi", "Narok", "Nyamira",
        "Nyandarua", "Nyeri", "Samburu", "Siaya", "Taita Taveta", "Tana River", "Tharaka Nithi",
        "Trans Nzoia", "Turkana", "Uasin Gishu", "Vihiga", "Wajir", "West Pokot"
    )

    val professions = Profession.values().map { it.displayName }

    // --- Sign Up State ---
    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    fun onSignUpFullNameChange(value: String) = _signUpState.update { it.copy(fullName = value, fullNameError = null) }
    fun onSignUpEmailChange(value: String) = _signUpState.update { it.copy(email = value, emailError = null) }
    fun onSignUpPhoneChange(value: String) = _signUpState.update { it.copy(phone = value, phoneError = null) }
    fun onSignUpProfessionChange(value: String) = _signUpState.update { it.copy(profession = value, professionError = null) }
    fun onSignUpProfessionalNumberChange(value: String) = _signUpState.update { it.copy(professionalNumber = value) }
    fun onSignUpFacilityChange(value: String) = _signUpState.update { it.copy(facilityName = value, facilityError = null) }
    fun onSignUpCountyChange(value: String) = _signUpState.update { it.copy(county = value, countyError = null) }

    fun selectFacility(facilityName: String, county: String) {
        _signUpState.update {
            it.copy(
                facilityName = facilityName,
                county = county,
                facilityError = null,
                countyError = null
            )
        }
    }
    
    fun onSignUpPinChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _signUpState.update { it.copy(pin = value, pinError = null) }
        }
    }
    
    fun onSignUpConfirmPinChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _signUpState.update { it.copy(confirmPin = value, confirmPinError = null) }
        }
    }
    
    fun toggleShowPin() = _signUpState.update { it.copy(showPin = !it.showPin) }
    fun toggleShowConfirmPin() = _signUpState.update { it.copy(showConfirmPin = !it.showConfirmPin) }
    
    fun clearSignUpError() = _signUpState.update { it.copy(error = null) }

    fun submitSignUp() {
        val state = _signUpState.value
        
        // Validation
        var isValid = true
        val fullNameRes = ValidationUtils.validateFullName(state.fullName)
        if (fullNameRes is ValidationResult.Invalid) {
            _signUpState.update { it.copy(fullNameError = fullNameRes.message) }
            isValid = false
        }
        
        val emailRes = ValidationUtils.validateEmail(state.email)
        if (emailRes is ValidationResult.Invalid) {
            _signUpState.update { it.copy(emailError = emailRes.message) }
            isValid = false
        }
        
        val phoneRes = ValidationUtils.validatePhoneNumber(state.phone)
        if (phoneRes is ValidationResult.Invalid) {
            _signUpState.update { it.copy(phoneError = phoneRes.message) }
            isValid = false
        }
        
        if (state.profession.isBlank()) {
            _signUpState.update { it.copy(professionError = "Profession is required") }
            isValid = false
        }
        
        if (state.facilityName.isBlank()) {
            _signUpState.update { it.copy(facilityError = "Facility name is required") }
            isValid = false
        }
        
        if (state.county.isBlank()) {
            _signUpState.update { it.copy(countyError = "County is required") }
            isValid = false
        }
        
        val pinRes = ValidationUtils.validatePin(state.pin)
        if (pinRes is ValidationResult.Invalid) {
            _signUpState.update { it.copy(pinError = pinRes.message) }
            isValid = false
        }
        
        if (!ValidationUtils.pinsMatch(state.pin, state.confirmPin)) {
            _signUpState.update { it.copy(confirmPinError = "PINs do not match") }
            isValid = false
        }
        
        if (!isValid) return

        // Register
        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true, error = null) }
            
            val formattedPhone = ValidationUtils.formatPhoneToE164(state.phone)
            val result = authRepository.registerUser(
                fullName = state.fullName,
                email = state.email,
                phone = formattedPhone,
                profession = state.profession,
                professionalNumber = state.professionalNumber,
                facilityName = state.facilityName,
                county = state.county,
                pin = state.pin
            )
            
            when (result) {
                is AuthResult.Success -> {
                    _signUpState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AuthResult.Error -> {
                    _signUpState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    // --- Login State ---
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginIdentifierChange(value: String) = _loginState.update { it.copy(identifier = value, error = null) }
    
    fun onLoginPinChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _loginState.update { it.copy(pin = value, error = null) }
        }
    }
    
    fun toggleLoginShowPin() = _loginState.update { it.copy(showPin = !it.showPin) }
    fun clearLoginError() = _loginState.update { it.copy(error = null) }

    fun submitLogin() {
        val state = _loginState.value
        
        if (state.isLockedOut) return
        
        if (state.identifier.isBlank() || state.pin.length != 6) {
            _loginState.update { it.copy(error = "Please enter your phone/email and 6-digit PIN") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, statusMessage = "Checking account credentials locally & online...", error = null) }
            
            // Format phone if it looks like a Kenyan number, otherwise use as is (might be email)
            val identifier = if (state.identifier.startsWith("07") || state.identifier.startsWith("01")) {
                ValidationUtils.formatPhoneToE164(state.identifier)
            } else {
                state.identifier
            }

            val result = authRepository.loginUser(identifier, state.pin)
            
            when (result) {
                is AuthResult.Success -> {
                    _loginState.update { it.copy(isLoading = false, statusMessage = null, isSuccess = true, failedAttempts = 0) }
                }
                is AuthResult.Error -> {
                    val attempts = state.failedAttempts + 1
                    if (attempts >= 5) {
                        handleLockout()
                    } else {
                        _loginState.update { 
                            it.copy(
                                isLoading = false, 
                                statusMessage = null,
                                error = result.message,
                                failedAttempts = attempts
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    private fun handleLockout() {
        viewModelScope.launch {
            _loginState.update { 
                it.copy(
                    isLoading = false, 
                    isLockedOut = true, 
                    lockoutSecondsRemaining = 30,
                    error = "Too many attempts. Account temporarily locked."
                ) 
            }
            
            for (i in 30 downTo 1) {
                delay(1000)
                _loginState.update { it.copy(lockoutSecondsRemaining = i - 1) }
            }
            
            _loginState.update { 
                it.copy(isLockedOut = false, failedAttempts = 0, error = null) 
            }
        }
    }
}
