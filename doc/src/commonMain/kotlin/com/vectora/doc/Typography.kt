package com.vectora.doc

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import vectora.doc.generated.resources.LatoBold
import vectora.doc.generated.resources.LatoRegular
import vectora.doc.generated.resources.Res

@Composable
fun LatoTypography(): Typography {
    val Lato = FontFamily(
        Font(Res.font.LatoRegular, FontWeight.Normal),
        Font(Res.font.LatoBold, FontWeight.Bold)
    )

    return Typography(
        displayLarge = Typography().displayLarge.copy(fontFamily = Lato),
        displayMedium = Typography().displayMedium.copy(fontFamily = Lato),
        displaySmall = Typography().displaySmall.copy(fontFamily = Lato),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = Lato),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = Lato),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = Lato),
        titleLarge = Typography().titleLarge.copy(fontFamily = Lato),
        titleMedium = Typography().titleMedium.copy(fontFamily = Lato),
        titleSmall = Typography().titleSmall.copy(fontFamily = Lato),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = Lato),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = Lato),
        bodySmall = Typography().bodySmall.copy(fontFamily = Lato),
        labelLarge = Typography().labelLarge.copy(fontFamily = Lato),
        labelMedium = Typography().labelMedium.copy(fontFamily = Lato),
        labelSmall = Typography().labelSmall.copy(fontFamily = Lato)
    )
}
