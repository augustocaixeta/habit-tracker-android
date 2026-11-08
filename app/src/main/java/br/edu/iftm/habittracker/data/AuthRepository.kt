package br.edu.iftm.habittracker.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

enum class AuthError {
    INVALID_EMAIL,
    INVALID_CREDENTIALS,
    USER_DISABLED,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    TOO_MANY_REQUESTS,
    NETWORK,
    UNKNOWN
}

class AuthException(val error: AuthError) : Exception(error.name)

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Entra com e-mail e senha.
     *
     * @param email e-mail digitado.
     * @param password senha digitada.
     * @return usuário autenticado, ou falha com [AuthException].
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        checkNotNull(result.user)
    }.mapFirebaseError()

    /**
     * Cria a conta e salva o nome no perfil do Firebase.
     *
     * @param name nome de exibição.
     * @param email e-mail da conta.
     * @param password senha com pelo menos 6 caracteres.
     * @return usuário criado e já autenticado, ou falha com [AuthException].
     */
    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = checkNotNull(result.user)
        user.updateProfile(userProfileChangeRequest { displayName = name.trim() }).await()
        user
    }.mapFirebaseError()

    /**
     * Envia o e-mail de redefinição de senha.
     *
     * @param email e-mail da conta.
     * @return sucesso, ou falha com [AuthException].
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.mapFirebaseError()

    /**
     * Encerra a sessão atual.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Troca as exceções do Firebase por uma [AuthException] com o motivo da falha.
     *
     * @receiver resultado da chamada ao Firebase.
     * @return o mesmo resultado, com a falha convertida.
     */
    private fun <T> Result<T>.mapFirebaseError(): Result<T> = recoverCatching { throwable ->
        throw AuthException(toAuthError(throwable))
    }

    /**
     * Identifica o motivo da falha pelo tipo da exceção e pelo código de erro do Firebase.
     *
     * @param throwable exceção lançada pelo Firebase.
     * @return motivo da falha.
     */
    private fun toAuthError(throwable: Throwable): AuthError {
        if (throwable is FirebaseNetworkException) {
            return AuthError.NETWORK
        }
        if (throwable is FirebaseTooManyRequestsException) {
            return AuthError.TOO_MANY_REQUESTS
        }
        return when ((throwable as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> AuthError.INVALID_EMAIL
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND" -> AuthError.INVALID_CREDENTIALS
            "ERROR_USER_DISABLED" -> AuthError.USER_DISABLED
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EMAIL_IN_USE
            "ERROR_WEAK_PASSWORD" -> AuthError.WEAK_PASSWORD
            "ERROR_TOO_MANY_REQUESTS" -> AuthError.TOO_MANY_REQUESTS
            else -> AuthError.UNKNOWN
        }
    }
}
