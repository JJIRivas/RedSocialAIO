package com.example.redsocialaio.exceptions;

/**
 * Excepción personalizada para errores relacionados con el procesamiento de posts unificados
 * Se usa para manejar errores de parsing, conversión, o problemas con el contenido
 */
public class UnifiedPostException extends Exception {

    public enum PostErrorType {
        PARSING_ERROR("Error al parsear el contenido del post"),
        INVALID_JSON("JSON inválido o malformado"),
        MISSING_REQUIRED_FIELDS("Faltan campos obligatorios"),
        UNSUPPORTED_CONTENT_TYPE("Tipo de contenido no soportado"),
        INVALID_TIMESTAMP("Timestamp inválido"),
        INVALID_USER_DATA("Datos de usuario inválidos"),
        MEDIA_PROCESSING_ERROR("Error al procesar archivos multimedia"),
        EMOJI_PROCESSING_ERROR("Error al procesar emojis personalizados"),
        CONTENT_TOO_LONG("Contenido demasiado largo"),
        INVALID_PLATFORM("Plataforma no reconocida"),
        CONVERSION_ERROR("Error al convertir entre formatos"),
        MERGE_ERROR("Error al fusionar posts de diferentes plataformas");

        private final String message;

        PostErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final PostErrorType errorType;
    private final String platform; // "misskey", "mastodon", o "unified"
    private final String postId;
    private final String fieldName; // Campo específico que causó el error
    private final Object invalidValue; // Valor que causó el error

    // Constructor básico
    public UnifiedPostException(PostErrorType errorType, String platform) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.platform = platform;
        this.postId = null;
        this.fieldName = null;
        this.invalidValue = null;
    }

    // Constructor con post ID
    public UnifiedPostException(PostErrorType errorType, String platform, String postId) {
        super(errorType.getMessage() + " (Post ID: " + postId + ")");
        this.errorType = errorType;
        this.platform = platform;
        this.postId = postId;
        this.fieldName = null;
        this.invalidValue = null;
    }

    // Constructor con campo específico
    public UnifiedPostException(PostErrorType errorType, String platform,
                                String postId, String fieldName, Object invalidValue) {
        super(errorType.getMessage() + " - Campo: " + fieldName);
        this.errorType = errorType;
        this.platform = platform;
        this.postId = postId;
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    // Constructor completo con mensaje personalizado
    public UnifiedPostException(PostErrorType errorType, String platform,
                                String postId, String fieldName, Object invalidValue,
                                String customMessage) {
        super(customMessage);
        this.errorType = errorType;
        this.platform = platform;
        this.postId = postId;
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    // Constructor con causa
    public UnifiedPostException(PostErrorType errorType, String platform,
                                String postId, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
        this.platform = platform;
        this.postId = postId;
        this.fieldName = null;
        this.invalidValue = null;
    }

    // Factory method para errores de parsing JSON
    public static UnifiedPostException fromJsonError(String platform, String postId,
                                                     String jsonString, Throwable cause) {
        return new UnifiedPostException(PostErrorType.INVALID_JSON, platform, postId,
                "JSON parsing failed for: " +
                        (jsonString.length() > 100 ? jsonString.substring(0, 100) + "..." : jsonString),
                cause);
    }

    // Factory method para campos faltantes
    public static UnifiedPostException missingField(String platform, String postId,
                                                    String fieldName) {
        return new UnifiedPostException(PostErrorType.MISSING_REQUIRED_FIELDS, platform,
                postId, fieldName, null, "Campo requerido faltante: " + fieldName);
    }

    // Factory method para valores inválidos
    public static UnifiedPostException invalidValue(String platform, String postId,
                                                    String fieldName, Object invalidValue) {
        return new UnifiedPostException(PostErrorType.PARSING_ERROR, platform,
                postId, fieldName, invalidValue,
                "Valor inválido para " + fieldName + ": " + invalidValue);
    }

    // Getters
    public PostErrorType getErrorType() {
        return errorType;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPostId() {
        return postId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    // Método para verificar si el error es de parsing
    public boolean isParsingError() {
        return errorType == PostErrorType.PARSING_ERROR ||
                errorType == PostErrorType.INVALID_JSON ||
                errorType == PostErrorType.MISSING_REQUIRED_FIELDS;
    }

    // Método para verificar si el error es recuperable
    public boolean isRecoverable() {
        return errorType == PostErrorType.MEDIA_PROCESSING_ERROR ||
                errorType == PostErrorType.EMOJI_PROCESSING_ERROR ||
                errorType == PostErrorType.CONVERSION_ERROR;
    }

    // Método para obtener mensaje amigable para el usuario
    public String getUserFriendlyMessage() {
        String platformName = getPlatformDisplayName();

        switch (errorType) {
            case PARSING_ERROR:
            case INVALID_JSON:
                return "Error al procesar el contenido del post de " + platformName + ".";
            case MISSING_REQUIRED_FIELDS:
                return "El post de " + platformName + " tiene información incompleta.";
            case UNSUPPORTED_CONTENT_TYPE:
                return "Este tipo de contenido no es compatible.";
            case INVALID_TIMESTAMP:
                return "Fecha del post inválida.";
            case INVALID_USER_DATA:
                return "Información de usuario inválida.";
            case MEDIA_PROCESSING_ERROR:
                return "Error al cargar archivos multimedia.";
            case EMOJI_PROCESSING_ERROR:
                return "Error al cargar emojis personalizados.";
            case CONTENT_TOO_LONG:
                return "El contenido es demasiado largo para procesar.";
            case INVALID_PLATFORM:
                return "Plataforma no reconocida.";
            case CONVERSION_ERROR:
                return "Error al convertir el formato del post.";
            case MERGE_ERROR:
                return "Error al combinar posts de diferentes plataformas.";
            default:
                return "Error al procesar el post de " + platformName + ".";
        }
    }

    private String getPlatformDisplayName() {
        switch (platform) {
            case "misskey":
                return "Misskey";
            case "mastodon":
                return "Mastodon";
            case "unified":
                return "timeline unificado";
            default:
                return platform;
        }
    }

    @Override
    public String toString() {
        return "UnifiedPostException{" +
                "errorType=" + errorType +
                ", platform='" + platform + '\'' +
                ", postId='" + postId + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", invalidValue=" + invalidValue +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
