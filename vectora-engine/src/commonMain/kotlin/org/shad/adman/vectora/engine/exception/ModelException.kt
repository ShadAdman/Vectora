package org.shad.adman.vectora.engine.exception

/**
 * Base exception for model-related errors.
 */
open class ModelException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when the model fails to load.
 */
class ModelLoadException(message: String, cause: Throwable? = null) : ModelException(message, cause)

/**
 * Thrown when inference fails.
 */
class InferenceException(message: String, cause: Throwable? = null) : ModelException(message, cause)

/**
 * Thrown when preprocessing fails.
 */
class PreprocessingException(message: String, cause: Throwable? = null) : ModelException(message, cause)

/**
 * Thrown when postprocessing fails.
 */
class PostprocessingException(message: String, cause: Throwable? = null) : ModelException(message, cause)
