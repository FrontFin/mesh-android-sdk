package com.getfront.catalog.ui

import android.content.Context
import android.content.Intent
import com.getfront.catalog.utils.getParcelableExtraCompat
import com.getfront.catalog.utils.intent

fun getFrontCatalogIntent(activity: Context, catalogLink: String): Intent {
    return intent<FrontCatalogActivity>(activity).putExtra(FrontCatalogActivity.LINK, catalogLink)
}

fun getFrontCatalogResult(data: Intent?): FrontCatalogResult {
    val result = data?.getParcelableExtraCompat<FrontCatalogResult>(FrontCatalogActivity.DATA)
    return result ?: FrontCatalogResult.Cancelled()
}
