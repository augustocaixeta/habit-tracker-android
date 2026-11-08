package br.edu.iftm.habittracker.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.habittracker.HabitTrackerApp
import br.edu.iftm.habittracker.data.AuthError
import br.edu.iftm.habittracker.data.AuthException
import br.edu.iftm.habittracker.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthFormError {
    NAME_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    PASSWORD_SHORT,
    PASSWORD_MISMATCH,
    RESET_EMAIL_REQUIRED,
    INVALID_CREDENTIALS,
    USER_DISABLED,
    EMAIL_IN_USE,
    TOO_MANY_REQUESTS,
    NETWORK,
    UNKNOWN
}

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: AuthFormError? = null,
    val resetSentTo: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isLoggedIn = auth.currentUser != null))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    /**
     * Atualiza o nome digitado no registro.
     *
     * @param value texto do campo.
     */
    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, error = null) }
    }

    /**
     * Atualiza o e-mail digitado.
     *
     * @param value texto do campo.
     */
    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    /**
     * Atualiza a senha digitada.
     *
     * @param value texto do campo.
     */
    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    /**
     * Atualiza a confirmação de senha do registro.
     *
     * @param value texto do campo.
     */
    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value, error = null) }
    }

    /**
     * Limpa as mensagens e as senhas ao trocar entre Login e Registro.
     */
    fun resetMessages() {
        _state.update { it.copy(error = null, resetSentTo = null, password = "", confirmPassword = "") }
    }

    /**
     * Valida os campos e entra com e-mail e senha.
     */
    fun login() {
        val current = _state.value
        val error = when {
            !isValidEmail(current.email) -> AuthFormError.EMAIL_INVALID
            current.password.isBlank() -> AuthFormError.PASSWORD_REQUIRED
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        authenticate { auth.login(current.email, current.password) }
    }

    /**
     * Valida os campos e cria a conta, que já fica autenticada.
     */
    fun register() {
        val current = _state.value
        val error = when {
            current.name.isBlank() -> AuthFormError.NAME_REQUIRED
            !isValidEmail(current.email) -> AuthFormError.EMAIL_INVALID
            current.password.length < MIN_PASSWORD_LENGTH -> AuthFormError.PASSWORD_SHORT
            current.password != current.confirmPassword -> AuthFormError.PASSWORD_MISMATCH
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        authenticate { auth.register(current.name, current.email, current.password) }
    }

    /**
     * Envia o e-mail de redefinição de senha para o endereço digitado no Login.
     */
    fun forgotPassword() {
        val email = _state.value.email
        if (!isValidEmail(email)) {
            _state.update { it.copy(error = AuthFormError.RESET_EMAIL_REQUIRED) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, resetSentTo = null) }
            auth.sendPasswordReset(email)
                .onSuccess { _state.update { it.copy(isLoading = false, resetSentTo = email.trim()) } }
                .onFailure { failure -> _state.update { it.copy(isLoading = false, error = toFormError(failure)) } }
        }
    }

    /**
     * Encerra a sessão e limpa o formulário.
     */
    fun logout() {
        auth.logout()
        _state.value = AuthUiState()
    }

    /**
     * Executa o login ou o registro, controlando o carregamento e o resultado.
     *
     * @param action chamada ao repositório de autenticação.
     */
    private fun authenticate(action: suspend () -> Result<*>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, resetSentTo = null) }
            action()
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isLoggedIn = true, password = "", confirmPassword = "") }
                }
                .onFailure { failure ->
                    _state.update { it.copy(isLoading = false, error = toFormError(failure)) }
                }
        }
    }

    /**
     * Converte a falha do repositório no erro exibido no formulário.
     *
     * @param failure exceção recebida do repositório.
     * @return erro do formulário.
     */
    private fun toFormError(failure: Throwable): AuthFormError {
        return when ((failure as? AuthException)?.error) {
            AuthError.INVALID_EMAIL -> AuthFormError.EMAIL_INVALID
            AuthError.INVALID_CREDENTIALS -> AuthFormError.INVALID_CREDENTIALS
            AuthError.USER_DISABLED -> AuthFormError.USER_DISABLED
            AuthError.EMAIL_IN_USE -> AuthFormError.EMAIL_IN_USE
            AuthError.WEAK_PASSWORD -> AuthFormError.PASSWORD_SHORT
            AuthError.TOO_MANY_REQUESTS -> AuthFormError.TOO_MANY_REQUESTS
            AuthError.NETWORK -> AuthFormError.NETWORK
            AuthError.UNKNOWN, null -> AuthFormError.UNKNOWN
        }
    }

    /**
     * Verifica o formato do e-mail.
     *
     * @param email texto digitado.
     * @return true quando o e-mail tem um formato válido.
     */
    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitTrackerApp
                AuthViewModel(app.auth)
            }
        }
    }
}
