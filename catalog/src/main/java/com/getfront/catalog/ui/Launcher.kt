package com.getfront.catalog.ui

import android.content.Context
import com.getfront.catalog.utils.intent

fun launchCatalog(activity: Context, catalogLink: String) {
    activity.startActivity(
        intent<FrontCatalogActivity>(activity)
            .putExtra(FrontCatalogActivity.LINK, catalogLink)
    )
}
