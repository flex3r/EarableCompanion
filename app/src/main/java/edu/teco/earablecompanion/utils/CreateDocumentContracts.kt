package edu.teco.earablecompanion.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class CreateCsvDocumentContract : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent = super.createIntent(context, input).setType("application/csv")
}

class CreateZipDocumentContract : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent = super.createIntent(context, input).setType("application/zip")
}