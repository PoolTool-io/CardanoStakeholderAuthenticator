package com.pegasus.csas.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object AuthenticatorFirebaseApp {

    val options = FirebaseOptions.Builder()
        .setDatabaseUrl("https://pegasus-pool.firebaseio.com")
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val app = FirebaseApp.initializeApp(options)

}