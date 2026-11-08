package br.edu.iftm.habittracker.ui.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.iftm.habittracker.R

/**
 * Tela de login com e-mail e senha, recuperação de senha e acesso ao registro.
 *
 * @param viewModel estado da autenticação.
 * @param onGoToRegister abre a tela de registro.
 */
@Composable
fun LoginScreen(viewModel: AuthViewModel, onGoToRegister: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthScaffold(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle)
    ) {
        AuthField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.field_email),
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        PasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.field_password),
            imeAction = ImeAction.Done
        )
        TextButton(onClick = viewModel::forgotPassword, modifier = Modifier.align(Alignment.End)) {
            Text(text = stringResource(R.string.login_forgot_password))
        }
        Messages(error = state.error, resetSentTo = state.resetSentTo)
        PrimaryButton(text = stringResource(R.string.login_action), loading = state.isLoading, onClick = viewModel::login)
        SwitchScreenRow(
            prompt = stringResource(R.string.login_no_account),
            action = stringResource(R.string.login_create_account),
            onClick = {
                viewModel.resetMessages()
                onGoToRegister()
            }
        )
    }
}

/**
 * Tela de registro com nome, e-mail, senha e confirmação de senha.
 *
 * @param viewModel estado da autenticação.
 * @param onBackToLogin volta para a tela de login.
 */
@Composable
fun RegisterScreen(viewModel: AuthViewModel, onBackToLogin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthScaffold(
        title = stringResource(R.string.register_title),
        subtitle = stringResource(R.string.register_subtitle)
    ) {
        AuthField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = stringResource(R.string.field_name),
            icon = Icons.Outlined.Person,
            keyboardType = KeyboardType.Text
        )
        AuthField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.field_email),
            icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        PasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.field_password_new),
            imeAction = ImeAction.Next
        )
        PasswordField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = stringResource(R.string.field_password_confirm),
            imeAction = ImeAction.Done
        )
        Spacer(Modifier.height(4.dp))
        Messages(error = state.error, resetSentTo = state.resetSentTo)
        PrimaryButton(text = stringResource(R.string.register_action), loading = state.isLoading, onClick = viewModel::register)
        SwitchScreenRow(
            prompt = stringResource(R.string.register_has_account),
            action = stringResource(R.string.login_action),
            onClick = {
                viewModel.resetMessages()
                onBackToLogin()
            }
        )
    }
}

/**
 * Estrutura comum das telas de autenticação: logo, título, subtítulo e o formulário com rolagem.
 *
 * @param title título principal.
 * @param subtitle texto de apoio abaixo do título.
 * @param content campos e botões do formulário.
 */
@Composable
private fun AuthScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/**
 * Campo de texto de uma linha com ícone à esquerda.
 *
 * @param value texto atual do campo.
 * @param onValueChange recebe o texto digitado.
 * @param label rótulo do campo.
 * @param icon ícone exibido à esquerda.
 * @param keyboardType tipo de teclado.
 */
@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next)
    )
}

/**
 * Campo de senha com botão para mostrar ou ocultar o texto.
 *
 * @param value texto atual do campo.
 * @param onValueChange recebe o texto digitado.
 * @param label rótulo do campo.
 * @param imeAction ação da tecla de confirmação do teclado.
 */
@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, imeAction: ImeAction) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(if (visible) R.string.cd_hide_password else R.string.cd_show_password)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction)
    )
}

/**
 * Mensagem de erro ou de confirmação do envio do e-mail de recuperação.
 *
 * @param error erro do formulário, ou null.
 * @param resetSentTo e-mail que recebeu o link de recuperação, ou null.
 */
@Composable
private fun Messages(error: AuthFormError?, resetSentTo: String?) {
    if (error != null) {
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
    if (resetSentTo != null) {
        Text(
            text = stringResource(R.string.auth_reset_sent, resetSentTo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Botão principal de largura total, que mostra um indicador de progresso durante o carregamento.
 *
 * @param text texto do botão.
 * @param loading true enquanto a chamada ao Firebase está em andamento.
 * @param onClick ação do botão.
 */
@Composable
private fun PrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = !loading,
        shape = RoundedCornerShape(14.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Linha centralizada com uma pergunta e o link para a outra tela de autenticação.
 *
 * @param prompt pergunta exibida antes do link.
 * @param action texto do link.
 * @param onClick ação do link.
 */
@Composable
private fun SwitchScreenRow(prompt: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = prompt, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onClick) {
            Text(text = action)
        }
    }
}

/**
 * Texto exibido para cada erro do formulário.
 *
 * @receiver erro do formulário.
 * @return id do texto no strings.xml.
 */
@StringRes
private fun AuthFormError.messageRes(): Int {
    return when (this) {
        AuthFormError.NAME_REQUIRED -> R.string.auth_error_name_required
        AuthFormError.EMAIL_INVALID -> R.string.auth_error_email_invalid
        AuthFormError.PASSWORD_REQUIRED -> R.string.auth_error_password_required
        AuthFormError.PASSWORD_SHORT -> R.string.auth_error_password_short
        AuthFormError.PASSWORD_MISMATCH -> R.string.auth_error_password_mismatch
        AuthFormError.RESET_EMAIL_REQUIRED -> R.string.auth_error_reset_email_required
        AuthFormError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthFormError.USER_DISABLED -> R.string.auth_error_user_disabled
        AuthFormError.EMAIL_IN_USE -> R.string.auth_error_email_in_use
        AuthFormError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
        AuthFormError.NETWORK -> R.string.auth_error_network
        AuthFormError.UNKNOWN -> R.string.auth_error_unknown
    }
}
