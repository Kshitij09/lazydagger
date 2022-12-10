package com.lazydagger.sample

import com.kshitijpatil.lazydagger.LazyDagger
import dagger.hilt.components.SingletonComponent

@LazyDagger(SingletonComponent::class)
interface PostPresenterDelegate {
    val postId: Int
    val title: String
}