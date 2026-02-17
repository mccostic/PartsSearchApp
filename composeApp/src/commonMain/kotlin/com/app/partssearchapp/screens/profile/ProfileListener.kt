package com.app.partssearchapp.screens.profile

interface ProfileListener {
  fun onNameUpdated(name: String)
  fun onLogout()
  fun onError(message: String)
}