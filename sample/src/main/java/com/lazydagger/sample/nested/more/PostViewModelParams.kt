package com.lazydagger.sample.nested.more

import com.kshitijpatil.lazydagger.LazyDagger
import javax.inject.Inject

@LazyDagger
interface PostViewModelParams {
    val likeUseCase: LikeUseCase
    val commentRepository: CommentRepository
}

class CommentRepository @Inject constructor()

class LikeUseCase @Inject constructor()


