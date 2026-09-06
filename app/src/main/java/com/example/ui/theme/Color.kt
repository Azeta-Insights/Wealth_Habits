package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Editorial Aesthetic Palette (from Design Specifications)
// Primary - Deep Editorial Olive / Forest Green
val EditorialOlive = Color(0xFF2D4F1E)
val EditorialOliveLight = Color(0xFF456B34)
val EditorialOliveContainer = Color(0xFFD9E4D5)
val OnEditorialOliveContainer = Color(0xFF14290D)

// Secondary / Accent - Editorial Terracotta / Rust
val EditorialRust = Color(0xFFB35C44)
val EditorialRustDark = Color(0xFF8E3E28)
val EditorialRustContainer = Color(0xFFFCE8E2)
val OnEditorialRustContainer = Color(0xFF3E150B)

// Neutral & Tints
val EditorialSlate = Color(0xFF4A4A4A)
val EditorialNeutralContainer = Color(0xFFE8E8E8)
val EditorialDivider = Color(0xFFF0EBE6)

// Surface, Background & Borders - Warm Editorial Paper
val EditorialCanvas = Color(0xFFFDFCFB)       // Crisp warm paper background
val EditorialCardBg = Color(0xFFF4F1EE)       // Warm stone editorial paper
val EditorialBorder = Color(0xFFE5E0DA)       // Soft warm paper edge border
val EditorialInk = Color(0xFF1C1B17)          // Rich espresso ink text
val EditorialInkSubtle = Color(0xFF6E6961)    // Secondary subtle text
val EditorialSurface = Color(0xFFFFFFFF)

// Badges & Categories Soft Pastels
val BadgeFoodBg = Color(0xFFFDF1E7)
val BadgeDataBg = Color(0xFFE7F3FD)
val BadgeTransportBg = Color(0xFFE7FDE9)
val BadgeRentBg = Color(0xFFF2EAF9)
val BadgeBusinessBg = Color(0xFFFFF7DB)
val BadgeOtherBg = Color(0xFFF0EBE6)

// Dark Theme Variants (Warm Editorial Night)
val EditorialNightCanvas = Color(0xFF141713)
val EditorialNightCard = Color(0xFF1D221C)
val EditorialNightSurface = Color(0xFF232A21)
val EditorialNightBorder = Color(0xFF374235)
val EditorialNightInk = Color(0xFFF4F1EE)
val EditorialNightInkSubtle = Color(0xFFA5AEA2)
val EditorialNightOlive = Color(0xFF8CC978)
val EditorialNightRust = Color(0xFFE58F78)

// Backward compatibility alias bindings
val ForestGreenPrimary = EditorialOlive
val ForestGreenLight = EditorialOliveLight
val ForestGreenContainer = EditorialOliveContainer
val OnForestGreenContainer = OnEditorialOliveContainer

val WarmClaySecondary = EditorialRust
val WarmClayDark = EditorialRustDark
val WarmClayContainer = EditorialRustContainer
val OnWarmClayContainer = OnEditorialRustContainer

val SoftSageTertiary = Color(0xFF5B7A54)
val SoftSageContainer = EditorialOliveContainer
val OnSoftSageContainer = OnEditorialOliveContainer
val WarmSand = Color(0xFFD8A47F)

val WarmLinenBackground = EditorialCanvas
val WarmSurface = EditorialSurface
val WarmSurfaceVariant = EditorialCardBg
val WarmOutline = EditorialBorder
val WarmOnSurface = EditorialInk
val WarmOnSurfaceSubtle = EditorialInkSubtle

val ForestGreenDark = EditorialNightOlive
val WarmClayLight = EditorialNightRust
val SoftSageLight = Color(0xFFA4C49F)
val DarkSurfaceBackground = EditorialNightCanvas
val DarkSurface = EditorialNightSurface
val DarkSurfaceVariant = EditorialNightCard
val DarkOnSurface = EditorialNightInk
val DarkOnSurfaceSubtle = EditorialNightInkSubtle

val Purple80 = ForestGreenDark
val PurpleGrey80 = SoftSageLight
val Pink80 = WarmClayLight
val Purple40 = ForestGreenPrimary
val PurpleGrey40 = SoftSageTertiary
val Pink40 = WarmClaySecondary
