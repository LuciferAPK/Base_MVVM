package com.example.basemvvm.config

fun String.beSureSecure() = this.replace("http://", "https://", true)