package com.example.data.model

enum class TransactionCategory(
    val idName: String,
    val displayName: String,
    val emoji: String,
    val keywords: List<String>
) {
    FOOD(
        idName = "food",
        displayName = "Food & Groceries",
        emoji = "🍲",
        keywords = listOf(
            "bukka", "buka", "amala", "jollof", "chicken republic", "shoprite",
            "market", "chowdeck", "restaurant", "supermarket", "eatery",
            "sweet sensation", "domino", "kilimanjaro", "groceries", "food",
            "kitchen", "mama cass", "mr biggs", "genesis", "the place", "tantalizers"
        )
    ),
    TRANSPORT(
        idName = "transport",
        displayName = "Transport & Fuel",
        emoji = "🚗",
        keywords = listOf(
            "uber", "bolt", "indrive", "keke", "danfo", "brt", "fuel", "total",
            "nnpc", "filling station", "toll", "cowry", "conoil", "ardova",
            "oando", "mobil", "transport", "bus", "cab", "ferry"
        )
    ),
    DATA_AIRTIME(
        idName = "data_airtime",
        displayName = "Data & Airtime",
        emoji = "📱",
        keywords = listOf(
            "mtn", "airtel", "glo", "9mobile", "recharge", "vtu", "airtime",
            "data bundle", "spectranet", "smile", "ipnx", "bundle", "swift"
        )
    ),
    RENT(
        idName = "rent",
        displayName = "Rent & Bills",
        emoji = "🏠",
        keywords = listOf(
            "rent", "landlord", "estate dues", "service charge", "lawma",
            "facility management", "waste", "tenement", "phcn", "ikedc", "ekedc", "nepa"
        )
    ),
    BUSINESS(
        idName = "business",
        displayName = "Business & Work",
        emoji = "💼",
        keywords = listOf(
            "inventory", "supplier", "pos settlement", "invoice", "logistics",
            "freelance", "customer", "dispatch", "settlement", "goods", "wholesaler"
        )
    ),
    OTHER(
        idName = "other",
        displayName = "Other",
        emoji = "✨",
        keywords = emptyList()
    );

    companion object {
        fun matchFromNarration(narration: String): TransactionCategory {
            val lower = narration.lowercase()
            for (category in entries) {
                if (category == OTHER) continue
                for (keyword in category.keywords) {
                    if (lower.contains(keyword)) {
                        return category
                    }
                }
            }
            return OTHER
        }

        fun fromId(id: String): TransactionCategory {
            return entries.find { it.idName.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: OTHER
        }
    }
}
