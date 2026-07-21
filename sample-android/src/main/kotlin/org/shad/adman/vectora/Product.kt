package org.shad.adman.vectora

/**
 * Represent a real-world product for the sample.
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val color: String,
    val brand: String = "Nike"
)
