package org.shad.adman.vectora.core.embedding

/**
 * Marker for engines whose vectors are always L2-normalized. Search can then
 * score with a plain dot product instead of full cosine similarity.
 */
interface NormalizedEmbeddingEngine : EmbeddingEngine
