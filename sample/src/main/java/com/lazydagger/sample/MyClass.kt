package com.lazydagger.sample

import com.kshitijpatil.lazydagger.LazyDagger
import javax.inject.Inject

@LazyDagger
interface PostPresenterDelegate {
    val postId: Int
    val title: String
}

class CommentRepository @Inject constructor()

class LikeUseCase @Inject constructor()


@LazyDagger
interface PostViewModelParams {
    val likeUseCase: LikeUseCase
    val commentRepository: CommentRepository
}