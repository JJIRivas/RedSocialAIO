package com.example.redsocialaio.exceptions;

/**
 * Excepción personalizada para errores específicos de las APIs de redes sociales
 * Se usa para manejar errores HTTP, respuestas malformadas, o problemas con endpoints
 */
public class SocialNetworkApiException extends Exception {

    public enum ApiErrorType {
        HTTP_ERROR("Error HTTP en la respuesta"),
        ENDPOINT_NOT_FOUND("Endpoint no encontrado"),
        UNSUPPORTED_OPERATION("Operación no soportada"),
        INVALID_PARAMETERS("Parámetros inválidos"),
        SERVER_ERROR("Error interno del servidor"),
        NETWORK_ERROR("Error de red"),
        TIMEOUT("Tiempo de espera agotado"),
        CONTENT_NOT_FOUND("Contenido no encontrado"),
        PERMISSION_DENIED("Permisos insuficientes"),
        QUOTA_EXCEEDED("Cuota excedida"),
        MAINTENANCE_MODE("Servidor en mantenimiento");

        private final String message;

        ApiErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final ApiErrorType errorType;
    private final String platform;
    private final String endpoint;
    private final int httpStatusCode;
    private final String serverResponse;

    // Constructor básico
    public SocialNetworkApiException(ApiErrorType errorType, String platform, String endpoint) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.platform = platform;
        this.endpoint = endpoint;
        this.httpStatusCode = -1;
        this.serverResponse = null;
    }

    // Constructor con código HTTP
    public SocialNetworkApiException(ApiErrorType errorType, String platform,
                                     String endpoint, int httpStatusCode) {
        super(errorType.getMessage() + " (HTTP " + httpStatusCode + ")");
        this.errorType = errorType;
        this.platform = platform;
        this.endpoint = endpoint;
        this.httpStatusCode = httpStatusCode;
        this.serverResponse = null;
    }

    // Constructor completo
    public SocialNetworkApiException(ApiErrorType errorType, String platform,
                                     String endpoint, int httpStatusCode,
                                     String serverResponse, String customMessage) {
        super(customMessage);
        this.errorType = errorType;
        this.platform = platform;
        this.endpoint = endpoint;
        this.httpStatusCode = httpStatusCode;
        this.serverResponse = serverResponse;
    }

    // Constructor con causa
    public SocialNetworkApiException(ApiErrorType errorType, String platform,
                                     String endpoint, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
        this.platform = platform;
        this.endpoint = endpoint;
        this.httpStatusCode = -1;
        this.serverResponse = null;
    }

    // Factory method para crear desde código HTTP
    public static SocialNetworkApiException fromHttpCode(int httpCode, String platform,
                                                         String endpoint, String response) {
        ApiErrorType errorType;

        switch (httpCode) {
            case 400:
                errorType = ApiErrorType.INVALID_PARAMETERS;
                break;
            case 401:
                errorType = ApiErrorType.PERMISSION_DENIED;
                break;
            case 404:
                errorType = ApiErrorType.ENDPOINT_NOT_FOUND;
                break;
            case 429:
                errorType = ApiErrorType.QUOTA_EXCEEDED;
                break;
            case 503:
                errorType = ApiErrorType.SERVER_ERROR;
                break;
            default:
                errorType = ApiErrorType.HTTP_ERROR;
        }

        return new SocialNetworkApiException(errorType, platform, endpoint, httpCode,
                response, "Error HTTP " + httpCode + " en " + endpoint);
    }

    // Getters
    public ApiErrorType getErrorType() {
        return errorType;
    }

    public String getPlatform() {
        return platform;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getServerResponse() {
        return serverResponse;
    }

    // Método para verificar si el error es temporal
    public boolean isTemporary() {
        return errorType == ApiErrorType.NETWORK_ERROR ||
                errorType == ApiErrorType.TIMEOUT ||
                errorType == ApiErrorType.SERVER_ERROR ||
                errorType == ApiErrorType.MAINTENANCE_MODE ||
                httpStatusCode == 503;
    }

    // Método para verificar si se puede reintentar
    public boolean isRetryable() {
        return isTemporary() || errorType == ApiErrorType.QUOTA_EXCEEDED;
    }

    // Método para obtener mensaje amigable para el usuario
    public String getUserFriendlyMessage() {
        String platformName = "misskey".equals(platform) ? "Misskey" : "Mastodon";

        switch (errorType) {
            case NETWORK_ERROR:
                return "Error de conexión. Verifica tu conexión a internet.";
            case TIMEOUT:
                return "La operación tardó demasiado tiempo. Inténtalo de nuevo.";
            case SERVER_ERROR:
                return "Error en el servidor de " + platformName + ". Inténtalo más tarde.";
            case MAINTENANCE_MODE:
                return "El servidor de " + platformName + " está en mantenimiento.";
            case QUOTA_EXCEEDED:
                return "Has excedido el límite de requests. Espera un momento.";
            case PERMISSION_DENIED:
                return "No tienes permisos para realizar esta acción.";
            case CONTENT_NOT_FOUND:
                return "El contenido solicitado no existe.";
            case INVALID_PARAMETERS:
                return "Los datos proporcionados no son válidos.";
            case UNSUPPORTED_OPERATION:
                return "Esta operación no está disponible en " + platformName + ".";
            default:
                return "Error al comunicarse con " + platformName + ". Inténtalo de nuevo.";
        }
    }

    @Override
    public String toString() {
        return "SocialNetworkApiException{" +
                "errorType=" + errorType +
                ", platform='" + platform + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
