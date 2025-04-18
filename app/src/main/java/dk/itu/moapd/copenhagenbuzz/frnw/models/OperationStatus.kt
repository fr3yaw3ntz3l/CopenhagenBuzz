package dk.itu.moapd.copenhagenbuzz.frnw.models

/**
 * Represents the status of an operation in the app.
 * Used to communicate success or failure of database operations to the UI.
 */
sealed class OperationStatus {
    /**
     * Represents a successful operation with a message.
     */
    data class Success(val message: String) : OperationStatus()

    /**
     * Represents a failed operation with an error message.
     */
    data class Error(val message: String) : OperationStatus()
}