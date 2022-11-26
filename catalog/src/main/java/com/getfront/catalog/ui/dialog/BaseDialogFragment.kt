package com.getfront.catalog.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

internal abstract class BaseDialogFragment : DialogFragment() {

    protected abstract val layoutRes: Int

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, state: Bundle?): View? =
        inflater.inflate(layoutRes, group, false)
}
