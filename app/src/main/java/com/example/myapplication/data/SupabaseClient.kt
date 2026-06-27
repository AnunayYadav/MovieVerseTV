package com.example.myapplication.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    private const val SUPABASE_URL = "https://ieclcfngpqxknggeurpo.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImllY2xjZm5ncHF4a25nZ2V1cnBvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUzNDMxMDgsImV4cCI6MjA4MDkxOTEwOH0.FNg_ToqN2ZvkmYhBoOYJhIxmcYEVOY0yvSPZcICivGs"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
