package com.lazydagger.sample

import com.kshitijpatil.lazydagger.LazyDagger

@LazyDagger(LoginComponent::class, ViewComponent::class)
interface PostPresenterDelegate {
    val postId: Int
    val title: String
}