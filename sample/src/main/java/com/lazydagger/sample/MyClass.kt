package com.lazydagger.sample

import com.kshitijpatil.lazydagger.LazyDagger

@LazyDagger
interface PostPresenterDelegate {
    val postId: Int
    val title: String
}