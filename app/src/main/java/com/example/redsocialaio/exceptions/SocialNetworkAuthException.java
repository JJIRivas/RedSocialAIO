package com.example.redsocialaio.exceptions;

/**
 * Excepción personalizada para errores de autenticación en redes sociales
 * Se usa para manejar tokens inválidos, expirados, o errores de OAuth
 */
public class SocialNetworkAuthException extends Exception {

    public enum AuthErrorType {
        TOKEN_EXPIRED("El token de acceso ha expirado"),
        TOKEN_INVALID("El token de acceso es inválido"),
        TOKEN_REVOKED("El token de acceso ha sido revocado"),
        OAUTH_FAILED("Falló la autenticación OAuth"),
        INSTANCE_UNREACHABLE("No se puede conectar con la instancia"),
        INVALID_CREDENTIALS("Credenciales inválidas"),
        ACCOUNT_SUSPENDED("La cuenta ha sido suspendida"),
        RATE_LIMITED("Límite de requests excedido"),
        NETWORK_NOT_SUPPORTED("Red social no soportada");

        private final String message;

        AuthErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final AuthErrorType errorType;
    private final String platform; // "misskey" o "mastodon"
    private final String instanceUrl;
    private final int errorCode;

    // Constructor básico
    public SocialNetworkAuthException(AuthErrorType errorType, String platform) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.platform = platform;
        this.instanceUrl = null;
        this.errorCode = -1;
    }

    // Constructor con mensaje personalizado
    public SocialNetworkAuthException(AuthErrorType errorType, String platform, String customMessage) {
        super(customMessage);
        this.errorType = errorType;
        this.platform = platform;
        this.instanceUrl = null;
        this.errorCode = -1;
    }

    // Constructor completo
    public SocialNetworkAuthException(AuthErrorType errorType, String platform,
                                      String instanceUrl, int errorCode, String customMessage) {
        super(customMessage);
        this.errorType = errorType;
        this.platform = platform;
        this.instanceUrl = instanceUrl;
        this.errorCode = errorCode;
    }

    // Constructor con causa
    public SocialNetworkAuthException(AuthErrorType errorType, String platform,
                                      String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorType = errorType;
        this.platform = platform;
        this.instanceUrl = null;
        this.errorCode = -1;
    }

    // Getters
    public AuthErrorType getErrorType() {
        return errorType;
    }

    public String getPlatform() {
        return platform;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public int getErrorCode() {
        return errorCode;
    }

    // Método para verificar si el error es recuperable
    public boolean isRecoverable() {
        return errorType == AuthErrorType.NETWORK_NOT_SUPPORTED ||
                errorType == AuthErrorType.INSTANCE_UNREACHABLE ||
                errorType == AuthErrorType.RATE_LIMITED;
    }

    // Método para verificar si necesita reautenticación
    public boolean needsReauth() {
        return errorType == AuthErrorType.TOKEN_EXPIRED ||
                errorType == AuthErrorType.TOKEN_INVALID ||
                errorType == AuthErrorType.TOKEN_REVOKED;
    }

    // Método para obtener mensaje formateado para mostrar al usuario
    public String getUserFriendlyMessage() {
        String platformName = "misskey".equals(platform) ? "Misskey" : "Mastodon";

        switch (errorType) {
            case TOKEN_EXPIRED:
                return "Tu sesión en " + platformName + " ha expirado. Por favor, inicia sesión nuevamente.";
            case TOKEN_INVALID:
            case TOKEN_REVOKED:
                return "Hubo un problema con tu sesión de " + platformName + ". Por favor, inicia sesión nuevamente.";
            case OAUTH_FAILED:
                return "No se pudo completar el inicio de sesión en " + platformName + ". Inténtalo de nuevo.";
            case INSTANCE_UNREACHABLE:
                return "No se puede conectar con el servidor de " + platformName + ". Verifica tu conexión.";
            case INVALID_CREDENTIALS:
                return "Las credenciales proporcionadas no son válidas.";
            case ACCOUNT_SUSPENDED:
                return "Tu cuenta en " + platformName + " ha sido suspendida.";
            case RATE_LIMITED:
                return "Has excedido el límite de requests. Por favor, espera un momento.";
            case NETWORK_NOT_SUPPORTED:
                return "Esta red social no es compatible con la aplicación.";
            default:
                return "Error de autenticación en " + platformName + ": " + getMessage();
        }
    }

    @Override
    public String toString() {
        return "SocialNetworkAuthException{" +
                "errorType=" + errorType +
                ", platform='" + platform + '\'' +
                ", instanceUrl='" + instanceUrl + '\'' +
                ", errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
